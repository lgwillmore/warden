package code.laurence.warden.ktor

import io.ktor.http.*

enum class WardenRouteBehaviour {
    IGNORE,
    ENFORCE
}

data class WardenRoute(
    val route: String,
    val behaviour: WardenRouteBehaviour,
    val methods: Set<HttpMethod> = HttpMethod.DefaultMethods.toSet()
) {
    internal val reg = route.toRegex()
}

internal fun evaluateRoute(stack: List<WardenRoute>, route: String, method: HttpMethod): WardenRouteBehaviour {
    for (wardenRoute in stack) {
        if (wardenRoute.reg.matches(route) && wardenRoute.methods.contains(method)) {
            return wardenRoute.behaviour
        }
    }
    return WardenRouteBehaviour.ENFORCE
}
