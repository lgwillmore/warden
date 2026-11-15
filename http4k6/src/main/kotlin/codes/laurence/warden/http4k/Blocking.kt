package codes.laurence.warden.http4k

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessRequestBatch
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.FilterAccessRequest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.enforce.EnforcementPoint
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.runBlocking

/**
 * Runs a suspending [block] from an http4k handler and returns its result, carrying the current
 * request into the coroutine so that an [EnforcementPointHttp4k] reached on any dispatcher can mark
 * it as enforced. A bare `runBlocking` works too, but only while the coroutine stays on the request
 * thread.
 */
fun <T> warded(block: suspend () -> T): T {
  val call = currentWardenCall.get()
  val context = if (call == null) EmptyCoroutineContext else WardenCoroutineContext(call)
  return runBlocking(context) { block() }
}

/** [EnforcementPoint.enforceAuthorization] for code that does not suspend. */
fun EnforcementPoint.enforceAuthorizationBlocking(request: AccessRequest) = warded {
  enforceAuthorization(request)
}

/** [EnforcementPoint.filterAuthorization] for code that does not suspend. */
fun <RESOURCE> EnforcementPoint.filterAuthorizationBlocking(
    request: FilterAccessRequest<RESOURCE>
): List<RESOURCE> = warded { filterAuthorization(request) }

/** [DecisionPoint.checkAuthorized] for code that does not suspend. */
fun DecisionPoint.checkAuthorizedBlocking(request: AccessRequest): AccessResponse = warded {
  checkAuthorized(request)
}

/** [DecisionPoint.checkAuthorizedBatch] for code that does not suspend. */
fun DecisionPoint.checkAuthorizedBatchBlocking(request: AccessRequestBatch): List<AccessResponse> =
    warded {
      checkAuthorizedBatch(request)
    }
