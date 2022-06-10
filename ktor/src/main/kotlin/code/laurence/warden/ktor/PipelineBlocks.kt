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
suspend fun PipelineContext<Unit, ApplicationCall>.warded(
    bodyOfCall: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    val pipeline = this
    val wardenContext = enter(this.call)
    withContext(wardenContext) {
        pipeline.bodyOfCall()
    }
    exit(wardenContext)
}

/**
 * Allow this call to respond without enforcing authorization.
 */
suspend fun PipelineContext<Unit, ApplicationCall>.unwarded(
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
    val wardedRoute = this.createChild(object : RouteSelector(1.0) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercepts calls from this route at the features step
    wardedRoute.intercept(ApplicationCallPipeline.Call) {
        val wardenContext = enter(this.call)
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
    val unWardedRoute = this.createChild(object : RouteSelector(1.0) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercepts calls from this route at the features step
    unWardedRoute.intercept(ApplicationCallPipeline.Call) {
        this.call.attributes.put(Warden.WARDEN_IGNORED, true)
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
    val beforeEndpointRoute = this.createChild(object : RouteSelector(1.0) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercepts calls from this route at the features step
    beforeEndpointRoute.intercept(ApplicationCallPipeline.Call) {
        this.before()
        proceed()
    }

    // Configure this route with the block provided by the user
    callback(beforeEndpointRoute)

    return beforeEndpointRoute
}

internal suspend fun enter(call: ApplicationCall): CoroutineContext {
    var context = coroutineContext
    val wardenCall = WardenKtorCall(call)
    context = context.plus(wardenCall)
    return context
}

internal fun exit(context: CoroutineContext) {
    val wardenCall = context[WardenKtorCall.Key]
        ?: throw Exception("This should never be called before ensuring wardenCall present")
    wardenCall.call = null
}

internal class WardenKtorCall(var call: ApplicationCall?) : AbstractCoroutineContextElement(WardenKtorCall) {
    /**
     * Key for [CoroutineActorStateStack] instance in the coroutine context.
     */
    companion object Key :
        CoroutineContext.Key<WardenKtorCall>
}
