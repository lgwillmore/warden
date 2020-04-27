package codes.laurence.warden.policy

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

/**
 * A policy for building basic 2 operand expressions.
 *
 * Conceptual Examples:
 *
 *      1) Subjects "id" attribute is equal to Resources "owner_id" attribute.
 *      2) Environments "ip" attribute is equal to 102.132.128.37
 *      3) Subjects "age" attribute is greater than 18
 *      4) Subjects "roles" attribute contains any of ["admin", "supervisor"]
 */
class ExpressionPolicy(
    val leftOperand: ValueReference,
    val operatorType: OperatorType,
    val rightOperand: ValueReference
) : Policy {

    override fun checkAuthorized(accessRequest: AccessRequest): AccessResponse {
        try {
            val left = leftOperand.get(accessRequest)
            val right = rightOperand.get(accessRequest)
            val granted = when (operatorType) {
                OperatorType.EQUAL -> left == right
                OperatorType.GREATER_THAN,
                OperatorType.GREATER_THAN_EQUAL,
                OperatorType.LESS_THAN,
                OperatorType.LESS_THAN_EQUAL -> checkComparable(left, right, operatorType)
                OperatorType.IS_IN -> {
                    @Suppress("UNCHECKED_CAST")
                    try {
                        right as Collection<Any>
                    } catch (e: Exception) {
                        throw BadExpressionException("Value of $rightOperand is not Collection")
                    }
                    right.contains(left)
                }
                OperatorType.CONTAINS -> {
                    @Suppress("UNCHECKED_CAST")
                    try {
                        left as Collection<Any>
                    } catch (e: Exception) {
                        throw BadExpressionException("Value of $leftOperand is not Collection")
                    }
                    left.contains(right)
                }
                OperatorType.CONTAINS_ANY,
                OperatorType.CONTAINS_ALL -> checkCollection(left, right, operatorType)
            }
            return if (granted) {
                AccessResponse(Access.Granted, accessRequest)
            } else {
                AccessResponse(Access.Denied(), accessRequest)
            }
        } catch (e: NoSuchAttributeException) {
            return AccessResponse(Access.Denied(), accessRequest)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun checkComparable(left: Any?, right: Any?, operatorType: OperatorType): Boolean {
        try {
            left as Comparable<Any> > right as Comparable<Any>
        } catch (e: Exception) {
            throw BadExpressionException("Values of $leftOperand and $rightOperand are not comparable")
        }
        return when (operatorType) {
            OperatorType.GREATER_THAN -> left > right
            OperatorType.GREATER_THAN_EQUAL -> left >= right
            OperatorType.LESS_THAN -> left < right
            OperatorType.LESS_THAN_EQUAL -> left <= right
            else -> throw IncorrectOperator("$operatorType not recognized as a Comparison operator")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun checkCollection(left: Any?, right: Any?, operatorType: OperatorType): Boolean {
        try {
            left as Collection<Any>
            right as Collection<Any>
        } catch (e: Exception) {
            throw BadExpressionException("Values of $leftOperand and $rightOperand are not Collections")
        }
        return when (operatorType) {
            OperatorType.CONTAINS_ALL -> left.containsAll(right)
            OperatorType.CONTAINS_ANY -> right.any { left.contains(it) }
            else -> throw IncorrectOperator("$operatorType not recognized as a Collection operator")
        }
    }

}

class BadExpressionException(message: String) : Exception(message)
class IncorrectOperator(message: String) : Exception(message)


enum class AttributeType {
    SUBJECT,
    ACTION,
    RESOURCE,
    ENVIRONMENT
}

/**
 * The available expression Operators
 */
enum class OperatorType {
    EQUAL,
    GREATER_THAN,
    GREATER_THAN_EQUAL,
    LESS_THAN,
    LESS_THAN_EQUAL,
    CONTAINS,
    CONTAINS_ALL,
    CONTAINS_ANY,
    IS_IN
}

interface ValueReference {
    fun get(accessRequest: AccessRequest): Any?
}

data class PassThroughReference(val value: Any?) : ValueReference {
    override fun get(accessRequest: AccessRequest): Any? {
        return value
    }
}

data class AttributeReference(
    val type: AttributeType,
    val key: String
) : ValueReference {
    override fun get(accessRequest: AccessRequest): Any? {
        val attributeMap = when (type) {
            AttributeType.SUBJECT -> accessRequest.subject
            AttributeType.ACTION -> accessRequest.action
            AttributeType.RESOURCE -> accessRequest.resource
            AttributeType.ENVIRONMENT -> accessRequest.environment
        }
        if (!attributeMap.containsKey(key)) {
            throw NoSuchAttributeException()
        }
        return attributeMap[key]
    }
}


class NoSuchAttributeException : Exception()
