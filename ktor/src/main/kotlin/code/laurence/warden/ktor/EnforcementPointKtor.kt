package code.laurence.warden.ktor

import code.laurence.warden.ktor.Warden.Feature.WARDEN_ENFORCED
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.enforce.EnforcementPoint
import codes.laurence.warden.enforce.EnforcementPointDefault
import io.ktor.application.ApplicationCall

/**
 * An [EnforcementPoint] that works with the Ktor [Warden] feature.
 *
 * This means that all routes must have an [enforceAuthorization] call or be explicitly ignored.
 */
class EnforcementPointKtor(decisionPoint: DecisionPoint) {

    private val enforcementPoint: EnforcementPoint = EnforcementPointDefault(decisionPoint)

    suspend fun enforceAuthorization(request: AccessRequest, call: ApplicationCall) {
        call.attributes.put(WARDEN_ENFORCED, true)
        enforcementPoint.enforceAuthorization(request)
    }
}