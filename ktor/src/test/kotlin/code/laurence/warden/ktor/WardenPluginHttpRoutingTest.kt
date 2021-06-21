package code.laurence.warden.ktor

import code.laurence.warden.ktor.Warden.Feature.NOT_ENFORCED_MESSAGE
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.enforce.NotAuthorizedException
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

private fun Application.testableAppHttp() {

    val enforcementPointKtor = EnforcementPointKtor(
        listOf(
            mockk {
                every { checkAuthorized(AccessRequest(subject = mapOf("access" to "granted"))) } returns AccessResponse(
                    Access.Granted(),
                    AccessRequest(subject = mapOf("returned" to "from policy"))
                )
                every { checkAuthorized(AccessRequest(subject = mapOf("access" to "denied"))) } returns AccessResponse(
                    Access.Denied(mapOf("message" to "Auth Denied")),
                    AccessRequest(subject = mapOf("returned" to "from policy"))
                )
            }
        )
    )
    install(Warden) {
        routePriorityStack = listOf(
            WardenRoute("/authorizationNotEnforced/IgnoredInConfig", WardenRouteBehaviour.IGNORE),
        )
    }
    install(StatusPages) {
        exception<NotAuthorizedException> { cause ->
            call.respondText(
                cause.deniedProperties.getOrDefault("message", "No Denied message") as String,
                status = HttpStatusCode.Forbidden
            )
        }
    }

    routing {
        route("/authorizationNotEnforced") {
            get("") {
                warded {
                    call.respondText("You should not be able to get me. Not Enforced")
                }
            }
            get("/IgnoredInline") {
                unwarded {
                    call.respondText("You should see me, enforcement ignored")
                }
            }
            get("/IgnoredInConfig") {
                call.respondText("You should see me, enforcement ignored")
            }
        }
        route("/authorizationEnforced") {
            get("/Granted") {
                warded {
                    enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "granted")))
                    call.respondText("You should see me")
                }
            }
            get("/Denied") {
                warded {
                    enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "denied")))
                    call.respondText("You should not be able to get me")
                }
            }
            warded {
                get("/NotSuccess") {
                    call.respondText(
                        "You should see me because something else went wrong",
                        status = HttpStatusCode.Conflict
                    )
                }
            }
            get("/wardenCallNotCalled") {
                enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "granted")))
                call.respondText("You should not see me due to an not being authorized")
            }
        }
        route("/routeParentWarded") {
            warded {
                get("/Granted") {
                    enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "granted")))
                    call.respondText("You should see me")
                }
                route("/Unwarded") {
                    unwarded {
                        get {
                            call.respondText("You should see me, enforcement ignored")
                        }
                    }
                }
            }
        }
        route("/routeParentUnwarded") {
            unwarded {
                get("/NotEnforcedOrGranted") {
                    call.respondText("You should see me, enforcement ignored")
                }
            }
        }
    }
}

class WardenPluginHttpRoutingTest {

    @Test
    fun `get - enforcement point not called - 401`() {
        withTestApplication({ testableAppHttp() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationNotEnforced")) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertEquals(NOT_ENFORCED_MESSAGE, response.content)
            }
        }
    }

    @Test
    fun `get - enforcement point not called - ignored inline`() {
        withTestApplication({ testableAppHttp() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationNotEnforced/IgnoredInline")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me, enforcement ignored", response.content)
            }
            // Check that ignoring once does not effect other calls
            with(handleRequest(HttpMethod.Get, "/authorizationNotEnforced")) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }
    }

    @Test
    fun `get - enforcement point not called - not successful status code`() {
        withTestApplication({ testableAppHttp() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationEnforced/NotSuccess")) {
                assertEquals(HttpStatusCode.Conflict, response.status())
                assertEquals("You should see me because something else went wrong", response.content)
            }
        }
    }

    @Test
    fun `get - enforcement point not called - ignored in config`() {
        withTestApplication({ testableAppHttp() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationNotEnforced/IgnoredInConfig")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me, enforcement ignored", response.content)
            }
            // Check that ignoring once does not effect other calls
            with(handleRequest(HttpMethod.Get, "/authorizationNotEnforced")) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }
    }

    @Test
    fun `get - enforcement point called - granted`() {
        withTestApplication({ testableAppHttp() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationEnforced/Granted")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me", response.content)
            }
        }
    }

    @Test
    fun `get - enforcement point called - denied`() {
        withTestApplication({ testableAppHttp() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationEnforced/Denied")) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertEquals("No Denied message", response.content)
            }
        }
    }

    @Test
    fun `get - enforcement point called - warden not wrapped`() {
        withTestApplication({ testableAppHttp() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationEnforced/wardenCallNotCalled")) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }
    }

    @Test
    fun `get - parent route warded - enforcement point called - granted`() {
        withTestApplication({ testableAppHttp() }) {
            with(handleRequest(HttpMethod.Get, "/routeParentWarded/Granted")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me", response.content)
            }
        }
    }

    @Test
    fun `get - parent route warded - nested route unwarded - enforcement point called - granted`() {
        withTestApplication({ testableAppHttp() }) {
            with(handleRequest(HttpMethod.Get, "/routeParentWarded/Unwarded")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me, enforcement ignored", response.content)
            }
        }
    }

    @Test
    fun `get - parent route unwarded - endpoint not enforced`() {
        withTestApplication({ testableAppHttp() }) {
            with(handleRequest(HttpMethod.Get, "/routeParentUnwarded/NotEnforcedOrGranted")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me, enforcement ignored", response.content)
            }
        }
    }
}
