package codes.laurence.warden.policy

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

interface Policy {
    fun checkAuthorization(request: AccessRequest): AccessResponse
}