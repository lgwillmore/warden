package codes.laurence.warden.decision

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessRequestBatch
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.information.InformationPoint
import codes.laurence.warden.policy.Policy
import codes.laurence.warden.test.attributesFixture
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.coEvery
import io.mockative.coVerify
import io.mockative.every
import io.mockative.mock
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DecisionPointLocalTest {
    private val accessRequest =
        AccessRequest().copy(
            action = mapOf("foo" to "bar"),
        )
    private val enrichedRequest =
        AccessRequest().copy(
            action = mapOf("foo" to "enriched"),
        )

    @Mock
    val informationPointMock = mock(classOf<InformationPoint>())

    private val denial = AccessResponse(Access.Denied(), accessRequest)
    private val granted = AccessResponse(Access.Granted(), accessRequest)

    @Mock
    val willAuthorizePolicy = mock(classOf<Policy>())

    @Mock
    val willNotAuthorizePolicy = mock(classOf<Policy>())

    @Mock
    val allowResource1Policy = mock(classOf<Policy>())

    @BeforeTest
    fun beforeEachTest() =
        runBlocking {
            coEvery { informationPointMock.enrich(accessRequest) }.returns(enrichedRequest)

            every { willAuthorizePolicy.checkAuthorized(accessRequest) }.returns(granted)
            every { willAuthorizePolicy.checkAuthorized(enrichedRequest) }.returns(AccessResponse(Access.Granted(), enrichedRequest))

            every { willNotAuthorizePolicy.checkAuthorized(accessRequest) }.returns(denial)
            every { willNotAuthorizePolicy.checkAuthorized(enrichedRequest) }.returns(AccessResponse(Access.Granted(), enrichedRequest))
        }

    @Test
    fun `checkAuthorised - enriches before decision`() =
        runBlockingTest {
            val accessPolicyMock = willAuthorizePolicy
            val denyPolicyMock = willNotAuthorizePolicy

            val testObj =
                DecisionPointLocal(
                    allow = listOf(accessPolicyMock),
                    deny = listOf(denyPolicyMock),
                    informationPoint = informationPointMock,
                )

            testObj.checkAuthorized(accessRequest)

            coVerify {
                informationPointMock.enrich(accessRequest)
                accessPolicyMock.checkAuthorized(enrichedRequest)
                denyPolicyMock.checkAuthorized(enrichedRequest)
            }.wasInvoked()
        }

    @Test
    fun checkAuthorized_emptyPolicies() =
        runBlockingTest {
            assertEquals(
                AccessResponse(
                    access = Access.Denied(),
                    request = accessRequest,
                ),
                DecisionPointLocal(
                    allow = emptyList(),
                ).checkAuthorized(accessRequest),
            )
        }

    @Test
    fun checkAuthorized_atLeastOneAuthorized() =
        runBlockingTest {
            assertEquals(
                granted,
                DecisionPointLocal(
                    allow =
                    listOf(
                        willNotAuthorizePolicy,
                        willNotAuthorizePolicy,
                        willAuthorizePolicy,
                        willNotAuthorizePolicy,
                    ),
                ).checkAuthorized(accessRequest),
            )
        }

    @Test
    fun checkAuthorized_allAuthorized() =
        runBlockingTest {
            assertEquals(
                granted,
                DecisionPointLocal(
                    allow =
                    listOf(
                        willAuthorizePolicy,
                        willAuthorizePolicy,
                        willAuthorizePolicy,
                    ),
                ).checkAuthorized(accessRequest),
            )
        }

    @Test
    fun checkAuthorized_NoneAuthorized() =
        runBlockingTest {
            assertEquals(
                denial,
                DecisionPointLocal(
                    allow =
                    listOf(
                        willNotAuthorizePolicy,
                        willNotAuthorizePolicy,
                        willNotAuthorizePolicy,
                    ),
                ).checkAuthorized(accessRequest),
            )
        }

    @Test
    fun checkAuthorized_atLeastOneAuthorized_alsoDenied() =
        runBlockingTest {
            assertEquals(
                denial,
                DecisionPointLocal(
                    allow =
                    listOf(
                        willNotAuthorizePolicy,
                        willNotAuthorizePolicy,
                        willAuthorizePolicy,
                        willNotAuthorizePolicy,
                    ),
                    deny =
                    listOf(
                        willAuthorizePolicy,
                    ),
                ).checkAuthorized(accessRequest),
            )
        }

    @Test
    fun checkAuthorized_atLeastOneAuthorized_notDenied() =
        runBlockingTest {
            assertEquals(
                granted,
                DecisionPointLocal(
                    allow =
                    listOf(
                        willNotAuthorizePolicy,
                        willNotAuthorizePolicy,
                        willAuthorizePolicy,
                        willNotAuthorizePolicy,
                    ),
                    deny =
                    listOf(
                        willNotAuthorizePolicy,
                    ),
                ).checkAuthorized(accessRequest),
            )
        }

    @Test
    fun checkAuthorizedBatch() {
        val resource1 = attributesFixture()
        val resource2 = attributesFixture()
        val request =
            AccessRequestBatch(
                subject = attributesFixture(),
                action = attributesFixture(),
                environment = attributesFixture(),
                resources = listOf(resource1, resource2),
            )

        val request1 =
            AccessRequest(
                subject = request.subject,
                action = request.action,
                environment = request.environment,
                resource = resource1,
            )
        val response1 = AccessResponse(access = Access.Granted(), request1)

        val request2 =
            AccessRequest(
                subject = request.subject,
                action = request.action,
                environment = request.environment,
                resource = resource2,
            )
        val response2 = AccessResponse(access = Access.Denied(), request2)

        every { allowResource1Policy.checkAuthorized(request1) }.returns(response1)
        every { allowResource1Policy.checkAuthorized(request2) }.returns(response2)

        val testObj =
            DecisionPointLocal(
                allow = listOf(allowResource1Policy),
            )

        runBlockingTest {
            val actual = testObj.checkAuthorizedBatch(request)

            assertEquals(listOf(response1, response2), actual)
        }
    }
}
