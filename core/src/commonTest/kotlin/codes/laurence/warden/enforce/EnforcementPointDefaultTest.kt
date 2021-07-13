package codes.laurence.warden.enforce

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import codes.laurence.warden.*
import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.policy.expression.Exp
import codes.laurence.warden.test.attributesFixture
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFailsWith

class EnforcementPointDefaultTest {

    @Test
    fun `constructor - list of policies`() {
        val policies = listOf(Exp.action("foo") equalTo "bar")
        val testObj = EnforcementPointDefault(policies)

        assertThat(testObj.decisionPoint).isInstanceOf(DecisionPoint::class)
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

    @Test
    fun filterAuthorization() {
        val resources = List(10) { index ->
            ResourceAttributePair(
                resource = ArbitraryResource(index),
                attributes = mapOf("index" to index)
            )
        }
        val request = FilterAccessRequest(
            subject = attributesFixture(),
            action = attributesFixture(),
            resources = resources,
            environment = attributesFixture()
        )

        val responses = resources.map {
            val granted = listOf(true, false).random()
            if (granted) {
                AccessResponse(
                    access = Access.Granted(),
                    request = AccessRequest(
                        subject = request.subject,
                        action = request.action,
                        resource = it.attributes,
                        environment = request.environment
                    )
                )
            } else {
                AccessResponse(
                    access = Access.Denied(),
                    request = AccessRequest(
                        subject = request.subject,
                        action = request.action,
                        resource = it.attributes,
                        environment = request.environment
                    )
                )
            }
        }

        val decisionPointMock = mockk<DecisionPoint> {
            coEvery {
                checkAuthorizedBatch(
                    AccessRequestBatch(
                        subject = request.subject,
                        action = request.action,
                        resources = request.resources.map { it.attributes },
                        environment = request.environment
                    )
                )
            } returns responses
        }

        val expectedResult = resources.mapIndexedNotNull { index, resourceAttributePair ->
            when (responses[index].access) {
                is Access.Granted -> resourceAttributePair.resource
                else -> null
            }
        }

        val testObj = EnforcementPointDefault(decisionPointMock)

        runBlockingTest {
            val actual = testObj.filterAuthorization(request)

            assertThat(actual).isEqualTo(expectedResult)
        }
    }
}

private data class ArbitraryResource(
    val counter: Int
)
