package code.laurence.warden.ktor

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.enforce.NotAuthorizedException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.mockk.every
import io.mockk.mockk

fun Application.testableAppDependencies() {

    val enforcementPointKtor = EnforcementPointKtor(listOf(
        mockk {
            every { checkAuthorized(AccessRequest(subject = mapOf("access" to "granted"))) } returns AccessResponse(
                Access.Granted,
                AccessRequest(subject = mapOf("returned" to "from policy"))
            )
            every { checkAuthorized(AccessRequest(subject = mapOf("access" to "denied"))) } returns AccessResponse(
                Access.Denied(mapOf("message" to "Auth Denied")),
                AccessRequest(subject = mapOf("returned" to "from policy"))
            )
        }
    )
    )
    install(Warden){
        routePriorityStack = listOf(
            WardenRoute("/api", WardenRouteBehaviour.ENFORCE),
            WardenRoute("/", WardenRouteBehaviour.IGNORE)
        )
    }
    install(StatusPages) {
        exception<NotAuthorizedException> { cause ->
            call.respondText(
                cause.deniedProperties.getOrDefault("message", "No Denied message") as String,
                status = HttpStatusCode.Unauthorized
            )
        }
    }

    routing {
        route("/authorizationNotEnforced") {
            get("") {
                wardenCall{
                    call.respondText("You should not be able to get me. Not Enforced")
                }
            }
            get("/IgnoredInline") {
                wardenIgnore()
                call.respondText("You should see me, enforcement ignored")
            }
        }
        route("/authorizationEnforced") {
            get("/Granted") {
                wardenCall {
                    enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "granted")))
                    call.respondText("You should see me")
                }
            }
            get("/Denied") {
                wardenCall{
                    enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "denied")))
                    call.respondText("You should not be able to get me")
                }
            }
            get("/wardenCallNotCalled"){
                try {
                    enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "granted")))
                    call.respondText("You should not see me due to an error")
                }catch (e: KtorEnforcementPointException){
                    wardenIgnore()
                    call.respondText("Got a KtorEnforcementError")
                }
            }
        }
    }
}
