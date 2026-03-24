package code.laurence.warden.ktor

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.enforce.NotAuthorizedException
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.junit.Test
import kotlin.test.assertEquals

private fun Application.testableAppWebsockets() {

    val decisionPoint = object : codes.laurence.warden.decision.DecisionPoint {
        override suspend fun checkAuthorized(request: AccessRequest): AccessResponse =
            when (request.subject["access"]) {
                "granted" -> AccessResponse(Access.Granted(), AccessRequest(subject = mapOf("returned" to "from policy")))
                "denied" -> AccessResponse(
                    Access.Denied(mapOf("message" to "Auth Denied")),
                    AccessRequest(subject = mapOf("returned" to "from policy"))
                )
                else -> AccessResponse(Access.Denied(), request)
            }

        override suspend fun checkAuthorizedBatch(request: codes.laurence.warden.AccessRequestBatch): List<AccessResponse> =
            request.resources.map { attrs ->
                val single = AccessRequest(subject = request.subject, action = request.action, environment = request.environment, resource = attrs)
                checkAuthorized(single)
            }
    }
    val enforcementPointKtor = EnforcementPointKtor(decisionPoint)
    install(io.ktor.server.websocket.WebSockets)
    install(Warden) {
        routePriorityStack = listOf(
            WardenRoute("/ws/unwardedRouteConfig", WardenRouteBehaviour.IGNORE),
        )
    }
    install(StatusPages) {
        exception<NotAuthorizedException> { call, cause ->
            call.respondText(
                cause.deniedProperties.getOrDefault("message", "No Denied message") as String,
                status = HttpStatusCode.Forbidden
            )
        }
        exception<SomeOtherException> { call, _ ->
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
                    }) {
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
    fun `ws - not warded`() = testApplication {
        application { testableAppWebsockets() }
        val client = createClient { install(io.ktor.client.plugins.websocket.WebSockets) }
        var failed = false
        try {
            client.webSocket("/ws/notWarded") { }
        } catch (e: Throwable) {
            failed = true
        }
        if (!failed) kotlin.test.fail("Expected websocket handshake to fail")
    }

    @Test
    fun `ws - unwarded block`() = testApplication {
        application { testableAppWebsockets() }
        val client = createClient { install(io.ktor.client.plugins.websocket.WebSockets) }
        client.webSocket("/ws/unwarded") {
            val frameText = (incoming.receive() as Frame.Text).readText()
            assertEquals("You should see me, enforcement ignored", frameText)
        }
    }

    @Test
    fun `ws - unwarded route config`() = testApplication {
        application { testableAppWebsockets() }
        val client = createClient { install(io.ktor.client.plugins.websocket.WebSockets) }
        client.webSocket("/ws/unwardedRouteConfig") {
            val frameText = (incoming.receive() as Frame.Text).readText()
            assertEquals("You should see me, enforcement ignored", frameText)
        }
    }

    @Test
    fun `ws - warded - denied`() = testApplication {
        application { testableAppWebsockets() }
        val client = createClient { install(io.ktor.client.plugins.websocket.WebSockets) }
        var failed = false
        try {
            client.webSocket("/ws/warded/denied") { }
        } catch (e: Throwable) {
            failed = true
        }
        if (!failed) kotlin.test.fail("Expected websocket handshake to fail")
    }

    @Test
    fun `ws - warded - granted`() = testApplication {
        application { testableAppWebsockets() }
        val client = createClient { install(io.ktor.client.plugins.websocket.WebSockets) }
        client.webSocket("/ws/warded/granted") {
            val frameText = (incoming.receive() as Frame.Text).readText()
            assertEquals("You should see me", frameText)
        }
    }

    @Test
    fun `ws - warded - exception before`() = testApplication {
        application { testableAppWebsockets() }
        val client = createClient { install(io.ktor.client.plugins.websocket.WebSockets) }
        var failed = false
        try {
            client.webSocket("/ws/warded/errorBefore") { }
        } catch (e: Throwable) {
            failed = true
        }
        if (!failed) kotlin.test.fail("Expected websocket handshake to fail")
    }
}

private class SomeOtherException : Exception()
