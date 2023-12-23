package codes.laurence.warden.policy

import codes.laurence.warden.policy.expression.AttributeReference
import codes.laurence.warden.policy.expression.AttributeType
import codes.laurence.warden.policy.expression.ExpressionPolicy
import kotlin.test.assertEquals

fun assertLeftOperand(
    policy: ExpressionPolicy,
    expectedType: AttributeType,
    expectedPath: List<String>,
) {
    val leftOperand = policy.leftOperand as AttributeReference
    assertEquals(expectedType, leftOperand.type)
    assertEquals(expectedPath, leftOperand.path)
}
