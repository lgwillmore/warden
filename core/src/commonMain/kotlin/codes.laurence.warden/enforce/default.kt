package codes.laurence.warden.enforce

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.decision.DecisionPointLocal
import codes.laurence.warden.policy.Policy

class EnforcementPointDefault(val decisionPoint: DecisionPoint) : EnforcementPoint {

    constructor(allow: List<Policy>, deny: List<Policy> = emptyList()) : this(DecisionPointLocal(allow, deny))

    override suspend fun enforceAuthorization(request: AccessRequest) {
        val response = decisionPoint.checkAuthorized(request)
        when (val access = response.access) {
            is Access.Denied -> {
                throw NotAuthorizedException(response.request, access.properties)
            }
        }
    }
}
