package codes.laurence.warden.policy

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

interface Policy {
    fun checkAuthorized(accessRequest: AccessRequest): AccessResponse
}