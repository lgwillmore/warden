package codes.laurence.warden.http4k

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import org.http4k.core.Method

class RouteConfigTest {

  @Test
  fun `routeStack - route not in stack - enforced`() {
    val stack: List<WardenRoute> = listOf()
    val actual = evaluateRoute(stack, "/something", Method.entries.random())
    assertThat(actual).isEqualTo(WardenRouteBehaviour.ENFORCE)
  }

  @Test
  fun `routeStack - route matches - method does not`() {
    val stack: List<WardenRoute> =
        listOf(WardenRoute("/something", WardenRouteBehaviour.IGNORE, methods = setOf(Method.GET)))
    val actual = evaluateRoute(stack, "/something", Method.PUT)
    assertThat(actual).isEqualTo(WardenRouteBehaviour.ENFORCE)
  }

  @Test
  fun `routeStack - route matches - method matches`() {
    val stack: List<WardenRoute> =
        listOf(WardenRoute("/something", WardenRouteBehaviour.IGNORE, methods = setOf(Method.GET)))
    val actual = evaluateRoute(stack, "/something", Method.GET)
    assertThat(actual).isEqualTo(WardenRouteBehaviour.IGNORE)
  }

  @Test
  fun `routeStack - route regex prefix matches - method matches`() {
    val stack: List<WardenRoute> =
        listOf(WardenRoute("/some.*", WardenRouteBehaviour.IGNORE, methods = setOf(Method.GET)))
    val actual = evaluateRoute(stack, "/something", Method.GET)
    assertThat(actual).isEqualTo(WardenRouteBehaviour.IGNORE)
  }

  @Test
  fun `routeStack - route non regex prefix does not match - method matches`() {
    val stack: List<WardenRoute> =
        listOf(WardenRoute("/some", WardenRouteBehaviour.IGNORE, methods = setOf(Method.GET)))
    val actual = evaluateRoute(stack, "/something", Method.GET)
    assertThat(actual).isEqualTo(WardenRouteBehaviour.ENFORCE)
  }

  @Test
  fun `routeStack - higher priority overrides lower match`() {
    val stack: List<WardenRoute> =
        listOf(
            WardenRoute("/some/thing", WardenRouteBehaviour.ENFORCE, methods = setOf(Method.GET)),
            WardenRoute("/some", WardenRouteBehaviour.IGNORE, methods = setOf(Method.GET)))
    val actual = evaluateRoute(stack, "/some/thing", Method.GET)
    assertThat(actual).isEqualTo(WardenRouteBehaviour.ENFORCE)
  }
}
