package code.laurence.warden.ktor

import code.laurence.warden.ktor.Warden.Feature.NOT_ENFORCED_MESSAGE
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.enforce.NotAuthorizedException
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

fun Application.testableAppDependencies() {

    val enforcementPointKtor = EnforcementPointKtor(listOf(
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
    ))
    install(WebSockets)
    install(Warden) {
        routePriorityStack = listOf(
            WardenRoute("/authorizationNotEnforced/IgnoredInConfig", WardenRouteBehaviour.IGNORE),
            WardenRoute("/ws/unwardedRouteConfig", WardenRouteBehaviour.IGNORE),
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
        route("/ws") {
            webSocket("/notWarded") {
                outgoing.send(Frame.Text("You should not be able to get me. Unwarded websocket"))
            }
            route("unwarded") {
                unwarded {
                    webSocket {
                        outgoing.send(Frame.Text("You should see me, enforcement ignored"))
                    }
                }
            }
            webSocket("/unwardedRouteConfig") {
                outgoing.send(Frame.Text("You should see me, enforcement ignored"))
            }
            route("/warded") {
                warded {
                    beforeEach({
                        enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "granted")))
                    }) {
                        webSocket("/granted") {
                            outgoing.send(Frame.Text("You should see me"))
                        }
                    }

                    route("/denied") {
                        beforeEach({
                            enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "denied")))
                        }) {
                            webSocket {
                                outgoing.send(Frame.Text("You should not see me"))
                            }
                        }
                    }
                }
            }
        }
    }
}

class WardenTest {

    @Test
    fun `get - enforcement point not called - 401`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationNotEnforced")) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
                assertEquals(NOT_ENFORCED_MESSAGE, response.content)
            }
        }
    }

    @Test
    fun `get - enforcement point not called - ignored inline`() {
        withTestApplication({ testableAppDependencies() }) {
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
    fun `get - enforcement point not called - ignored in config`() {
        withTestApplication({ testableAppDependencies() }) {
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
                assertEquals("No Denied message", response.content)
            }
        }
    }

    @Test
    fun `get - enforcement point called - warden not wrapped`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/authorizationEnforced/wardenCallNotCalled")) {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }
    }

    @Test
    fun `get - parent route warded - enforcement point called - granted`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/routeParentWarded/Granted")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me", response.content)
            }
        }
    }

    @Test
    fun `get - parent route warded - nested route unwarded - enforcement point called - granted`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/routeParentWarded/Unwarded")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me, enforcement ignored", response.content)
            }
        }
    }

    @Test
    fun `get - parent route unwarded - endpoint not enforced`() {
        withTestApplication({ testableAppDependencies() }) {
            with(handleRequest(HttpMethod.Get, "/routeParentUnwarded/NotEnforcedOrGranted")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("You should see me, enforcement ignored", response.content)
            }
        }
    }

    @Test
    fun `ws - not warded`() {
        withTestApplication({ testableAppDependencies() }) {
            val call = handleWebSocket("/ws/notWarded") {}
            assertEquals(HttpStatusCode.Forbidden, call.response.status())
            assertEquals(NOT_ENFORCED_MESSAGE, call.response.content)
        }
    }

    @Test
    fun `ws - unwarded block`() {
        withTestApplication({ testableAppDependencies() }) {
            handleWebSocketConversation("/ws/unwarded") { incoming, _ ->
                val frameText = (incoming.receive() as Frame.Text).readText()
                assertEquals("You should see me, enforcement ignored", frameText)
            }
        }
    }

    @Test
    fun `ws - unwarded route config`() {
        withTestApplication({ testableAppDependencies() }) {
            handleWebSocketConversation("/ws/unwardedRouteConfig") { incoming, _ ->
                val frameText = (incoming.receive() as Frame.Text).readText()
                assertEquals("You should see me, enforcement ignored", frameText)
            }
        }
    }

    @Test
    fun `ws - warded - denied`() {
        withTestApplication({ testableAppDependencies() }) {
            val call = handleWebSocket("/ws/warded/denied") {}
            assertEquals(HttpStatusCode.Unauthorized, call.response.status())
            assertEquals("No Denied message", call.response.content)
        }
    }

    @Test
    fun `ws - warded - granted`() {
        withTestApplication({ testableAppDependencies() }) {
            handleWebSocketConversation("/ws/warded/granted") { incoming, _ ->
                val frameText = (incoming.receive() as Frame.Text).readText()
                assertEquals("You should see me", frameText)
            }
        }
    }

}