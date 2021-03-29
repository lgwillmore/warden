package codes.laurence.warden.policy.members

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.policy.Policy
import codes.laurence.warden.policy.expression.ValueReference

class MemberPolicy(
    val collectionSource: ValueReference,

) : Policy {

    override fun checkAuthorized(accessRequest: AccessRequest): AccessResponse {
        TODO("Not yet implemented")
    }
}

