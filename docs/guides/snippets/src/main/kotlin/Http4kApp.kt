package codes.laurence.warden.docs

import codes.laurence.warden.http4k.CatchNotAuthorized
import codes.laurence.warden.http4k.EnforcementPointHttp4k
import codes.laurence.warden.http4k.Unwarded
import codes.laurence.warden.http4k.WardenFilter
import codes.laurence.warden.http4k.WardenRoute
import codes.laurence.warden.http4k.WardenRouteBehaviour
import codes.laurence.warden.http4k.warded
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

/** Authentication is not Warden's concern. This sample trusts a header. */
fun Request.currentUser(users: Map<String, User>): User =
    users[header("X-User")] ?: throw NotAuthenticatedException()

fun blogApp(users: Map<String, User>, articles: ArticleStore): HttpHandler {
  val enforcementPoint = EnforcementPointHttp4k(articlePolicies)
  val service = ArticleService(articles, enforcementPoint)

  val warden =
      WardenFilter(
          listOf(
              // The first matching entry wins. Health checks are open for GET only.
              WardenRoute("/health", WardenRouteBehaviour.IGNORE, setOf(GET)),
              // Everything else is enforced, which is also the default when nothing matches.
              WardenRoute("/.*", WardenRouteBehaviour.ENFORCE),
          ))
  val notAuthorized = CatchNotAuthorized { Response(Status.FORBIDDEN).body("Not Authorized") }
  val otherErrors = Filter { next ->
    { request ->
      try {
        next(request)
      } catch (_: NotAuthenticatedException) {
        Response(Status.UNAUTHORIZED).body("Not Authenticated")
      } catch (_: NoSuchElementException) {
        Response(Status.NOT_FOUND)
      }
    }
  }

  return warden
      .then(notAuthorized)
      .then(otherErrors)
      .then(
          routes(
              // Open through the filter configuration above.
              "/health" bind GET to { Response(Status.OK).body("ok") },

              // Open through an inline filter.
              "/about" bind
                  GET to
                  Unwarded.then { Response(Status.OK).body("A blog guarded by Warden") },

              // The service is suspending; `warded` bridges to it and keeps Warden in the loop.
              "/articles" bind
                  GET to
                  { request ->
                    val user = request.currentUser(users)
                    val titles = warded { service.listArticles(user) }.map { it.title }
                    Response(Status.OK).body(titles.joinToString("\n"))
                  },
              "/articles/{id}" bind
                  GET to
                  { request ->
                    val user = request.currentUser(users)
                    val article = warded { service.readArticle(user, request.path("id")!!) }
                    Response(Status.OK).body(article.title)
                  },
              "/articles/{id}" bind
                  DELETE to
                  { request ->
                    val user = request.currentUser(users)
                    warded { service.deleteArticle(user, request.path("id")!!) }
                    Response(Status.NO_CONTENT)
                  },

              // Nothing here calls the enforcement point, so Warden answers 403 instead of the
              // text.
              "/drafts" bind GET to { Response(Status.OK).body("Unpublished drafts") },
          ))
}
