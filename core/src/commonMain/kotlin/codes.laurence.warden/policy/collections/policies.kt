package codes.laurence.warden.policy.collections

import codes.laurence.warden.policy.Policy

open class CollectionBasedPolicy(val policies: MutableList<Policy>) {
    fun add(policy: Policy) {
        policies.add(policy)
    }
}