package codes.laurence.warden.http4k

import codes.laurence.warden.enforce.NotAuthorizedException
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Header

/**
 * An http4k [Filter] that ensures every handler behind it is protected by Warden, or explicitly
 * exempt from authorization.
 *
 * If a handler responds successfully and no [EnforcementPointHttp4k] was invoked for the request,
 * and the request was not ignored inline or by [routeStack], the response is replaced with a 403.
 * `OPTIONS` requests are never checked, and responses that are not a success pass through.
 *
 * Example:
 *
 *       val enforcementPoint = EnforcementPointHttp4k(policies)
 *       val app = WardenFilter(listOf(WardenRoute("/health", WardenRouteBehaviour.IGNORE)))
 *           .then(CatchNotAuthorized())
 *           .then(routes(
 *               "/health" bind GET to { Response(OK) },
 *               "/about" bind GET to Unwarded.then { Response(OK).body("Open") },
 *               "/articles/{id}" bind GET to { request ->
 *                   val article = warded { service.readArticle(request.currentUser(), request.path("id")!!) }
 *                   Response(OK).body(article.title)
 *               },
 *           ))
 *
 * The filter publishes the request to the thread handling it, which is where http4k runs the
 * handler. An [EnforcementPointHttp4k] reached on that thread, or inside a [warded] block, marks
 * the request as enforced. One reached anywhere else cannot, and the handler answers 403.
 */
class WardenFilter(routePriorityStack: List<WardenRoute> = emptyList()) : Filter {
  val routeStack: List<WardenRoute> = routePriorityStack.toList()

  companion object {
    internal const val NOT_ENFORCED_MESSAGE = "Not Authorized: EnforcementPoint not enforced"
  }

  override fun invoke(next: HttpHandler): HttpHandler = { request ->
    if (request.method == Method.OPTIONS) {
      next(request)
    } else {
      val call = WardenCall()
      val response = withWardenCall(call) { next(request) }
      val configIgnored =
          evaluateRoute(routeStack, request.uri.path, request.method) == WardenRouteBehaviour.IGNORE
      val success = response.status.successful || response.status == Status.SWITCHING_PROTOCOLS
      if (call.enforced || call.ignored || configIgnored || !success) {
        response
      } else {
        Response(Status.FORBIDDEN)
            .with(Header.CONTENT_TYPE of ContentType.TEXT_PLAIN)
            .body(NOT_ENFORCED_MESSAGE)
      }
    }
  }
}

/**
 * Lets every request passing through respond without authorization being enforced, like a Ktor
 * `unwarded` block. Compose it behind the [WardenFilter], around a handler or a group of routes.
 */
object Unwarded : Filter {
  override fun invoke(next: HttpHandler): HttpHandler = { request ->
    unwarded()
    next(request)
  }
}

/** Lets the current request respond without authorization being enforced. Call it in a handler. */
fun unwarded() {
  currentWardenCall.get()?.ignored = true
}

/**
 * Turns the [NotAuthorizedException] an enforcement point throws into a response, `403` with no
 * body unless [toResponse] says otherwise. http4k has no status mapping of its own, so without this
 * filter, or one like it, a denial surfaces as whatever the server does with an exception.
 */
class CatchNotAuthorized(
    private val toResponse: (NotAuthorizedException) -> Response = { Response(Status.FORBIDDEN) }
) : Filter {
  override fun invoke(next: HttpHandler): HttpHandler = { request ->
    try {
      next(request)
    } catch (e: NotAuthorizedException) {
      toResponse(e)
    }
  }
}
