package code.laurence.warden.ktor

import code.laurence.warden.ktor.Warden.Feature.CALL_ENFORCED_ATTRIBUTE_KEY
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.enforce.EnforcementPointDefault
import io.ktor.application.ApplicationCall

class EnforcementPointKtor(decisionPoint: DecisionPoint) {

    private val enforcementPoint = EnforcementPointDefault(decisionPoint)

    suspend fun enforceAuthorization(request: AccessRequest, call: ApplicationCall) {
        call.attributes.put(CALL_ENFORCED_ATTRIBUTE_KEY, true)
        enforcementPoint.enforceAuthorization(request)
    }
}