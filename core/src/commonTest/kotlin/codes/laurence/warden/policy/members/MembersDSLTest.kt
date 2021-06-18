package codes.laurence.warden.policy.members

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.fail
import codes.laurence.warden.policy.boolean.allOf
import codes.laurence.warden.policy.expression.AttributeReference
import codes.laurence.warden.policy.expression.AttributeType
import codes.laurence.warden.policy.expression.OperatorType
import codes.laurence.warden.policy.expression.subjectVal
import kotlin.test.Test

class MembersDSLTest {

    @Test
    fun forAnyMember() {
        val parentPolicy = allOf {
            subject("foo") forAnyMember {
                attribute("bar", "doe") equalTo subjectVal("wack")
                attribute("rae") contains subjectVal("wack")
            }
        }

        val expected = ForAnyMemberPolicy(
            memberSource = AttributeReference(AttributeType.SUBJECT, listOf("foo")),
            memberPolicies = listOf(
                MemberExpressionPolicy(
                    leftOperand = MemberAttributeReference(listOf("bar", "doe")),
                    operatorType = OperatorType.EQUAL,
                    rightOperand = AttributeReference(AttributeType.SUBJECT, path = listOf("wack"))
                ),
                MemberExpressionPolicy(
                    leftOperand = MemberAttributeReference(listOf("rae")),
                    operatorType = OperatorType.CONTAINS,
                    rightOperand = AttributeReference(AttributeType.SUBJECT, path = listOf("wack"))
                )
            ),
        )

        val actual = parentPolicy.policies.first() as ForAnyMemberPolicy
        assertThat(actual.memberSource).isEqualTo(expected.memberSource)

        actual.memberPolicies.forEachIndexed { index, actualPolicy ->
            val expectedPolicy = expected.memberPolicies[index] as MemberExpressionPolicy
            if (actualPolicy is MemberExpressionPolicy) {
                assertThat(actualPolicy.leftOperand).isEqualTo(expectedPolicy.leftOperand)
                assertThat(actualPolicy.internalExpressionPolicy).isEqualTo(expectedPolicy.internalExpressionPolicy)
            } else {
                fail("Not expected type")
            }
        }
    }

    @Test
    fun forAllMembers() {
        val parentPolicy = allOf {
            subject("foo") forAllMembers {
                attribute("bar", "doe") equalTo subjectVal("wack")
                attribute("rae") contains subjectVal("wack")
            }
        }

        val expected = ForAllMembersPolicy(
            memberSource = AttributeReference(AttributeType.SUBJECT, listOf("foo")),
            memberPolicies = listOf(
                MemberExpressionPolicy(
                    leftOperand = MemberAttributeReference(listOf("bar", "doe")),
                    operatorType = OperatorType.EQUAL,
                    rightOperand = AttributeReference(AttributeType.SUBJECT, path = listOf("wack"))
                ),
                MemberExpressionPolicy(
                    leftOperand = MemberAttributeReference(listOf("rae")),
                    operatorType = OperatorType.CONTAINS,
                    rightOperand = AttributeReference(AttributeType.SUBJECT, path = listOf("wack"))
                )
            ),
        )

        val actual = parentPolicy.policies.first() as ForAllMembersPolicy
        assertThat(actual.memberSource).isEqualTo(expected.memberSource)

        actual.memberPolicies.forEachIndexed { index, actualPolicy ->
            val expectedPolicy = expected.memberPolicies[index] as MemberExpressionPolicy
            if (actualPolicy is MemberExpressionPolicy) {
                assertThat(actualPolicy.leftOperand).isEqualTo(expectedPolicy.leftOperand)
                assertThat(actualPolicy.internalExpressionPolicy).isEqualTo(expectedPolicy.internalExpressionPolicy)
            } else {
                fail("Not expected type")
            }
        }
    }
}
