package codes.laurence.warden.policy

import codes.laurence.warden.test.accessRequestFixture
import codes.laurence.warden.test.expressionPolicyFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class PolicySourceInMemoryTest {
  @Test
  fun policies() {
    runBlocking {
      val expected =
          Policies(
              allow = listOf(expressionPolicyFixture()),
              deny = listOf(expressionPolicyFixture()),
          )

      val testObj =
          PolicySourceInMemory(
              allow = expected.allow,
              deny = expected.deny,
          )

      val actual = testObj.policies(accessRequestFixture())

      assertEquals(expected, actual)
    }
  }
}
