package codes.laurence.warden.enforce

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessRequestBatch
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.FilterAccessRequest
import codes.laurence.warden.ResourceAttributePair
import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.policy.expression.Exp
import codes.laurence.warden.test.attributesFixture
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.coEvery
import io.mockative.mock
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class EnforcementPointDefaultTest {
    private val deniedRequest =
        AccessRequest(
            subject = mapOf("foo" to "denied"),
        )

    private val grantedRequest =
        AccessRequest(
            subject = mapOf("foo" to "granted"),
        )

    private val resources =
        List(10) { index ->
            ResourceAttributePair(
                resource = ArbitraryResource(index),
                attributes = mapOf("index" to index),
            )
        }
    private val request =
        FilterAccessRequest(
            subject = attributesFixture(),
            action = attributesFixture(),
            resources = resources,
            environment = attributesFixture(),
        )

    private val responses =
        resources.map {
            val granted = listOf(true, false).random()
            if (granted) {
                AccessResponse(
                    access = Access.Granted(),
                    request =
                    AccessRequest(
                        subject = request.subject,
                        action = request.action,
                        resource = it.attributes,
                        environment = request.environment,
                    ),
                )
            } else {
                AccessResponse(
                    access = Access.Denied(),
                    request =
                    AccessRequest(
                        subject = request.subject,
                        action = request.action,
                        resource = it.attributes,
                        environment = request.environment,
                    ),
                )
            }
        }

    @Mock
    val decisionPointMock = mock(classOf<DecisionPoint>())

    @BeforeTest
    fun beforeEachTest() = runBlocking {
        coEvery { decisionPointMock.checkAuthorized(deniedRequest) }.returns(
            AccessResponse(
                access = Access.Denied(),
                request =
                AccessRequest(
                    subject = mapOf("foo" to "returned"),
                ),
            ),
        )

        coEvery { decisionPointMock.checkAuthorized(grantedRequest) }.returns(
            AccessResponse(
                access = Access.Granted(),
                request =
                AccessRequest(
                    subject = mapOf("foo" to "returned"),
                ),
            ),
        )

        coEvery {
            decisionPointMock.checkAuthorizedBatch(
                AccessRequestBatch(
                    subject = request.subject,
                    action = request.action,
                    resources = request.resources.map { it.attributes },
                    environment = request.environment,
                ),
            )
        }.returns(responses)
    }

    @Test
    fun `constructor - list of policies`() {
        val policies = listOf(Exp.action("foo") equalTo "bar")
        val testObj = EnforcementPointDefault(policies)

        assertIs<DecisionPoint>(testObj.decisionPoint)
    }

    @Test
    fun `enforceAuthorization - Denied`() {
        val testObj = EnforcementPointDefault(decisionPointMock)

        assertFailsWith(NotAuthorizedException::class) {
            runBlockingTest {
                testObj.enforceAuthorization(deniedRequest)
            }
        }
    }

    @Test
    fun `enforceAuthorization - Granted`() {
        val testObj = EnforcementPointDefault(decisionPointMock)

        runBlockingTest {
            testObj.enforceAuthorization(grantedRequest)
        }
    }

    @Test
    fun filterAuthorization() {
        val expectedResult =
            resources.mapIndexedNotNull { index, resourceAttributePair ->
                when (responses[index].access) {
                    is Access.Granted -> resourceAttributePair.resource
                    else -> null
                }
            }

        val testObj = EnforcementPointDefault(decisionPointMock)

        runBlockingTest {
            val actual = testObj.filterAuthorization(request)

            assertEquals(expectedResult, actual)
        }
    }
}

private data class ArbitraryResource(
    val counter: Int,
)
