package codes.laurence.warden.enforce

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

interface EnforcementPoint {

    /**
     * @throws NotAuthorizedException if [codes.laurence.warden.Access] is [codes.laurence.warden.Access.Denied]
     */
    suspend fun enforceAuthorization(request: AccessRequest)

}


data class NotAuthorizedException(val accessResponse: AccessResponse) : Exception("Not Authorized")
