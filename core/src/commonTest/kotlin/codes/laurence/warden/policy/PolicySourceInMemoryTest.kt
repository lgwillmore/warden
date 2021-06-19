package codes.laurence.warden.policy

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.test.accessRequestFixture
import codes.laurence.warden.test.expressionPolicyFixture
import kotlin.test.Test

class PolicySourceInMemoryTest {

    @Test
    fun policies() {
        runBlockingTest {
            val expected = Policies(
                allow = listOf(expressionPolicyFixture()),
                deny = listOf(expressionPolicyFixture())
            )

            val testObj = PolicySourceInMemory(
                allow = expected.allow,
                deny = expected.deny
            )

            val actual = testObj.policies(accessRequestFixture())

            assertThat(actual).isEqualTo(expected)
        }
    }
}
