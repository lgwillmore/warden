package codes.laurence.warden.policy.bool

import codes.laurence.warden.policy.Policy
import codes.laurence.warden.policy.collections.CollectionBasedPolicy

fun allOf(builder: CollectionBasedPolicy.() -> Unit) = AllOf(builder)

fun anyOf(builder: CollectionBasedPolicy.() -> Unit) = AnyOf(builder)

fun not(policy: Policy): Policy = Not(policy)
