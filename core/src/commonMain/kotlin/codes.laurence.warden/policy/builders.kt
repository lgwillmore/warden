package codes.laurence.warden.policy

fun allOf(builder: CollectionBasedPolicy.() -> Unit) = AllOf(builder)
fun anyOf(builder: CollectionBasedPolicy.() -> Unit) = AnyOf(builder)
fun not(policy: Policy): Policy = Not(policy)

private fun addToCollectionHandler(collection:CollectionBasedPolicy):PolicyBuiltHandler {
    return { policy ->
        collection.add(policy)
    }
}

fun CollectionBasedPolicy.subject(key: String) =
    OperatorBuilder(AttributeReference(AttributeType.SUBJECT, key), addToCollectionHandler(this))

fun CollectionBasedPolicy.action(key: String) =
    OperatorBuilder(AttributeReference(AttributeType.ACTION, key), addToCollectionHandler(this))

fun CollectionBasedPolicy.resource(key: String) =
    OperatorBuilder(AttributeReference(AttributeType.RESOURCE, key), addToCollectionHandler(this))

fun CollectionBasedPolicy.environment(key: String) =
    OperatorBuilder(AttributeReference(AttributeType.ENVIRONMENT, key), addToCollectionHandler(this))

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

fun subjectVal(key: String) = AttributeReference(AttributeType.SUBJECT, key)
fun environmentVal(key: String) = AttributeReference(AttributeType.ENVIRONMENT, key)
fun resourceVal(key: String) = AttributeReference(AttributeType.RESOURCE, key)

/**
 * Initializing token for an ExpressionPolicy builder.
 */
class Exp {
    companion object {
        fun subject(key: String) = OperatorBuilder(AttributeReference(AttributeType.SUBJECT, key))

        fun action(key: String) = OperatorBuilder(AttributeReference(AttributeType.ACTION, key))

        fun resource(key: String) = OperatorBuilder(AttributeReference(AttributeType.RESOURCE, key))

        fun environment(key: String) = OperatorBuilder(AttributeReference(AttributeType.ENVIRONMENT, key))
    }
}

typealias PolicyBuiltHandler = (Policy) -> Unit

class OperatorBuilder(
    private val leftValueReference: ValueReference,
    private val policyBuiltHandler: PolicyBuiltHandler? = null
){
    infix fun equalTo(value: Any?) = secondOperand(OperatorType.EQUAL, value)

    infix fun greaterThan(value: Comparable<*>) = secondOperand(OperatorType.GREATER_THAN, value)
    infix fun greaterThan(value: AttributeReference) = secondOperand(OperatorType.GREATER_THAN, value)

    infix fun greaterThanEqual(value: Comparable<*>) = secondOperand(OperatorType.GREATER_THAN_EQUAL, value)
    infix fun greaterThanEqual(value: AttributeReference) = secondOperand(OperatorType.GREATER_THAN_EQUAL, value)

    infix fun lessThan(value: Any?) = secondOperand(OperatorType.LESS_THAN, value)

    infix fun lessThanEqual(value: Any?) = secondOperand(OperatorType.LESS_THAN_EQUAL, value)

    infix fun contains(value: Any?) = secondOperand(OperatorType.CONTAINS, value)

    infix fun containsAll(value: Any?) = secondOperand(OperatorType.CONTAINS_ALL, value)

    infix fun containsAny(value: Any?) = secondOperand(OperatorType.CONTAINS_ANY, value)

    infix fun isIn(value: Any?) = secondOperand(OperatorType.IS_IN, value)

    private fun secondOperand(operatorType: OperatorType, secondOperand: Any?): ExpressionPolicy {
        val operandReference = when (secondOperand) {
            is ValueReference -> secondOperand
            else -> PassThroughReference(secondOperand)
        }
        val policy = ExpressionPolicy(
            leftOperand = leftValueReference,
            operatorType = operatorType,
            rightOperand = operandReference
        )
        policyBuiltHandler?.invoke(policy)
        return policy
    }
}