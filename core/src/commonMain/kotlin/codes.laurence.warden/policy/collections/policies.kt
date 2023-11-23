package codes.laurence.warden.policy.collections

import codes.laurence.warden.policy.Policy
import codes.laurence.warden.policy.PolicyDSL
import codes.laurence.warden.policy.bool.AllOf
import codes.laurence.warden.policy.bool.AnyOf
import codes.laurence.warden.policy.bool.Not
import codes.laurence.warden.policy.expression.*

@PolicyDSL
open class CollectionBasedPolicy(val policies: MutableList<Policy>) {
    fun add(policy: Policy) {
        policies.add(policy)
    }

    fun subject(pathRoot: String, vararg pathRest: String) =
        OperatorBuilder(
            AttributeReference(AttributeType.SUBJECT, listOf(pathRoot) + pathRest.toList()),
            addToCollectionHandler(this)
        )

    fun action(pathRoot: String, vararg pathRest: String) =
        OperatorBuilder(
            AttributeReference(AttributeType.ACTION, listOf(pathRoot) + pathRest.toList()),
            addToCollectionHandler(this)
        )

    fun resource(pathRoot: String, vararg pathRest: String) =
        OperatorBuilder(
            AttributeReference(AttributeType.RESOURCE, listOf(pathRoot) + pathRest.toList()),
            addToCollectionHandler(this)
        )

    fun environment(pathRoot: String, vararg pathRest: String) =
        OperatorBuilder(
            AttributeReference(AttributeType.ENVIRONMENT, listOf(pathRoot) + pathRest.toList()),
            addToCollectionHandler(this)
        )

    fun anyOf(builder: CollectionBasedPolicy.() -> Unit) {
        val anyOf = AnyOf()
        anyOf.builder()
        this.add(anyOf)
    }

    fun allOf(builder: CollectionBasedPolicy.() -> Unit) {
        val allOf = AllOf()
        allOf.builder()
        this.add(allOf)
    }

    fun notAllOf(builder: CollectionBasedPolicy.() -> Unit) {
        val allOf = AllOf()
        allOf.builder()
        this.add(Not(allOf))
    }

    fun notAnyOf(builder: CollectionBasedPolicy.() -> Unit) {
        val anyOf = AnyOf()
        anyOf.builder()
        this.add(Not(anyOf))
    }
}

private fun addToCollectionHandler(collection: CollectionBasedPolicy): PolicyBuiltHandler {
    return { policy ->
        collection.add(policy)
    }
}

fun List<String>.toPathSegments(): List<AttributePathSegment> = map { AttributePathSegment.AString(it) }
