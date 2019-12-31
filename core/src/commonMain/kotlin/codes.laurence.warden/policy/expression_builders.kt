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

    infix fun equalTo(value: Any?) = secondOperand(OperatorType.EQUAL, value)

    infix fun greaterThan(value: Any?) = secondOperand(OperatorType.GREATER_THAN, value)

    infix fun greaterThanEqual(value: Any?) = secondOperand(OperatorType.GREATER_THAN_EQUAL, value)

    infix fun lessThan(value: Any?) = secondOperand(OperatorType.LESS_THAN, value)

    infix fun lessThanEqual(value: Any?) = secondOperand(OperatorType.LESS_THAN_EQUAL, value)

    infix fun contains(value: Any?) = secondOperand(OperatorType.CONTAINS, value)

    infix fun containsAll(value: Any?) = secondOperand(OperatorType.CONTAINS_ALL, value)

    infix fun containsAny(value: Any?) = secondOperand(OperatorType.CONTAINS_ANY, value)

    infix fun isIn(value: Any?) = secondOperand(OperatorType.IS_IN, value)

    private fun secondOperand(operatorType: OperatorType, secondOperand: Any?): ExpressionPolicy {
        val operandReference = when(secondOperand){
            is ValueReference -> secondOperand
            else -> PassThroughReference(secondOperand)
        }
        return ExpressionPolicy(
            leftOperand = leftValueReference,
            operatorType = operatorType,
            rightOperand = operandReference
        )
    }
}