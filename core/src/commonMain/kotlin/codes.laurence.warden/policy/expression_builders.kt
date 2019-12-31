package codes.laurence.warden.policy

fun subjectVal(key: String) = AttributeReference(AttributeType.SUBJECT, key)
fun environmentVal(key: String) = AttributeReference(AttributeType.ENVIRONMENT, key)
fun resourceVal(key: String) = AttributeReference(AttributeType.RESOURCE, key)
fun rawVal(value: Any?) = PassThroughReference(value)

/**
 * Initializing token for an ExpressionPolicy builder.
 */
class Exp {
    companion object {
        fun subject(key: String) = Operator(AttributeReference(AttributeType.SUBJECT, key))

        fun action(key: String) = Operator(AttributeReference(AttributeType.ACTION, key))

        fun resource(key: String) = Operator(AttributeReference(AttributeType.RESOURCE, key))

        fun environment(key: String) = Operator(AttributeReference(AttributeType.ENVIRONMENT, key))
    }
}

class Operator internal constructor(
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

    private fun secondOperand(operatorType: OperatorType, secondOperand: ValueReference): ExpressionPolicy {
        return ExpressionPolicy(
            leftOperand = leftValueReference,
            operatorType = operatorType,
            rightOperand = secondOperand
        )
    }
}