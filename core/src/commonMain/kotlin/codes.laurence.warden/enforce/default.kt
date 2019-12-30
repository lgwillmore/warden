package codes.laurence.warden.enforce

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.decision.DecisionPoint

class EnforcementPointDefault(private val decisionPoint: DecisionPoint): EnforcementPoint {

    override suspend fun enforceAuthorization(request: AccessRequest) {
        val response = decisionPoint.checkAuthorized(request)
        when(val access = response.access){
            is Access.Denied -> {
                throw NotAuthorizedException(response)
            }
        }
    }
}