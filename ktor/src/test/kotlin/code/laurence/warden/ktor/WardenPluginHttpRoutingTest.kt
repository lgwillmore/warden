package code.laurence.warden.ktor

import code.laurence.warden.ktor.Warden.Feature.NOT_ENFORCED_MESSAGE
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.enforce.NotAuthorizedException
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals

private fun Application.testableAppHttp() {

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
    install(Warden) {
        routePriorityStack = listOf(
            WardenRoute("/authorizationNotEnforced/IgnoredInConfig", WardenRouteBehaviour.IGNORE),
        )
    }
    install(StatusPages) {
        exception<NotAuthorizedException> { call, cause ->
            call.respondText(
                cause.deniedProperties.getOrDefault("message", "No Denied message") as String,
                status = HttpStatusCode.Forbidden
            )
        }
    }

    routing {
        route("/authorizationNotEnforced") {
            warded {
                get("") {
                    call.respondText("You should not be able to get me. Not Enforced")
                }
            }
            unwarded {
                get("/IgnoredInline") {
                    call.respondText("You should see me, enforcement ignored")
                }
            }
            get("/IgnoredInConfig") {
                call.respondText("You should see me, enforcement ignored")
            }
        }
        route("/authorizationEnforced") {
            warded {
                get("/Granted") {
                    enforcementPointKtor.enforceAuthorization(AccessRequest(subject = mapOf("access" to "granted")))
                    call.respondText("You should see me")
                }
                get("/Denied") {
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
    fun `get - enforcement point not called - 401`() = testApplication {
        application { testableAppHttp() }
        val response = client.get("/authorizationNotEnforced")
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals(NOT_ENFORCED_MESSAGE, response.bodyAsText())
    }

    @Test
    fun `get - enforcement point not called - ignored inline`() = testApplication {
        application { testableAppHttp() }
        var response = client.get("/authorizationNotEnforced/IgnoredInline")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("You should see me, enforcement ignored", response.bodyAsText())
        // Check that ignoring once does not effect other calls
        response = client.get("/authorizationNotEnforced")
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `get - enforcement point not called - not successful status code`() = testApplication {
        application { testableAppHttp() }
        val response = client.get("/authorizationEnforced/NotSuccess")
        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals("You should see me because something else went wrong", response.bodyAsText())
    }

    @Test
    fun `get - enforcement point not called - ignored in config`() = testApplication {
        application { testableAppHttp() }
        var response = client.get("/authorizationNotEnforced/IgnoredInConfig")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("You should see me, enforcement ignored", response.bodyAsText())
        // Check that ignoring once does not effect other calls
        response = client.get("/authorizationNotEnforced")
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `get - enforcement point called - granted`() = testApplication {
        application { testableAppHttp() }
        val response = client.get("/authorizationEnforced/Granted")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("You should see me", response.bodyAsText())
    }

    @Test
    fun `get - enforcement point called - denied`() = testApplication {
        application { testableAppHttp() }
        val response = client.get("/authorizationEnforced/Denied")
        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals("Auth Denied", response.bodyAsText())
    }

    @Test
    fun `get - enforcement point called - warden not wrapped`() = testApplication {
        application { testableAppHttp() }
        val response = client.get("/authorizationEnforced/wardenCallNotCalled")
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `get - parent route warded - enforcement point called - granted`() = testApplication {
        application { testableAppHttp() }
        val response = client.get("/routeParentWarded/Granted")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("You should see me", response.bodyAsText())
    }

    @Test
    fun `get - parent route warded - nested route unwarded - enforcement point called - granted`() = testApplication {
        application { testableAppHttp() }
        val response = client.get("/routeParentWarded/Unwarded")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("You should see me, enforcement ignored", response.bodyAsText())
    }

    @Test
    fun `get - parent route unwarded - endpoint not enforced`() = testApplication {
        application { testableAppHttp() }
        val response = client.get("/routeParentUnwarded/NotEnforcedOrGranted")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("You should see me, enforcement ignored", response.bodyAsText())
    }
}
