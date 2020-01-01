package code.laurence.warden.ktor

import code.laurence.warden.ktor.Warden.Feature.NOT_ENFORCED_MESSAGE
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.decision.DecisionPointInMemory
import codes.laurence.warden.enforce.NotAuthorizedException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

fun Application.testableAppDependencies() {

    val enforcementPointKtor = EnforcementPointKtor(DecisionPointInMemory(
        listOf(
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
    ))
    install(Warden) {
        enforcementPoint = enforcementPointKtor
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
        get("/authorizationNotEnforced") {
            call.respondText("You should not be able to get me")
        }
        get("/authorizationEnforcedGranted") {
            enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "granted")), call)
            call.respondText("You should see me")
        }
        get("/authorizationEnforcedDenied") {
            enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "denied")), call)
            call.respondText("You should not be able to get me")
        }
    }
}

class WardenTest {

    @Test
    fun `get - enforcement point not called - 401`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationNotEnforced")) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
                assertEquals(NOT_ENFORCED_MESSAGE, response.content)
            }
        }
    }

    @Test
    fun `get - enforcement point called - granted`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationEnforcedGranted")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me", response.content)
            }
        }
    }

    @Test
    fun `get - enforcement point called - denied`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationEnforcedDenied")) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
                assertEquals("Auth Denied", response.content)
            }
        }
    }


}