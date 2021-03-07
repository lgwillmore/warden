package code.laurence.warden.ktor

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.request.httpMethod
import io.ktor.request.uri
import io.ktor.response.ApplicationSendPipeline
import io.ktor.routing.*
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

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, WardenConfiguration, Warden> {

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
                if (!enforced) {
                    val content = TextContent(
                        NOT_ENFORCED_MESSAGE,
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.Unauthorized
                    )
                    proceedWith(content)
                }
            }
            return warden
        }
    }
}


