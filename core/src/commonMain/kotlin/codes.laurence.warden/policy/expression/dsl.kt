package codes.laurence.warden.policy.expression

import codes.laurence.warden.policy.Policy
import codes.laurence.warden.policy.members.MemberBuilder
import codes.laurence.warden.policy.members.MemberPolicy

fun subjectVal(pathRoot: String, vararg pathRest: String) =
    AttributeReference(AttributeType.SUBJECT, listOf(pathRoot) + pathRest.toList())

fun environmentVal(pathRoot: String, vararg pathRest: String) =
    AttributeReference(AttributeType.ENVIRONMENT, listOf(pathRoot) + pathRest.toList())

fun resourceVal(pathRoot: String, vararg pathRest: String) =
    AttributeReference(AttributeType.RESOURCE, listOf(pathRoot) + pathRest.toList())

fun actionVal(pathRoot: String, vararg pathRest: String) =
    AttributeReference(AttributeType.ACTION, listOf(pathRoot) + pathRest.toList())

/**
 * Initializing token for an ExpressionPolicy builder.
 */
class Exp {
    companion object {
        fun subject(pathRoot: String, vararg pathRest: String) =
            OperatorBuilder(AttributeReference(AttributeType.SUBJECT, listOf(pathRoot) + pathRest.toList()))

        fun action(pathRoot: String, vararg pathRest: String) =
            OperatorBuilder(AttributeReference(AttributeType.ACTION, listOf(pathRoot) + pathRest.toList()))

        fun resource(pathRoot: String, vararg pathRest: String) =
            OperatorBuilder(AttributeReference(AttributeType.RESOURCE, listOf(pathRoot) + pathRest.toList()))

        fun environment(pathRoot: String, vararg pathRest: String) =
            OperatorBuilder(AttributeReference(AttributeType.ENVIRONMENT, listOf(pathRoot) + pathRest.toList()))
    }
}

typealias PolicyBuiltHandler = (Policy) -> Unit

open class OperatorBuilderBase(
    private val leftValueReference: ValueReference,
    private val policyBuiltHandler: PolicyBuiltHandler? = null
) {
    infix fun equalTo(value: Any?) = secondOperand(OperatorType.EQUAL, value)

    infix fun greaterThan(value: Comparable<*>) = secondOperand(OperatorType.GREATER_THAN, value)
    infix fun greaterThan(value: AttributeReference) = secondOperand(OperatorType.GREATER_THAN, value)

    infix fun greaterThanEqual(value: Comparable<*>) = secondOperand(OperatorType.GREATER_THAN_EQUAL, value)
    infix fun greaterThanEqual(value: AttributeReference) = secondOperand(OperatorType.GREATER_THAN_EQUAL, value)

    infix fun lessThan(value: Comparable<*>) = secondOperand(OperatorType.LESS_THAN, value)
    infix fun lessThan(value: AttributeReference) = secondOperand(OperatorType.LESS_THAN, value)

    infix fun lessThanEqual(value: Comparable<*>) = secondOperand(OperatorType.LESS_THAN_EQUAL, value)
    infix fun lessThanEqual(value: AttributeReference) = secondOperand(OperatorType.LESS_THAN_EQUAL, value)

    infix fun contains(value: Any?) = secondOperand(OperatorType.CONTAINS, value)

    infix fun containsAll(value: Collection<*>) = secondOperand(OperatorType.CONTAINS_ALL, value)
    infix fun containsAll(value: AttributeReference) = secondOperand(OperatorType.CONTAINS_ALL, value)

    infix fun containsAny(value: Collection<*>) = secondOperand(OperatorType.CONTAINS_ANY, value)
    infix fun containsAny(value: AttributeReference) = secondOperand(OperatorType.CONTAINS_ANY, value)

    infix fun isIn(value: Collection<*>) = secondOperand(OperatorType.IS_IN, value)
    infix fun isIn(value: AttributeReference) = secondOperand(OperatorType.IS_IN, value)

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


class OperatorBuilder(
    leftValueReference: ValueReference,
    policyBuiltHandler: PolicyBuiltHandler? = null
) : OperatorBuilderBase(leftValueReference, policyBuiltHandler) {
    infix fun forAnyMember(builder: MemberBuilder.() -> Unit): MemberPolicy {
        TODO()
    }

    infix fun forAllMembers(builder: MemberBuilder.() -> Unit): MemberPolicy {
        TODO()
    }
}