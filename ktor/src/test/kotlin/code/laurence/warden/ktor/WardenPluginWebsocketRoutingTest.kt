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

private fun Application.testableAppWebsockets() {

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
        exception<SomeOtherException> {
            call.respond(
                status = HttpStatusCode.InternalServerError,
                "Something went wrong"
            )
        }
    }

    routing {
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

                    beforeEach({
                        throw SomeOtherException()
                    }){
                        webSocket("/errorBefore") {
                            outgoing.send(Frame.Text("You should not see me"))
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

class WardenPluginWebsocketRoutingTest {

    @Test
    fun `ws - not warded`() {
        withTestApplication({ testableAppWebsockets() }) {
            val call = handleWebSocket("/ws/notWarded") {}
            assertEquals(HttpStatusCode.Forbidden, call.response.status())
            assertEquals(NOT_ENFORCED_MESSAGE, call.response.content)
        }
    }

    @Test
    fun `ws - unwarded block`() {
        withTestApplication({ testableAppWebsockets() }) {
            handleWebSocketConversation("/ws/unwarded") { incoming, _ ->
                val frameText = (incoming.receive() as Frame.Text).readText()
                assertEquals("You should see me, enforcement ignored", frameText)
            }
        }
    }

    @Test
    fun `ws - unwarded route config`() {
        withTestApplication({ testableAppWebsockets() }) {
            handleWebSocketConversation("/ws/unwardedRouteConfig") { incoming, _ ->
                val frameText = (incoming.receive() as Frame.Text).readText()
                assertEquals("You should see me, enforcement ignored", frameText)
            }
        }
    }

    @Test
    fun `ws - warded - denied`() {
        withTestApplication({ testableAppWebsockets() }) {
            val call = handleWebSocket("/ws/warded/denied") {}
            assertEquals(HttpStatusCode.Unauthorized, call.response.status())
            assertEquals("No Denied message", call.response.content)
        }
    }

    @Test
    fun `ws - warded - granted`() {
        withTestApplication({ testableAppWebsockets() }) {
            handleWebSocketConversation("/ws/warded/granted") { incoming, _ ->
                val frameText = (incoming.receive() as Frame.Text).readText()
                assertEquals("You should see me", frameText)
            }
        }
    }

    @Test
    fun `ws - warded - exception before`() {
        withTestApplication({ testableAppWebsockets() }) {
            val call = handleWebSocket("/ws/warded/errorBefore") {}
            assertEquals(HttpStatusCode.InternalServerError, call.response.status())
        }
    }
}

private class SomeOtherException: Exception()