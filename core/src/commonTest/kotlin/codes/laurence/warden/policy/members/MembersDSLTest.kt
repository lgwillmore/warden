package codes.laurence.warden.policy.members

import codes.laurence.warden.policy.bool.allOf
import codes.laurence.warden.policy.expression.AttributeReference
import codes.laurence.warden.policy.expression.AttributeType
import codes.laurence.warden.policy.expression.OperatorType
import codes.laurence.warden.policy.expression.subjectVal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MembersDSLTest {
    @Test
    fun forAnyMember() {
        val parentPolicy =
            allOf {
                subject("foo") forAnyMember {
                    attribute("bar", "doe") equalTo subjectVal("wack")
                    attribute("rae") contains subjectVal("wack")
                }
            }

        val expected =
            ForAnyMemberPolicy(
                memberSource = AttributeReference(AttributeType.SUBJECT, listOf("foo")),
                memberPolicies =
                listOf(
                    MemberExpressionPolicy(
                        leftOperand = MemberAttributeReference(listOf("bar", "doe")),
                        operatorType = OperatorType.EQUAL,
                        rightOperand = AttributeReference(AttributeType.SUBJECT, path = listOf("wack")),
                    ),
                    MemberExpressionPolicy(
                        leftOperand = MemberAttributeReference(listOf("rae")),
                        operatorType = OperatorType.CONTAINS,
                        rightOperand = AttributeReference(AttributeType.SUBJECT, path = listOf("wack")),
                    ),
                ),
            )

        val actual = parentPolicy.policies.first() as ForAnyMemberPolicy
        assertEquals(expected.memberSource, actual.memberSource)

        actual.memberPolicies.forEachIndexed { index, actualPolicy ->
            val expectedPolicy = expected.memberPolicies[index] as MemberExpressionPolicy
            assertIs<MemberExpressionPolicy>(actualPolicy)
            assertEquals(expectedPolicy.leftOperand, actualPolicy.leftOperand)
            assertEquals(expectedPolicy.internalExpressionPolicy, actualPolicy.internalExpressionPolicy)
        }
    }

    @Test
    fun forAllMembers() {
        val parentPolicy =
            allOf {
                subject("foo") forAllMembers {
                    attribute("bar", "doe") equalTo subjectVal("wack")
                    attribute("rae") contains subjectVal("wack")
                }
            }

        val expected =
            ForAllMembersPolicy(
                memberSource = AttributeReference(AttributeType.SUBJECT, listOf("foo")),
                memberPolicies =
                listOf(
                    MemberExpressionPolicy(
                        leftOperand = MemberAttributeReference(listOf("bar", "doe")),
                        operatorType = OperatorType.EQUAL,
                        rightOperand = AttributeReference(AttributeType.SUBJECT, path = listOf("wack")),
                    ),
                    MemberExpressionPolicy(
                        leftOperand = MemberAttributeReference(listOf("rae")),
                        operatorType = OperatorType.CONTAINS,
                        rightOperand = AttributeReference(AttributeType.SUBJECT, path = listOf("wack")),
                    ),
                ),
            )

        val actual = parentPolicy.policies.first() as ForAllMembersPolicy
        assertEquals(expected.memberSource, actual.memberSource)

        actual.memberPolicies.forEachIndexed { index, actualPolicy ->
            val expectedPolicy = expected.memberPolicies[index] as MemberExpressionPolicy
            assertIs<MemberExpressionPolicy>(actualPolicy)
            assertEquals(expectedPolicy.leftOperand, actualPolicy.leftOperand)
            assertEquals(expectedPolicy.internalExpressionPolicy, actualPolicy.internalExpressionPolicy)
        }
    }
}
