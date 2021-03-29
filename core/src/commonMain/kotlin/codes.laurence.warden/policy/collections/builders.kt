package codes.laurence.warden.policy.collections

import codes.laurence.warden.policy.boolean.AllOf
import codes.laurence.warden.policy.boolean.AnyOf
import codes.laurence.warden.policy.boolean.Not
import codes.laurence.warden.policy.expression.AttributeReference
import codes.laurence.warden.policy.expression.AttributeType
import codes.laurence.warden.policy.expression.OperatorBuilder
import codes.laurence.warden.policy.expression.PolicyBuiltHandler


private fun addToCollectionHandler(collection: CollectionBasedPolicy): PolicyBuiltHandler {
    return { policy ->
        collection.add(policy)
    }
}

fun CollectionBasedPolicy.subject(pathRoot: String, vararg pathRest: String) =
    OperatorBuilder(
        AttributeReference(AttributeType.SUBJECT, listOf(pathRoot) + pathRest.toList()),
        addToCollectionHandler(this)
    )

fun CollectionBasedPolicy.action(pathRoot: String, vararg pathRest: String) =
    OperatorBuilder(
        AttributeReference(AttributeType.ACTION, listOf(pathRoot) + pathRest.toList()),
        addToCollectionHandler(this)
    )

fun CollectionBasedPolicy.resource(pathRoot: String, vararg pathRest: String) =
    OperatorBuilder(
        AttributeReference(AttributeType.RESOURCE, listOf(pathRoot) + pathRest.toList()),
        addToCollectionHandler(this)
    )

fun CollectionBasedPolicy.environment(pathRoot: String, vararg pathRest: String) =
    OperatorBuilder(
        AttributeReference(AttributeType.ENVIRONMENT, listOf(pathRoot) + pathRest.toList()),
        addToCollectionHandler(this)
    )

fun CollectionBasedPolicy.anyOf(builder: CollectionBasedPolicy.() -> Unit) {
    val anyOf = AnyOf()
    anyOf.builder()
    this.add(anyOf)
}

fun CollectionBasedPolicy.allOf(builder: CollectionBasedPolicy.() -> Unit) {
    val allOf = AllOf()
    allOf.builder()
    this.add(allOf)
}

fun CollectionBasedPolicy.notAllOf(builder: CollectionBasedPolicy.() -> Unit) {
    val allOf = AllOf()
    allOf.builder()
    this.add(Not(allOf))
}

fun CollectionBasedPolicy.notAnyOf(builder: CollectionBasedPolicy.() -> Unit) {
    val anyOf = AnyOf()
    anyOf.builder()
    this.add(Not(anyOf))
}

