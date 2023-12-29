package codes.laurence.warden.policy.expression

import codes.laurence.warden.policy.assertLeftOperand
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionDSLTest {
    @Test
    fun `leftOperand - Subject`() {
        val key = "key1"
        val policy = Exp.subject(key) equalTo 1
        assertLeftOperand(policy, AttributeType.SUBJECT, listOf(key))
    }

    @Test
    fun `leftOperand - Action`() {
        val key = "key2"
        val policy = Exp.action(key) equalTo 1
        assertLeftOperand(policy, AttributeType.ACTION, listOf(key))
    }

    @Test
    fun `leftOperand - Resource`() {
        val key = "key2"
        val policy = Exp.resource(key) equalTo 1
        assertLeftOperand(policy, AttributeType.RESOURCE, listOf(key))
    }

    @Test
    fun `leftOperand - Environment`() {
        val key = "key2"
        val policy = Exp.environment(key) equalTo 1
        assertLeftOperand(policy, AttributeType.ENVIRONMENT, listOf(key))
    }

    @Test
    fun `leftOperand - nested`() {
        val expectedPath = listOf("key1", "key2")
        val policy = Exp.environment("key1", "key2") equalTo 1
        assertLeftOperand(policy, AttributeType.ENVIRONMENT, expectedPath)
    }

    @Test
    fun `rightOperand - Subject`() {
        val key = "key1"
        val policy = Exp.subject("blah") equalTo subjectVal(key)
        assertRightOperand(policy, AttributeType.SUBJECT, listOf(key))
    }

    @Test
    fun `rightOperand - Action`() {
        val key = "key2"
        val policy = Exp.subject("blah") equalTo actionVal(key)
        assertRightOperand(policy, AttributeType.ACTION, listOf(key))
    }

    @Test
    fun `rightOperand - Resource`() {
        val key = "key2"
        val policy = Exp.subject("blah") equalTo resourceVal(key)
        assertRightOperand(policy, AttributeType.RESOURCE, listOf(key))
    }

    @Test
    fun `rightOperand - Environment`() {
        val key = "key2"
        val policy = Exp.subject("blah") equalTo environmentVal(key)
        assertRightOperand(policy, AttributeType.ENVIRONMENT, listOf(key))
    }

    @Test
    fun `rightOperand - raw value`() {
        val value = 2
        val policy = Exp.subject("blah") equalTo value
        val rightOperand = policy.rightOperand as PassThroughReference
        assertEquals(value, rightOperand.value)
    }

    @Test
    fun `rightOperand - nested`() {
        val expectedPath = listOf("key1", "key2")
        val policy = Exp.subject("blah") equalTo environmentVal("key1", "key2")
        assertRightOperand(policy, AttributeType.ENVIRONMENT, expectedPath)
    }

    private fun assertRightOperand(
        policy: ExpressionPolicy,
        expectedType: AttributeType,
        expectedPath: List<String>,
    ) {
        val rightOperand = policy.rightOperand as AttributeReference
        assertEquals(expectedType, rightOperand.type)
        assertEquals(expectedPath, rightOperand.path)
    }

    @Test
    fun `operator - equalTo`() {
        val policy = Exp.subject("blah") equalTo 1
        assertEquals(OperatorType.EQUAL, policy.operatorType)
    }

    @Test
    fun `operator - greaterThan - value`() {
        val policy = Exp.subject("blah") greaterThan 1
        assertEquals(OperatorType.GREATER_THAN, policy.operatorType)
    }

    @Test
    fun `operator - greaterThan - attribute`() {
        val policy = Exp.subject("blah") greaterThan subjectVal("foo")
        assertEquals(OperatorType.GREATER_THAN, policy.operatorType)
    }

    @Test
    fun `operator - greaterThanEqual - value`() {
        val policy = Exp.subject("blah") greaterThanEqual 1
        assertEquals(OperatorType.GREATER_THAN_EQUAL, policy.operatorType)
    }

    @Test
    fun `operator - greaterThanEqual - attribute`() {
        val policy = Exp.subject("blah") greaterThanEqual subjectVal("foo")
        assertEquals(OperatorType.GREATER_THAN_EQUAL, policy.operatorType)
    }

    @Test
    fun `operator - lessThanEqual - value`() {
        val policy = Exp.subject("blah") lessThanEqual 1
        assertEquals(OperatorType.LESS_THAN_EQUAL, policy.operatorType)
    }

    @Test
    fun `operator - lessThanEqual - attribute`() {
        val policy = Exp.subject("blah") lessThanEqual subjectVal("foo")
        assertEquals(OperatorType.LESS_THAN_EQUAL, policy.operatorType)
    }

    @Test
    fun `operator - lessThan - value`() {
        val policy = Exp.subject("blah") lessThan 1
        assertEquals(OperatorType.LESS_THAN, policy.operatorType)
    }

    @Test
    fun `operator - lessThan - attribute`() {
        val policy = Exp.subject("blah") lessThan subjectVal("foo")
        assertEquals(OperatorType.LESS_THAN, policy.operatorType)
    }

    @Test
    fun `operator - contains`() {
        val policy = Exp.subject("blah") contains 1
        assertEquals(OperatorType.CONTAINS, policy.operatorType)
    }

    @Test
    fun `operator - containsAll - value`() {
        val policy = Exp.subject("blah") containsAll listOf(1)
        assertEquals(OperatorType.CONTAINS_ALL, policy.operatorType)
    }

    @Test
    fun `operator - containsAll - attribute`() {
        val policy = Exp.subject("blah") containsAll resourceVal("foo")
        assertEquals(OperatorType.CONTAINS_ALL, policy.operatorType)
    }

    @Test
    fun `operator - containsAny - value`() {
        val policy = Exp.subject("blah") containsAny listOf(1)
        assertEquals(OperatorType.CONTAINS_ANY, policy.operatorType)
    }

    @Test
    fun `operator - containsAny - attribute`() {
        val policy = Exp.subject("blah") containsAny resourceVal("foo")
        assertEquals(OperatorType.CONTAINS_ANY, policy.operatorType)
    }

    @Test
    fun `operator - isIn - value`() {
        val policy = Exp.subject("blah") isIn listOf(1)
        assertEquals(OperatorType.IS_IN, policy.operatorType)
    }

    @Test
    fun `operator - isIn - attribute`() {
        val policy = Exp.subject("blah") isIn resourceVal("foo")
        assertEquals(OperatorType.IS_IN, policy.operatorType)
    }
}
