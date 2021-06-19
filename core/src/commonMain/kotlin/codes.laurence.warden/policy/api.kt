package codes.laurence.warden.policy

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

/**
 * A [Policy] encapsulates the authorization logic for a given Authorization rule.
 */
interface Policy {
    fun checkAuthorized(accessRequest: AccessRequest): AccessResponse
}

/**
 * A Source of [Policies]
 */
interface PolicySource {
    /**
     * Fetches policies and allows for matching on a given [AccessRequest].
     */
    suspend fun policies(request: AccessRequest? = null): Policies
}

data class Policies(
    val allow: List<Policy>,
    val deny: List<Policy>,
)
