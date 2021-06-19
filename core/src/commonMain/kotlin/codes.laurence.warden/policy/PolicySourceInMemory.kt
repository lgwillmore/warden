package codes.laurence.warden.policy

import codes.laurence.warden.AccessRequest

/**
 * A simple in memory [PolicySource] with no [codes.laurence.warden.AccessRequest] matching.
 */
class PolicySourceInMemory(
    allow: List<Policy>,
    deny: List<Policy> = emptyList(),
) : PolicySource {

    private val policies = Policies(
        allow = allow,
        deny = deny
    )

    override suspend fun policies(request: AccessRequest?): Policies {
        return policies
    }
}