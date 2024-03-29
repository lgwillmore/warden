package code.laurence.warden.ktor

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.Plugin
import io.ktor.server.application.call
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.ApplicationSendPipeline
import io.ktor.server.routing.*
import io.ktor.util.AttributeKey

/**
 * A ktor Feature that ensures that all your routes are protected by Warden, or explicitly exempt from Authorization.
 *
 * If a route is called, and the EnforcementPointKtor instance is not enforced or ignored,
 * it will be intercepted and respond with a 401.
 *
 * Example Installation:
 *
 *       install(Warden)
 *       install(StatusPages) {
 *          exception<NotAuthorizedException> { cause ->
 *              call.respondText(
 *                  "Not Authorized",
 *                  status = HttpStatusCode.Unauthorized
 *              )
 *          }
 *       }
 *
 * Once installed, you must then use the EnforcementPointKtor in each of your routes, or ignore the route.
 *
 * Example Routing:
 *
 *       routing {
 *          warded{
 *              route("/authorizationNotEnforced"){
 *                  get("") {
 *                      call.respondText("You should not be able to get me, you did not enforce authorization")
 *                  }
 *                  get("/Ignored"){
 *                      unwarded {
 *                          call.respondText("You should see me")
 *                      }
 *                  }
 *              }
 *              get("/authorizationEnforced"){
 *                  val accessRequest: AccessRequest = <build an access>
 *                  enforcementPointKtor.enforceAuthorization(accessRequest, call)
 *                  call.respondText("You should see me if you are authorized")
 *              }
 *          }
 *       }
 */
class Warden(config: WardenConfiguration) {
    val routeStack: List<WardenRoute> = config.routePriorityStack.toList()

    class WardenConfiguration {
        var routePriorityStack: List<WardenRoute> = emptyList()
    }

    companion object Feature : Plugin<ApplicationCallPipeline, WardenConfiguration, Warden> {

        internal val WARDEN_ENFORCED = AttributeKey<Boolean>("warden.enforced")
        val WARDEN_IGNORED = AttributeKey<Boolean>("warden.ignored")
        internal const val NOT_ENFORCED_MESSAGE = "Not Authorized: EnforcementPoint not enforced"

        override val key = AttributeKey<Warden>("Warden")

        override fun install(pipeline: ApplicationCallPipeline, configure: WardenConfiguration.() -> Unit): Warden {
            val config = WardenConfiguration().apply(configure)
            val warden = Warden(config)

            pipeline.sendPipeline.intercept(ApplicationSendPipeline.After) {
                val configIgnored = when (evaluateRoute(warden.routeStack, call.request.uri, call.request.httpMethod)) {
                    WardenRouteBehaviour.ENFORCE -> false
                    WardenRouteBehaviour.IGNORE -> true
                }
                val inlineIgnored = call.attributes.getOrNull(WARDEN_IGNORED) ?: false
                if (inlineIgnored || configIgnored) return@intercept
                val enforced = call.attributes.getOrNull(WARDEN_ENFORCED) ?: false
                val status: Int = when (val s = subject) {
                    is OutgoingContent -> {
                        val subjectStatus = s.status?.value
                        if (subjectStatus == null) {
                            val responseStatus: Int? = call.response.status()?.value
                            responseStatus ?: 200
                        } else {
                            subjectStatus
                        }
                    }
                    else -> 200
                }
                if (!enforced && (status in 200..299 || status == 101)) {
                    val content = TextContent(
                        NOT_ENFORCED_MESSAGE,
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.Forbidden
                    )
                    proceedWith(content)
                }
            }
            return warden
        }
    }
}
