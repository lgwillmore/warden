package codes.laurence.warden.policy

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.laurence.warden.policy.expression.AttributeReference
import codes.laurence.warden.policy.expression.AttributeType
import codes.laurence.warden.policy.expression.ExpressionPolicy

fun assertLeftOperand(policy: ExpressionPolicy, expectedType: AttributeType, expectedPath: List<String>) {
    val leftOperand = policy.leftOperand as AttributeReference
    assertThat(leftOperand.type).isEqualTo(expectedType)
    assertThat(leftOperand.path).isEqualTo(expectedPath)
}