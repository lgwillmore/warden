package codes.laurence.warden.enforce

import assertk.assertThat
import assertk.assertions.isInstanceOf
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.decision.DecisionPointInMemory
import codes.laurence.warden.policy.expression.Exp
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFailsWith

class EnforcementPointDefaultTest {

    @Test
    fun `constructor - list of policies`() {
        val policies = listOf(Exp.action("foo") equalTo "bar")
        val testObj = EnforcementPointDefault(policies)

        assertThat(testObj.decisionPoint).isInstanceOf(DecisionPointInMemory::class)
    }

    @Test
    fun `enforceAuthorization - Denied`() {

        val request = AccessRequest(
            subject = mapOf("foo" to "bar")
        )

        val deniedResponse = AccessResponse(
            access = Access.Denied(),
            request = AccessRequest(
                subject = mapOf("foo" to "returned")
            )
        )

        val decisionPointMock = mockk<DecisionPoint> {
            coEvery { checkAuthorized(request) } returns deniedResponse
        }

        val testObj = EnforcementPointDefault(decisionPointMock)

        assertFailsWith(NotAuthorizedException::class) {
            runBlockingTest {
                testObj.enforceAuthorization(request)
            }
        }

    }

    @Test
    fun `enforceAuthorization - Granted`() {

        val request = AccessRequest(
            subject = mapOf("foo" to "bar")
        )

        val grantedResponse = AccessResponse(
            access = Access.Granted(),
            request = AccessRequest(
                subject = mapOf("foo" to "returned")
            )
        )

        val decisionPointMock = mockk<DecisionPoint> {
            coEvery { checkAuthorized(request) } returns grantedResponse
        }

        val testObj = EnforcementPointDefault(decisionPointMock)

        runBlockingTest {
            testObj.enforceAuthorization(request)
        }

    }
}