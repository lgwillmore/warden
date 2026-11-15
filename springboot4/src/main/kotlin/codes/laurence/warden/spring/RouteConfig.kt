package codes.laurence.warden.spring

import org.springframework.http.HttpMethod

enum class WardenRouteBehaviour {
  IGNORE,
  ENFORCE
}

/**
 * One entry of a route priority stack. [route] is a regular expression that must match the whole
 * request path, and the entry applies only to requests with one of [methods].
 */
data class WardenRoute(
    val route: String,
    val behaviour: WardenRouteBehaviour,
    val methods: Set<HttpMethod> = HttpMethod.values().toSet(),
) {
  internal val reg = route.toRegex()
}

/**
 * Walks [stack] top down and returns the behaviour of the first entry matching [path] and [method].
 * A request that matches nothing is enforced.
 */
fun evaluateRoute(
    stack: List<WardenRoute>,
    path: String,
    method: HttpMethod,
): WardenRouteBehaviour {
  for (wardenRoute in stack) {
    if (wardenRoute.reg.matches(path) && wardenRoute.methods.contains(method)) {
      return wardenRoute.behaviour
    }
  }
  return WardenRouteBehaviour.ENFORCE
}
