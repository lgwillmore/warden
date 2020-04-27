package codes.laurence.warden.enforce

import codes.laurence.warden.AccessRequest


/**
 * Provides a hardened enforcement of the decision provided by a [codes.laurence.warden.decision.DecisionPoint].
 */
interface EnforcementPoint {

    /**
     * @throws NotAuthorizedException if [codes.laurence.warden.Access] is [codes.laurence.warden.Access.Denied]
     */
    suspend fun enforceAuthorization(request: AccessRequest)

}


data class NotAuthorizedException(val request: AccessRequest, val deniedProperties: Map<String, Any?>) :
    Exception("Not Authorized")
