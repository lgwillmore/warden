package codes.laurence.warden.enforce

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.decision.DecisionPointInMemory
import codes.laurence.warden.policy.Policy

class EnforcementPointDefault(val decisionPoint: DecisionPoint) : EnforcementPoint {

    constructor(policies: List<Policy>):this(DecisionPointInMemory(policies))

    override suspend fun enforceAuthorization(request: AccessRequest) {
        val response = decisionPoint.checkAuthorized(request)
        when (val access = response.access) {
            is Access.Denied -> {
                throw NotAuthorizedException(response.request, access.properties)
            }
        }
    }
}