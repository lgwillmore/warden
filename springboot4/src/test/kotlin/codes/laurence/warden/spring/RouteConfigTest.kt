package codes.laurence.warden.spring

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import org.springframework.http.HttpMethod

class RouteConfigTest {

  @Test
  fun `routeStack - route not in stack - enforced`() {
    val stack: List<WardenRoute> = listOf()
    val actual = evaluateRoute(stack, "/something", HttpMethod.values().random())
    assertThat(actual).isEqualTo(WardenRouteBehaviour.ENFORCE)
  }

  @Test
  fun `routeStack - route matches - method does not`() {
    val stack: List<WardenRoute> =
        listOf(
            WardenRoute("/something", WardenRouteBehaviour.IGNORE, methods = setOf(HttpMethod.GET)))
    val actual = evaluateRoute(stack, "/something", HttpMethod.PUT)
    assertThat(actual).isEqualTo(WardenRouteBehaviour.ENFORCE)
  }

  @Test
  fun `routeStack - route matches - method matches`() {
    val stack: List<WardenRoute> =
        listOf(
            WardenRoute("/something", WardenRouteBehaviour.IGNORE, methods = setOf(HttpMethod.GET)))
    val actual = evaluateRoute(stack, "/something", HttpMethod.GET)
    assertThat(actual).isEqualTo(WardenRouteBehaviour.IGNORE)
  }

  @Test
  fun `routeStack - route regex prefix matches - method matches`() {
    val stack: List<WardenRoute> =
        listOf(WardenRoute("/some.*", WardenRouteBehaviour.IGNORE, methods = setOf(HttpMethod.GET)))
    val actual = evaluateRoute(stack, "/something", HttpMethod.GET)
    assertThat(actual).isEqualTo(WardenRouteBehaviour.IGNORE)
  }

  @Test
  fun `routeStack - route non regex prefix does not match - method matches`() {
    val stack: List<WardenRoute> =
        listOf(WardenRoute("/some", WardenRouteBehaviour.IGNORE, methods = setOf(HttpMethod.GET)))
    val actual = evaluateRoute(stack, "/something", HttpMethod.GET)
    assertThat(actual).isEqualTo(WardenRouteBehaviour.ENFORCE)
  }

  @Test
  fun `routeStack - higher priority overrides lower match`() {
    val stack: List<WardenRoute> =
        listOf(
            WardenRoute(
                "/some/thing", WardenRouteBehaviour.ENFORCE, methods = setOf(HttpMethod.GET)),
            WardenRoute("/some", WardenRouteBehaviour.IGNORE, methods = setOf(HttpMethod.GET)))
    val actual = evaluateRoute(stack, "/some/thing", HttpMethod.GET)
    assertThat(actual).isEqualTo(WardenRouteBehaviour.ENFORCE)
  }
}
