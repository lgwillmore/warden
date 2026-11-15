package codes.laurence.warden.http4k

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessRequestBatch
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.http4k.WardenFilter.Companion.NOT_ENFORCED_MESSAGE
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.http4k.core.HttpHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.OPTIONS
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.routes

private val decisionPoint =
    object : DecisionPoint {
      override suspend fun checkAuthorized(request: AccessRequest): AccessResponse =
          when (request.subject["access"]) {
            "granted" ->
                AccessResponse(
                    Access.Granted(), AccessRequest(subject = mapOf("returned" to "from policy")))
            "denied" ->
                AccessResponse(
                    Access.Denied(mapOf("message" to "Auth Denied")),
                    AccessRequest(subject = mapOf("returned" to "from policy")))
            else -> AccessResponse(Access.Denied(), request)
          }

      override suspend fun checkAuthorizedBatch(request: AccessRequestBatch): List<AccessResponse> =
          request.resources.map { attrs ->
            val single =
                AccessRequest(
                    subject = request.subject,
                    action = request.action,
                    environment = request.environment,
                    resource = attrs)
            checkAuthorized(single)
          }
    }

private val enforcementPoint = EnforcementPointHttp4k(decisionPoint)

private fun granted() = AccessRequest(subject = mapOf("access" to "granted"))

private fun denied() = AccessRequest(subject = mapOf("access" to "denied"))

private fun testableApp(): HttpHandler {
  val warden =
      WardenFilter(
          listOf(
              WardenRoute("/authorizationNotEnforced/IgnoredInConfig", WardenRouteBehaviour.IGNORE),
              WardenRoute(
                  "/authorizationNotEnforced/IgnoredInConfigForGet",
                  WardenRouteBehaviour.IGNORE,
                  setOf(GET)),
          ))
  val notAuthorized = CatchNotAuthorized { cause ->
    Response(Status.FORBIDDEN)
        .body(cause.deniedProperties.getOrDefault("message", "No Denied message") as String)
  }
  return warden
      .then(notAuthorized)
      .then(
          routes(
              "/authorizationNotEnforced" bind
                  GET to
                  {
                    Response(Status.OK).body("You should not be able to get me. Not Enforced")
                  },
              "/authorizationNotEnforced/IgnoredInline" bind
                  GET to
                  Unwarded.then {
                    Response(Status.OK).body("You should see me, enforcement ignored")
                  },
              "/authorizationNotEnforced/IgnoredInHandler" bind
                  GET to
                  {
                    unwarded()
                    Response(Status.OK).body("You should see me, enforcement ignored")
                  },
              "/authorizationNotEnforced/IgnoredInConfig" bind
                  GET to
                  {
                    Response(Status.OK).body("You should see me, enforcement ignored")
                  },
              "/authorizationNotEnforced/IgnoredInConfigForGet" bind
                  DELETE to
                  {
                    Response(Status.OK).body("You should not be able to delete me. Not Enforced")
                  },
              "/authorizationNotEnforced/Options" bind
                  OPTIONS to
                  {
                    Response(Status.OK).body("You should see me, OPTIONS is never checked")
                  },
              "/authorizationNotEnforced/SwitchingProtocols" bind
                  GET to
                  {
                    Response(Status.SWITCHING_PROTOCOLS)
                  },
              "/authorizationEnforced/Granted" bind
                  GET to
                  {
                    runBlocking { enforcementPoint.enforceAuthorization(granted()) }
                    Response(Status.OK).body("You should see me")
                  },
              "/authorizationEnforced/GrantedBlocking" bind
                  GET to
                  {
                    enforcementPoint.enforceAuthorizationBlocking(granted())
                    Response(Status.OK).body("You should see me")
                  },
              "/authorizationEnforced/Denied" bind
                  GET to
                  {
                    runBlocking { enforcementPoint.enforceAuthorization(denied()) }
                    Response(Status.OK).body("You should not be able to get me")
                  },
              "/authorizationEnforced/NotSuccess" bind
                  GET to
                  {
                    Response(Status.CONFLICT)
                        .body("You should see me because something else went wrong")
                  },
              "/authorizationEnforced/OffThread" bind
                  GET to
                  {
                    // A bare runBlocking loses the request once the coroutine leaves the thread.
                    runBlocking {
                      withContext(Dispatchers.IO) {
                        enforcementPoint.enforceAuthorization(granted())
                      }
                    }
                    Response(Status.OK)
                        .body("You should not see me, Warden could not see the request")
                  },
              "/authorizationEnforced/OffThreadWarded" bind
                  GET to
                  {
                    warded {
                      withContext(Dispatchers.IO) {
                        enforcementPoint.enforceAuthorization(granted())
                      }
                    }
                    Response(Status.OK).body("You should see me")
                  },
              "/routeParentUnwarded" bind
                  Unwarded.then(
                      routes(
                          "/NotEnforcedOrGranted" bind
                              GET to
                              {
                                Response(Status.OK).body("You should see me, enforcement ignored")
                              })),
          ))
}

class WardenFilterRoutingTest {

  private val app = testableApp()

  @Test
  fun `get - enforcement point not called - 403`() {
    val response = app(Request(GET, "/authorizationNotEnforced"))
    assertThat(response.status).isEqualTo(Status.FORBIDDEN)
    assertThat(response.bodyString()).isEqualTo(NOT_ENFORCED_MESSAGE)
    assertThat(response.header("content-type")).isEqualTo("text/plain; charset=utf-8")
  }

  @Test
  fun `get - enforcement point not called - ignored inline`() {
    var response = app(Request(GET, "/authorizationNotEnforced/IgnoredInline"))
    assertThat(response.status).isEqualTo(Status.OK)
    assertThat(response.bodyString()).isEqualTo("You should see me, enforcement ignored")
    // Check that ignoring once does not effect other calls
    response = app(Request(GET, "/authorizationNotEnforced"))
    assertThat(response.status).isEqualTo(Status.FORBIDDEN)
  }

  @Test
  fun `get - enforcement point not called - ignored in handler`() {
    val response = app(Request(GET, "/authorizationNotEnforced/IgnoredInHandler"))
    assertThat(response.status).isEqualTo(Status.OK)
    assertThat(response.bodyString()).isEqualTo("You should see me, enforcement ignored")
  }

  @Test
  fun `get - enforcement point not called - not successful status code`() {
    val response = app(Request(GET, "/authorizationEnforced/NotSuccess"))
    assertThat(response.status).isEqualTo(Status.CONFLICT)
    assertThat(response.bodyString())
        .isEqualTo("You should see me because something else went wrong")
  }

  @Test
  fun `get - enforcement point not called - switching protocols counts as success`() {
    val response = app(Request(GET, "/authorizationNotEnforced/SwitchingProtocols"))
    assertThat(response.status).isEqualTo(Status.FORBIDDEN)
  }

  @Test
  fun `get - enforcement point not called - ignored in config`() {
    var response = app(Request(GET, "/authorizationNotEnforced/IgnoredInConfig"))
    assertThat(response.status).isEqualTo(Status.OK)
    assertThat(response.bodyString()).isEqualTo("You should see me, enforcement ignored")
    // Check that ignoring once does not effect other calls
    response = app(Request(GET, "/authorizationNotEnforced"))
    assertThat(response.status).isEqualTo(Status.FORBIDDEN)
  }

  @Test
  fun `delete - enforcement point not called - ignored in config for another method only`() {
    val response = app(Request(DELETE, "/authorizationNotEnforced/IgnoredInConfigForGet"))
    assertThat(response.status).isEqualTo(Status.FORBIDDEN)
  }

  @Test
  fun `get - enforcement point not called - query string does not defeat the config`() {
    val response = app(Request(GET, "/authorizationNotEnforced/IgnoredInConfig?page=2"))
    assertThat(response.status).isEqualTo(Status.OK)
  }

  @Test
  fun `options - enforcement point not called - never checked`() {
    val response = app(Request(OPTIONS, "/authorizationNotEnforced/Options"))
    assertThat(response.status).isEqualTo(Status.OK)
  }

  @Test
  fun `get - enforcement point called - granted`() {
    val response = app(Request(GET, "/authorizationEnforced/Granted"))
    assertThat(response.status).isEqualTo(Status.OK)
    assertThat(response.bodyString()).isEqualTo("You should see me")
  }

  @Test
  fun `get - enforcement point called blocking - granted`() {
    val response = app(Request(GET, "/authorizationEnforced/GrantedBlocking"))
    assertThat(response.status).isEqualTo(Status.OK)
    assertThat(response.bodyString()).isEqualTo("You should see me")
  }

  @Test
  fun `get - enforcement point called - denied`() {
    val response = app(Request(GET, "/authorizationEnforced/Denied"))
    assertThat(response.status).isEqualTo(Status.FORBIDDEN)
    assertThat(response.bodyString()).isEqualTo("Auth Denied")
  }

  @Test
  fun `get - enforcement point called off the request thread - not seen - 403`() {
    val response = app(Request(GET, "/authorizationEnforced/OffThread"))
    assertThat(response.status).isEqualTo(Status.FORBIDDEN)
    assertThat(response.bodyString()).isEqualTo(NOT_ENFORCED_MESSAGE)
  }

  @Test
  fun `get - enforcement point called off the request thread inside warded - granted`() {
    val response = app(Request(GET, "/authorizationEnforced/OffThreadWarded"))
    assertThat(response.status).isEqualTo(Status.OK)
    assertThat(response.bodyString()).isEqualTo("You should see me")
  }

  @Test
  fun `get - parent route unwarded - endpoint not enforced`() {
    val response = app(Request(GET, "/routeParentUnwarded/NotEnforcedOrGranted"))
    assertThat(response.status).isEqualTo(Status.OK)
    assertThat(response.bodyString()).isEqualTo("You should see me, enforcement ignored")
  }

  @Test
  fun `enforcement point called outside the filter - still enforces, nothing to mark`() {
    val response =
        CatchNotAuthorized().then { _ ->
          runBlocking { enforcementPoint.enforceAuthorization(denied()) }
          Response(Status.OK)
        }(Request(GET, "/anything"))
    assertThat(response.status).isEqualTo(Status.FORBIDDEN)
  }

  @Test
  fun `the request thread is clean after a request`() {
    app(Request(GET, "/authorizationEnforced/Granted"))
    assertThat(currentWardenCall.get()).isNull()
  }
}
