package code.laurence.warden.ktor

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Provides context for any nested calls to a [EnforcementPointKtor] to be registered and allow the call to return successfully if access is granted.
 */
@JvmName("wardedCallGeneric")
suspend fun PipelineContext<Unit, Any>.wardedCall(
    bodyOfCall: suspend PipelineContext<Unit, Any>.() -> Unit
) {
    val wardenContext = enter(this.context as ApplicationCall)
    withContext(wardenContext) {
        bodyOfCall()
    }
    exit(wardenContext)
}

suspend fun PipelineContext<Unit, ApplicationCall>.wardedCall(
    bodyOfCall: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    val wardenContext = enter(this.call)
    withContext(wardenContext) {
        bodyOfCall()
    }
    exit(wardenContext)
}

/**
 * Allow this call to respond without enforcing authorization.
 */
@JvmName("unwardedCallGeneric")
suspend fun PipelineContext<Unit, Any>.unwardedCall(
    bodyOfCall: suspend PipelineContext<Unit, Any>.() -> Unit
) {
    (this.context as ApplicationCall).attributes.put(Warden.WARDEN_IGNORED, true)
    this.bodyOfCall()
}

suspend fun PipelineContext<Unit, ApplicationCall>.unwardedCall(
    bodyOfCall: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    call.attributes.put(Warden.WARDEN_IGNORED, true)
    this.bodyOfCall()
}

/**
 * Provides context for any nested calls to a [EnforcementPointKtor] to be registered and allow the call to return successfully if access is granted.
 */
fun Route.warded(
    callback: Route.() -> Unit
): Route {
    // With createChild, we create a child node for this received Route
    val wardedRoute = this.createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercepts calls from this route at the features step
    wardedRoute.intercept(ApplicationCallPipeline.Call) { _ ->
        val wardenContext = enter(this.context as ApplicationCall)
        withContext(wardenContext) {
            proceed()
        }
        exit(wardenContext)
    }

    // Configure this route with the block provided by the user
    callback(wardedRoute)

    return wardedRoute
}

/**
 * Allow any calls nested within this block to respond without enforcing authorization.
 */
fun Route.unwarded(
    callback: Route.() -> Unit
): Route {
    // With createChild, we create a child node for this received Route
    val unWardedRoute = this.createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercepts calls from this route at the features step
    unWardedRoute.intercept(ApplicationCallPipeline.Call) { _ ->
        (this.context as ApplicationCall).attributes.put(Warden.WARDEN_IGNORED, true)
        proceed()
    }

    // Configure this route with the block provided by the user
    callback(unWardedRoute)

    return unWardedRoute
}

/**
 * Allows logic to be executed before any nested route endpoint handlers.
 *
 * This is intended for use with the WebSocket feature where websockets do not allow an opportunity to call an [codes.laurence.warden.enforce.EnforcementPoint] before the websocket connects.
 */
fun Route.beforeEach(
    before: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit,
    callback: Route.() -> Unit
): Route {
    // With createChild, we create a child node for this received Route
    val beforeEndpointRoute = this.createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercepts calls from this route at the features step
    beforeEndpointRoute.intercept(ApplicationCallPipeline.Call) { _ ->
        (this as PipelineContext<Unit, ApplicationCall>).before()
        proceed()
    }

    // Configure this route with the block provided by the user
    callback(beforeEndpointRoute)

    return beforeEndpointRoute
}

internal class WardenCoroutineContext(val call: ApplicationCall) : AbstractCoroutineContextElement(WardenCoroutineContext) {
    companion object Key : CoroutineContext.Key<WardenCoroutineContext>
}

internal suspend fun enter(call: ApplicationCall): CoroutineContext {
    return coroutineContext + WardenCoroutineContext(call)
}

internal fun exit(context: CoroutineContext) {
    // No-op
}
