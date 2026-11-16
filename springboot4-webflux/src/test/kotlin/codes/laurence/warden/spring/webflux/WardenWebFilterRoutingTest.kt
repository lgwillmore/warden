package codes.laurence.warden.spring.webflux

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessRequestBatch
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.spring.EnforcementPointSpring
import codes.laurence.warden.spring.Unwarded
import codes.laurence.warden.spring.WardenCall.Companion.NOT_ENFORCED_MESSAGE
import codes.laurence.warden.spring.WardenNotAuthorizedAdvice
import codes.laurence.warden.spring.WardenRoute
import codes.laurence.warden.spring.WardenRouteBehaviour
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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

private val enforcementPoint = EnforcementPointSpring(decisionPoint)

private fun grantedRequest() = AccessRequest(subject = mapOf("access" to "granted"))

private fun deniedRequest() = AccessRequest(subject = mapOf("access" to "denied"))

@RestController
private class TestController {

  @GetMapping("/authorizationNotEnforced")
  suspend fun notEnforced() = "You should not be able to get me. Not Enforced"

  @Unwarded
  @GetMapping("/authorizationNotEnforced/IgnoredByAnnotation")
  suspend fun ignoredByAnnotation() = "You should see me, enforcement ignored"

  @GetMapping("/authorizationNotEnforced/IgnoredInHandler")
  suspend fun ignoredInHandler(): String {
    unwarded()
    return "You should see me, enforcement ignored"
  }

  @GetMapping("/authorizationNotEnforced/IgnoredWithExchange")
  fun ignoredWithExchange(exchange: ServerWebExchange): Mono<String> {
    exchange.unwarded()
    return Mono.just("You should see me, enforcement ignored")
  }

  @GetMapping("/authorizationNotEnforced/IgnoredInConfig")
  suspend fun ignoredInConfig() = "You should see me, enforcement ignored"

  @DeleteMapping("/authorizationNotEnforced/IgnoredInConfigForGet")
  suspend fun ignoredInConfigForGet() = "You should not be able to delete me. Not Enforced"

  @RequestMapping("/authorizationNotEnforced/Options", method = [RequestMethod.OPTIONS])
  suspend fun options() = "You should see me, OPTIONS is never checked"

  @GetMapping("/authorizationNotEnforced/SwitchingProtocols")
  fun switchingProtocols(): ResponseEntity<Void> =
      ResponseEntity.status(HttpStatus.SWITCHING_PROTOCOLS).build()

  @GetMapping("/authorizationNotEnforced/NoBody") suspend fun noBody() {}

  @GetMapping("/authorizationNotEnforced/NoContent")
  fun noContent(): ResponseEntity<Void> = ResponseEntity.noContent().build()

  @GetMapping("/authorizationNotEnforced/Stream")
  fun stream(): Flux<String> = Flux.just("You should ", "not see me")

  @GetMapping("/authorizationEnforced/Granted")
  suspend fun granted(): String {
    enforcementPoint.enforceAuthorization(grantedRequest())
    return "You should see me"
  }

  @GetMapping("/authorizationEnforced/GrantedOffThread")
  suspend fun grantedOffThread(): String {
    withContext(Dispatchers.IO) { enforcementPoint.enforceAuthorization(grantedRequest()) }
    return "You should see me"
  }

  @GetMapping("/authorizationEnforced/GrantedReactive")
  fun grantedReactive(): Mono<String> =
      mono { enforcementPoint.enforceAuthorization(grantedRequest()) }
          .thenReturn("You should see me")

  @GetMapping("/authorizationEnforced/GrantedStream")
  fun grantedStream(): Flux<String> =
      mono { enforcementPoint.enforceAuthorization(grantedRequest()) }
          .thenMany(Flux.just("You ", "should ", "see me"))

  @GetMapping("/authorizationEnforced/GrantedNoBody")
  suspend fun grantedNoBody() {
    enforcementPoint.enforceAuthorization(grantedRequest())
  }

  @GetMapping("/authorizationEnforced/Denied")
  suspend fun denied(): String {
    enforcementPoint.enforceAuthorization(deniedRequest())
    return "You should not be able to get me"
  }

  @GetMapping("/authorizationEnforced/NotSuccess")
  fun notSuccess(): ResponseEntity<String> =
      ResponseEntity.status(HttpStatus.CONFLICT)
          .body("You should see me because something else went wrong")
}

@Unwarded
@RestController
@RequestMapping("/controllerUnwarded")
private class UnwardedController {
  @GetMapping("/NotEnforcedOrGranted") suspend fun open() = "You should see me, enforcement ignored"
}

class WardenWebFilterRoutingTest {

  private val client: WebTestClient =
      WebTestClient.bindToController(TestController(), UnwardedController())
          .webFilter<WebTestClient.ControllerSpec>(
              WardenWebFilter(
                  listOf(
                      WardenRoute(
                          "/authorizationNotEnforced/IgnoredInConfig", WardenRouteBehaviour.IGNORE),
                      WardenRoute(
                          "/authorizationNotEnforced/IgnoredInConfigForGet",
                          WardenRouteBehaviour.IGNORE,
                          setOf(org.springframework.http.HttpMethod.GET)),
                  )))
          .controllerAdvice(
              WardenNotAuthorizedAdvice { cause ->
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(cause.deniedProperties.getOrDefault("message", "No Denied message"))
              })
          .build()

  private fun get(path: String) = client.get().uri(path).exchange()

  @Test
  fun `get - enforcement point not called - 403`() {
    get("/authorizationNotEnforced")
        .expectStatus()
        .isForbidden
        .expectHeader()
        .contentType(MediaType("text", "plain", Charsets.UTF_8))
        .expectBody(String::class.java)
        .isEqualTo(NOT_ENFORCED_MESSAGE)
  }

  @Test
  fun `get - enforcement point not called - ignored by annotation`() {
    get("/authorizationNotEnforced/IgnoredByAnnotation")
        .expectStatus()
        .isOk
        .expectBody(String::class.java)
        .isEqualTo("You should see me, enforcement ignored")
    // Check that ignoring once does not effect other calls
    get("/authorizationNotEnforced").expectStatus().isForbidden
  }

  @Test
  fun `get - enforcement point not called - ignored in handler`() {
    get("/authorizationNotEnforced/IgnoredInHandler")
        .expectStatus()
        .isOk
        .expectBody(String::class.java)
        .isEqualTo("You should see me, enforcement ignored")
  }

  @Test
  fun `get - enforcement point not called - ignored with the exchange`() {
    get("/authorizationNotEnforced/IgnoredWithExchange")
        .expectStatus()
        .isOk
        .expectBody(String::class.java)
        .isEqualTo("You should see me, enforcement ignored")
  }

  @Test
  fun `get - enforcement point not called - ignored in config`() {
    get("/authorizationNotEnforced/IgnoredInConfig")
        .expectStatus()
        .isOk
        .expectBody(String::class.java)
        .isEqualTo("You should see me, enforcement ignored")
    get("/authorizationNotEnforced").expectStatus().isForbidden
  }

  @Test
  fun `delete - enforcement point not called - ignored in config for another method only`() {
    client
        .delete()
        .uri("/authorizationNotEnforced/IgnoredInConfigForGet")
        .exchange()
        .expectStatus()
        .isForbidden
  }

  @Test
  fun `get - enforcement point not called - query string does not defeat the config`() {
    get("/authorizationNotEnforced/IgnoredInConfig?page=2").expectStatus().isOk
  }

  @Test
  fun `options - enforcement point not called - never checked`() {
    client.options().uri("/authorizationNotEnforced/Options").exchange().expectStatus().isOk
  }

  @Test
  fun `get - enforcement point not called - switching protocols counts as success`() {
    get("/authorizationNotEnforced/SwitchingProtocols").expectStatus().isForbidden
  }

  @Test
  fun `get - enforcement point not called - no body - 403`() {
    get("/authorizationNotEnforced/NoBody")
        .expectStatus()
        .isForbidden
        .expectBody(String::class.java)
        .isEqualTo(NOT_ENFORCED_MESSAGE)
  }

  @Test
  fun `get - enforcement point not called - no content - 403`() {
    get("/authorizationNotEnforced/NoContent")
        .expectStatus()
        .isForbidden
        .expectBody(String::class.java)
        .isEqualTo(NOT_ENFORCED_MESSAGE)
  }

  @Test
  fun `get - enforcement point not called - streamed body - 403`() {
    get("/authorizationNotEnforced/Stream")
        .expectStatus()
        .isForbidden
        .expectBody(String::class.java)
        .isEqualTo(NOT_ENFORCED_MESSAGE)
  }

  @Test
  fun `get - enforcement point not called - not successful status code`() {
    get("/authorizationEnforced/NotSuccess")
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT)
        .expectBody(String::class.java)
        .isEqualTo("You should see me because something else went wrong")
  }

  @Test
  fun `get - enforcement point called - granted`() {
    get("/authorizationEnforced/Granted")
        .expectStatus()
        .isOk
        .expectBody(String::class.java)
        .isEqualTo("You should see me")
  }

  @Test
  fun `get - enforcement point called on another dispatcher - granted`() {
    get("/authorizationEnforced/GrantedOffThread")
        .expectStatus()
        .isOk
        .expectBody(String::class.java)
        .isEqualTo("You should see me")
  }

  @Test
  fun `get - enforcement point called from a reactive handler - granted`() {
    get("/authorizationEnforced/GrantedReactive")
        .expectStatus()
        .isOk
        .expectBody(String::class.java)
        .isEqualTo("You should see me")
  }

  @Test
  fun `get - enforcement point called before a streamed body - granted, body intact`() {
    get("/authorizationEnforced/GrantedStream")
        .expectStatus()
        .isOk
        .expectBody(String::class.java)
        .isEqualTo("You should see me")
  }

  @Test
  fun `get - enforcement point called - no body - granted`() {
    get("/authorizationEnforced/GrantedNoBody").expectStatus().isOk.expectBody().isEmpty
  }

  @Test
  fun `get - enforcement point called - denied`() {
    get("/authorizationEnforced/Denied")
        .expectStatus()
        .isForbidden
        .expectBody(String::class.java)
        .isEqualTo("Auth Denied")
  }

  @Test
  fun `get - controller unwarded - endpoint not enforced`() {
    get("/controllerUnwarded/NotEnforcedOrGranted")
        .expectStatus()
        .isOk
        .expectBody(String::class.java)
        .isEqualTo("You should see me, enforcement ignored")
  }

  @Test
  fun `enforcement point called outside the filter - still enforces, nothing to mark`() {
    val response =
        WebTestClient.bindToController(TestController())
            .controllerAdvice(WardenNotAuthorizedAdvice())
            .build()
            .get()
            .uri("/authorizationEnforced/Denied")
            .exchange()
    response.expectStatus().isForbidden
    assertThat(response.returnResult(String::class.java).responseBody.blockFirst()).isEqualTo(null)
  }
}
