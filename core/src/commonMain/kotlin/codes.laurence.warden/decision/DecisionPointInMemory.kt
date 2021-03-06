package codes.laurence.warden.decision

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.policy.AnyOf
import codes.laurence.warden.policy.Policy

/**
 * An in memory Decision point.
 *
 * Operates on a access list and deny list basis:
 *  - if there is not policy that grants access, there is no access.
 *  - if there is a policy that grants access, but the is also a policy that denies access, there is no access.
 *  - if there is a policy that grants access and no policy that denies, there is access.
 */
class DecisionPointInMemory(
    accessPolicies: List<Policy>,
    denyPolicies: List<Policy> = emptyList()
) : DecisionPoint {

    private val anyOfAccessContainer = AnyOf(accessPolicies)
    private val anyOfDenyContainer = AnyOf(denyPolicies)

    override suspend fun checkAuthorized(request: AccessRequest): AccessResponse {
        val accessResult = anyOfAccessContainer.checkAuthorized(request)
        return when(accessResult.access){
            is Access.Granted -> {
                val denyResult = anyOfDenyContainer.checkAuthorized(request)
                when(denyResult.access){
                    is Access.Granted -> accessResult.copy(
                        access = Access.Denied()
                    )
                    is Access.Denied -> accessResult
                }
            }
            is Access.Denied -> {
                accessResult
            }
        }
    }

}