package codes.laurence.warden.decision

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessRequestBatch
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.information.InformationPoint
import codes.laurence.warden.policy.Policy
import codes.laurence.warden.test.attributesFixture
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class DecisionPointLocalTest {
  private val accessRequest =
      AccessRequest()
          .copy(
              action = mapOf("foo" to "bar"),
          )
  private val enrichedRequest =
      AccessRequest()
          .copy(
              action = mapOf("foo" to "enriched"),
          )

  val informationPointMock = mockk<InformationPoint>()

  private val denial = AccessResponse(Access.Denied(), accessRequest)
  private val granted = AccessResponse(Access.Granted(), accessRequest)

  val willAuthorizePolicy = mockk<Policy>()

  val willNotAuthorizePolicy = mockk<Policy>()

  val allowResource1Policy = mockk<Policy>()

  @BeforeTest
  fun beforeEachTest() {
    runBlocking {
      coEvery { informationPointMock.enrich(accessRequest) } returns enrichedRequest

      every { willAuthorizePolicy.checkAuthorized(accessRequest) } returns granted
      every { willAuthorizePolicy.checkAuthorized(enrichedRequest) } returns
          AccessResponse(Access.Granted(), enrichedRequest)

      every { willNotAuthorizePolicy.checkAuthorized(accessRequest) } returns denial
      every { willNotAuthorizePolicy.checkAuthorized(enrichedRequest) } returns
          AccessResponse(Access.Granted(), enrichedRequest)
    }
  }

  @Test
  fun `checkAuthorised - enriches before decision`() = runBlocking {
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
    }
  }

  @Test
  fun checkAuthorized_emptyPolicies() = runBlocking {
    assertEquals(
        AccessResponse(
            access = Access.Denied(),
            request = accessRequest,
        ),
        DecisionPointLocal(
                allow = emptyList(),
            )
            .checkAuthorized(accessRequest),
    )
  }

  @Test
  fun checkAuthorized_atLeastOneAuthorized() = runBlocking {
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
            )
            .checkAuthorized(accessRequest),
    )
  }

  @Test
  fun checkAuthorized_allAuthorized() = runBlocking {
    assertEquals(
        granted,
        DecisionPointLocal(
                allow =
                    listOf(
                        willAuthorizePolicy,
                        willAuthorizePolicy,
                        willAuthorizePolicy,
                    ),
            )
            .checkAuthorized(accessRequest),
    )
  }

  @Test
  fun checkAuthorized_NoneAuthorized() = runBlocking {
    assertEquals(
        denial,
        DecisionPointLocal(
                allow =
                    listOf(
                        willNotAuthorizePolicy,
                        willNotAuthorizePolicy,
                        willNotAuthorizePolicy,
                    ),
            )
            .checkAuthorized(accessRequest),
    )
  }

  @Test
  fun checkAuthorized_atLeastOneAuthorized_alsoDenied() = runBlocking {
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
            )
            .checkAuthorized(accessRequest),
    )
  }

  @Test
  fun checkAuthorized_atLeastOneAuthorized_notDenied() = runBlocking {
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
            )
            .checkAuthorized(accessRequest),
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

    runBlocking {
      val actual = testObj.checkAuthorizedBatch(request)

      assertEquals(listOf(response1, response2), actual)
    }
  }
}
