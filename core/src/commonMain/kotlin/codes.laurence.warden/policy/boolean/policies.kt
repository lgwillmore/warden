package codes.laurence.warden.policy.boolean

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.policy.Policy
import codes.laurence.warden.policy.PolicyDSL
import codes.laurence.warden.policy.collections.CollectionBasedPolicy

/**
 * All of the policies must grant access, and at least 1 must grant access.
 */
@PolicyDSL
class AllOf(policies: List<Policy>) : Policy, CollectionBasedPolicy(policies.toMutableList()) {

    constructor(builder: CollectionBasedPolicy.() -> Unit) : this(mutableListOf()) {
        this.builder()
    }

    constructor(vararg policies: Policy) : this(policies.toMutableList())

    override fun checkAuthorized(accessRequest: AccessRequest): AccessResponse {
        if (policies.isEmpty()) {
            return AccessResponse(Access.Denied(), accessRequest)
        } else {
            policies.forEach {
                val response = it.checkAuthorized(accessRequest)
                when (response.access) {
                    is Access.Denied -> {
                        return response
                    }
                    else -> {
                    }
                }
            }
            return AccessResponse(Access.Granted, accessRequest)
        }
    }
}

/**
 * At least 1 policy must grant access.
 */
class AnyOf(policies: List<Policy>) : Policy, CollectionBasedPolicy(policies.toMutableList()) {

    constructor(builder: CollectionBasedPolicy.() -> Unit) : this(mutableListOf()) {
        this.builder()
    }

    constructor(vararg policies: Policy) : this(policies.toMutableList())

    override fun checkAuthorized(accessRequest: AccessRequest): AccessResponse {
        policies.forEach {
            val response = it.checkAuthorized(accessRequest)
            when (response.access) {
                is Access.Granted -> {
                    return response
                }
                else -> {
                }
            }
        }
        return AccessResponse(
            access = Access.Denied(),
            request = accessRequest
        )
    }

}

/**
 * Will grant access if the policy does not grant access.
 */
class Not(val policy: Policy) : Policy {

    override fun checkAuthorized(accessRequest: AccessRequest): AccessResponse {
        val internal = policy.checkAuthorized(accessRequest)
        return when (internal.access) {
            is Access.Granted -> {
                internal.copy(access = Access.Denied())
            }
            is Access.Denied -> {
                internal.copy(access = Access.Granted)
            }
        }
    }
}

