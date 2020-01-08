package code.laurence.warden.ktor

import code.laurence.warden.ktor.Warden.Feature.NOT_ENFORCED_MESSAGE
import code.laurence.warden.ktor.Warden.Feature.WARDEN_IGNORED
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.enforce.NotAuthorizedException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

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
    install(Warden)
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
                call.respondText("You should not be able to get me. Not Enforced")
            }
            get("/Ignored") {
                call.attributes.put(WARDEN_IGNORED, true)
                call.respondText("You should see me, enforcement ignored")
            }
        }
        route("/authorizationEnforced") {
            get("/Granted") {
                enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "granted")), call)
                call.respondText("You should see me")
            }
            get("/Denied") {
                enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "denied")), call)
                call.respondText("You should not be able to get me")
            }
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
    fun `get - enforcement point not called - ignored`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationNotEnforced/Ignored")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me, enforcement ignored", response.content)
            }
            // Check that ignoring once does not effect other calls
            with(handleRequest(HttpMethod.Get, "/authorizationNotEnforced")) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun `get - enforcement point called - granted`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationEnforced/Granted")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me", response.content)
            }
        }
    }

    @Test
    fun `get - enforcement point called - denied`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationEnforced/Denied")) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
                assertEquals("Auth Denied", response.content)
            }
        }
    }


}