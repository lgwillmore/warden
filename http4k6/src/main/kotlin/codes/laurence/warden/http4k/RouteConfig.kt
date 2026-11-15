package codes.laurence.warden.http4k

import org.http4k.core.Method

enum class WardenRouteBehaviour {
  IGNORE,
  ENFORCE
}

/**
 * One entry of a [WardenFilter] route priority stack. [route] is a regular expression that must
 * match the whole request path, and the entry applies only to requests with one of [methods].
 */
data class WardenRoute(
    val route: String,
    val behaviour: WardenRouteBehaviour,
    val methods: Set<Method> = Method.entries.toSet(),
) {
  internal val reg = route.toRegex()
}

internal fun evaluateRoute(
    stack: List<WardenRoute>,
    path: String,
    method: Method,
): WardenRouteBehaviour {
  for (wardenRoute in stack) {
    if (wardenRoute.reg.matches(path) && wardenRoute.methods.contains(method)) {
      return wardenRoute.behaviour
    }
  }
  return WardenRouteBehaviour.ENFORCE
}
