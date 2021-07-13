package codes.laurence.warden.enforce

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.FilterAccessRequest

/**
 * Provides a hardened enforcement of the decision provided by a [codes.laurence.warden.decision.DecisionPoint].
 */
interface EnforcementPoint {

    /**
     * @throws NotAuthorizedException if [codes.laurence.warden.Access] is [codes.laurence.warden.Access.Denied]
     */
    suspend fun enforceAuthorization(request: AccessRequest)

    /**
     * Filters a collection of resources based on authorization.
     */
    suspend fun <RESOURCE> filterAuthorization(request: FilterAccessRequest<RESOURCE>): List<RESOURCE>
}

data class NotAuthorizedException(val request: AccessRequest, val deniedProperties: Map<String, Any?>) :
    Exception("Not Authorized")
