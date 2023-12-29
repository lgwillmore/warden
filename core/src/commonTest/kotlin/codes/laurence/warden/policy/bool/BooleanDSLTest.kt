package codes.laurence.warden.policy.bool

import codes.laurence.warden.policy.assertLeftOperand
import codes.laurence.warden.policy.expression.AttributeType
import codes.laurence.warden.policy.expression.ExpressionPolicy
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BooleanDSLTest {
    @Test
    fun `leftOperand - Subject`() {
        val key = "key1"
        val policy = anyOf { subject(key) equalTo 1 }
        assertLeftOperand(policy.policies[0] as ExpressionPolicy, AttributeType.SUBJECT, listOf(key))
    }

    @Test
    fun `leftOperand - Action`() {
        val key = "key1"
        val policy = anyOf { action(key) equalTo 1 }
        assertLeftOperand(policy.policies[0] as ExpressionPolicy, AttributeType.ACTION, listOf(key))
    }

    @Test
    fun `leftOperand - Resource`() {
        val key = "key1"
        val policy = allOf { resource(key) equalTo 1 }
        assertLeftOperand(policy.policies[0] as ExpressionPolicy, AttributeType.RESOURCE, listOf(key))
    }

    @Test
    fun `leftOperand - Environment`() {
        val key = "key1"
        val policy = allOf { environment(key) equalTo 1 }
        assertLeftOperand(policy.policies[0] as ExpressionPolicy, AttributeType.ENVIRONMENT, listOf(key))
    }

    @Test
    fun `nested - anyOf`() {
        val policy =
            allOf {
                anyOf { environment("foo") equalTo 1 }
            }
        assertTrue(((policy.policies[0] as AnyOf).policies).isNotEmpty())
    }

    @Test
    fun `nested - allOf`() {
        val policy =
            allOf {
                allOf { environment("foo") equalTo 1 }
            }
        assertTrue(((policy.policies[0] as AllOf).policies).isNotEmpty())
    }

    @Test
    fun `nested - notAnyOf`() {
        val policy =
            allOf {
                notAnyOf { environment("foo") equalTo 1 }
            }
        assertIs<AnyOf>((policy.policies[0] as Not).policy)
    }

    @Test
    fun `nested - notAllOf`() {
        val policy =
            allOf {
                notAllOf { environment("foo") equalTo 1 }
            }
        assertIs<AllOf>((policy.policies[0] as Not).policy)
    }
}
