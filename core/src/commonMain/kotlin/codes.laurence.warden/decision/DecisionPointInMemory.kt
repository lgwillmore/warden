package codes.laurence.warden.decision

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.policy.AllOf
import codes.laurence.warden.policy.Policy

/**
 * An in memory Decision point.
 *
 * Operates on a whitelist basis - if there is not policy that grants access, there is no access.
 */
class DecisionPointInMemory(policies: List<Policy>) : DecisionPoint {

    private val allOfContainer = AllOf(policies)

    override suspend fun checkAuthorized(request: AccessRequest): AccessResponse {
        return allOfContainer.checkAuthorized(request)
    }

}