package codes.laurence.warden.policy.expression

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.laurence.warden.policy.assertLeftOperand
import kotlin.test.Test

class ExpTest {

    @Test
    fun `leftOperand - Subject`() {
        val key = "key1"
        val policy = Exp.subject(key) equalTo 1
        assertLeftOperand(policy, AttributeType.SUBJECT, listOf(AString(key)))
    }

    @Test
    fun `leftOperand - Action`() {
        val key = "key2"
        val policy = Exp.action(key) equalTo 1
        assertLeftOperand(policy, AttributeType.ACTION, listOf(AString(key)))
    }

    @Test
    fun `leftOperand - Resource`() {
        val key = "key2"
        val policy = Exp.resource(key) equalTo 1
        assertLeftOperand(policy, AttributeType.RESOURCE, listOf(AString(key)))
    }

    @Test
    fun `leftOperand - Environment`() {
        val key = "key2"
        val policy = Exp.environment(key) equalTo 1
        assertLeftOperand(policy, AttributeType.ENVIRONMENT, listOf(AString(key)))
    }

    @Test
    fun `leftOperand - nested`() {
        val expectedPath = listOf(AString("key1"), AString("key2"))
        val policy = Exp.environment("key1", "key2") equalTo 1
        assertLeftOperand(policy, AttributeType.ENVIRONMENT, expectedPath)
    }

    @Test
    fun `rightOperand - Subject`() {
        val key = "key1"
        val policy = Exp.subject("blah") equalTo subjectVal(key)
        assertRightOperand(policy, AttributeType.SUBJECT, listOf(AString(key)))
    }

    @Test
    fun `rightOperand - Action`() {
        val key = "key2"
        val policy = Exp.subject("blah") equalTo actionVal(key)
        assertRightOperand(policy, AttributeType.ACTION, listOf(AString(key)))
    }

    @Test
    fun `rightOperand - Resource`() {
        val key = "key2"
        val policy = Exp.subject("blah") equalTo resourceVal(key)
        assertRightOperand(policy, AttributeType.RESOURCE, listOf(AString(key)))
    }

    @Test
    fun `rightOperand - Environment`() {
        val key = "key2"
        val policy = Exp.subject("blah") equalTo environmentVal(key)
        assertRightOperand(policy, AttributeType.ENVIRONMENT, listOf(AString(key)))
    }

    @Test
    fun `rightOperand - raw value`() {
        val value = 2
        val policy = Exp.subject("blah") equalTo value
        val rightOperand = policy.rightOperand as PassThroughReference
        assertThat(rightOperand.value).isEqualTo(value)
    }

    @Test
    fun `rightOperand - nested`() {
        val expectedPath = listOf(AString("key1"), AString("key2"))
        val policy = Exp.subject("blah") equalTo environmentVal("key1", "key2")
        assertRightOperand(policy, AttributeType.ENVIRONMENT, expectedPath)
    }

    private fun assertRightOperand(policy: ExpressionPolicy, expectedType: AttributeType, expectedPath: List<AttributePathSegment>) {
        val rightOperand = policy.rightOperand as AttributeReference
        assertThat(rightOperand.type).isEqualTo(expectedType)
        assertThat(rightOperand.path).isEqualTo(expectedPath)
    }

    @Test
    fun `operator - equalTo`() {
        val policy = Exp.subject("blah") equalTo 1
        assertThat(policy.operatorType).isEqualTo(OperatorType.EQUAL)
    }

    @Test
    fun `operator - greaterThan - value`() {
        val policy = Exp.subject("blah") greaterThan 1
        assertThat(policy.operatorType).isEqualTo(OperatorType.GREATER_THAN)
    }

    @Test
    fun `operator - greaterThan - attribute`() {
        val policy = Exp.subject("blah") greaterThan subjectVal("foo")
        assertThat(policy.operatorType).isEqualTo(OperatorType.GREATER_THAN)
    }

    @Test
    fun `operator - greaterThanEqual - value`() {
        val policy = Exp.subject("blah") greaterThanEqual 1
        assertThat(policy.operatorType).isEqualTo(OperatorType.GREATER_THAN_EQUAL)
    }

    @Test
    fun `operator - greaterThanEqual - attribute`() {
        val policy = Exp.subject("blah") greaterThanEqual subjectVal("foo")
        assertThat(policy.operatorType).isEqualTo(OperatorType.GREATER_THAN_EQUAL)
    }

    @Test
    fun `operator - lessThanEqual - value`() {
        val policy = Exp.subject("blah") lessThanEqual 1
        assertThat(policy.operatorType).isEqualTo(OperatorType.LESS_THAN_EQUAL)
    }

    @Test
    fun `operator - lessThanEqual - attribute`() {
        val policy = Exp.subject("blah") lessThanEqual subjectVal("foo")
        assertThat(policy.operatorType).isEqualTo(OperatorType.LESS_THAN_EQUAL)
    }

    @Test
    fun `operator - lessThan - value`() {
        val policy = Exp.subject("blah") lessThan 1
        assertThat(policy.operatorType).isEqualTo(OperatorType.LESS_THAN)
    }

    @Test
    fun `operator - lessThan - attribute`() {
        val policy = Exp.subject("blah") lessThan subjectVal("foo")
        assertThat(policy.operatorType).isEqualTo(OperatorType.LESS_THAN)
    }

    @Test
    fun `operator - contains`() {
        val policy = Exp.subject("blah") contains 1
        assertThat(policy.operatorType).isEqualTo(OperatorType.CONTAINS)
    }

    @Test
    fun `operator - containsAll - value`() {
        val policy = Exp.subject("blah") containsAll listOf(1)
        assertThat(policy.operatorType).isEqualTo(OperatorType.CONTAINS_ALL)
    }

    @Test
    fun `operator - containsAll - attribute`() {
        val policy = Exp.subject("blah") containsAll resourceVal("foo")
        assertThat(policy.operatorType).isEqualTo(OperatorType.CONTAINS_ALL)
    }

    @Test
    fun `operator - containsAny - value`() {
        val policy = Exp.subject("blah") containsAny listOf(1)
        assertThat(policy.operatorType).isEqualTo(OperatorType.CONTAINS_ANY)
    }

    @Test
    fun `operator - containsAny - attribute`() {
        val policy = Exp.subject("blah") containsAny resourceVal("foo")
        assertThat(policy.operatorType).isEqualTo(OperatorType.CONTAINS_ANY)
    }

    @Test
    fun `operator - isIn - value`() {
        val policy = Exp.subject("blah") isIn listOf(1)
        assertThat(policy.operatorType).isEqualTo(OperatorType.IS_IN)
    }

    @Test
    fun `operator - isIn - attribute`() {
        val policy = Exp.subject("blah") isIn resourceVal("foo")
        assertThat(policy.operatorType).isEqualTo(OperatorType.IS_IN)
    }
}
