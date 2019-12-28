package codes.laurence.warden.enforce

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

interface EnforcementPoint {

    suspend fun enforceAuthorization(request: AccessRequest)

}


data class NotAuthorizedException(val accessResponse: AccessResponse) : Exception("Not Authorized")
