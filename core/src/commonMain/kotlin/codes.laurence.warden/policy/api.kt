package codes.laurence.warden.policy

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

/**
 * A Policy encapsulates the authorization logic for a given Authorization rule.
 */
interface Policy {
    fun checkAuthorized(accessRequest: AccessRequest): AccessResponse
}