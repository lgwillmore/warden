package code.laurence.warden.ktor

import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

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

suspend fun PipelineContext<Unit, ApplicationCall>.unwarded(
    bodyOfCall: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    call.attributes.put(Warden.WARDEN_IGNORED, true)
    this.bodyOfCall()
}

fun Route.warded(
    callback: Route.() -> Unit
): Route {
    // With createChild, we create a child node for this received Route
    val wardedRoute = this.createChild(object : RouteSelector(1.0) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercepts calls from this route at the features step
    wardedRoute.intercept(ApplicationCallPipeline.Features) {
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

fun Route.unwarded(
    callback: Route.() -> Unit
): Route {
    // With createChild, we create a child node for this received Route
    val unWardedRoute = this.createChild(object : RouteSelector(1.0) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercepts calls from this route at the features step
    unWardedRoute.intercept(ApplicationCallPipeline.Features) {
        this.call.attributes.put(Warden.WARDEN_IGNORED, true)
        proceed()
    }

    // Configure this route with the block provided by the user
    callback(unWardedRoute)

    return unWardedRoute
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