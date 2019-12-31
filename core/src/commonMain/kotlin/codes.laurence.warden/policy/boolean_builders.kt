package codes.laurence.warden.policy

fun allOf(builder: CollectionBasedPolicy.() -> Unit) = AllOf(builder)
fun anyOf(builder: CollectionBasedPolicy.() -> Unit) = AnyOf(builder)
fun not(policy: Policy): Policy = Not(policy)

fun CollectionBasedPolicy.subject(key: String) =
    PolicyCollectionOperator(this, AttributeReference(AttributeType.SUBJECT, key))

fun CollectionBasedPolicy.action(key: String) =
    PolicyCollectionOperator(this, AttributeReference(AttributeType.ACTION, key))

fun CollectionBasedPolicy.resource(key: String) =
    PolicyCollectionOperator(this, AttributeReference(AttributeType.RESOURCE, key))

fun CollectionBasedPolicy.environment(key: String) =
    PolicyCollectionOperator(this, AttributeReference(AttributeType.ENVIRONMENT, key))

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

class PolicyCollectionOperator internal constructor(
    private val collectionBasedPolicy: CollectionBasedPolicy,
    private val leftValueReference: ValueReference
) {

    infix fun equalTo(value: ValueReference) = secondOperand(OperatorType.EQUAL, value)

    infix fun greaterThan(value: ValueReference) = secondOperand(OperatorType.GREATER_THAN, value)

    infix fun greaterThanEqual(value: ValueReference) = secondOperand(OperatorType.GREATER_THAN_EQUAL, value)

    infix fun lessThan(value: ValueReference) = secondOperand(OperatorType.LESS_THAN, value)

    infix fun lessThanEqual(value: ValueReference) = secondOperand(OperatorType.LESS_THAN_EQUAL, value)

    infix fun contains(value: ValueReference) = secondOperand(OperatorType.CONTAINS, value)

    infix fun containsAll(value: ValueReference) = secondOperand(OperatorType.CONTAINS_ALL, value)

    infix fun containsAny(value: ValueReference) = secondOperand(OperatorType.CONTAINS_ANY, value)

    infix fun isIn(value: ValueReference) = secondOperand(OperatorType.IS_IN, value)

    private fun secondOperand(operatorType: OperatorType, secondOperand: ValueReference) {
        val policy = ExpressionPolicy(
            leftOperand = leftValueReference,
            operatorType = operatorType,
            rightOperand = secondOperand
        )
        collectionBasedPolicy.add(policy)
    }
}