package codes.laurence.warden.policy

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

class AllOf(policies: MutableList<Policy>) : Policy, CollectionBasedPolicy(policies) {

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
                }
            }
            return AccessResponse(Access.Granted, accessRequest)
        }
    }

}

class AnyOf(policies: MutableList<Policy>) : Policy, CollectionBasedPolicy(policies) {

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
            }
        }
        return AccessResponse(
            access = Access.Denied(),
            request = accessRequest
        )
    }

}

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

open class CollectionBasedPolicy(val policies: MutableList<Policy>) {
    fun add(policy: Policy) {
        policies.add(policy)
    }
}