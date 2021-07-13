package codes.laurence.warden.decision

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessRequestBatch
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.information.InformationPoint
import codes.laurence.warden.policy.Policy
import codes.laurence.warden.test.attributesFixture
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

val accessRequest = AccessRequest().copy(
    action = mapOf("foo" to "bar")
)
val enrichedRequest = AccessRequest().copy(
    action = mapOf("foo" to "enriched")
)
val informationPointMock = mockk<InformationPoint> {
    coEvery { enrich(accessRequest) } returns enrichedRequest
}

val denial = AccessResponse(Access.Denied(), accessRequest)
val granted = AccessResponse(Access.Granted(), accessRequest)

val willAuthorizePolicy = mockk<Policy> {
    every { checkAuthorized(accessRequest) } returns granted
    every { checkAuthorized(enrichedRequest) } returns AccessResponse(Access.Granted(), enrichedRequest)
}
val willNotAuthorizePolicy = mockk<Policy> {
    every { checkAuthorized(accessRequest) } returns denial
    every { checkAuthorized(enrichedRequest) } returns AccessResponse(Access.Granted(), enrichedRequest)
}

class DecisionPointLocalTest {

    @Test
    fun `checkAuthorised - enriches before decision`() = runBlockingTest {
        val accessPolicyMock = willAuthorizePolicy
        val denyPolicyMock = willNotAuthorizePolicy

        val testObj = DecisionPointLocal(
            allow = listOf(accessPolicyMock),
            deny = listOf(denyPolicyMock),
            informationPoint = informationPointMock
        )

        testObj.checkAuthorized(accessRequest)

        coVerifySequence {
            informationPointMock.enrich(accessRequest)
            accessPolicyMock.checkAuthorized(enrichedRequest)
            denyPolicyMock.checkAuthorized(enrichedRequest)
        }
    }

    @Test
    fun checkAuthorized_emptyPolicies() = runBlockingTest {
        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            ),
            DecisionPointLocal(
                allow = emptyList()
            ).checkAuthorized(accessRequest)
        )
    }

    @Test
    fun checkAuthorized_atLeastOneAuthorized() = runBlockingTest {
        assertEquals(
            granted,
            DecisionPointLocal(
                allow = listOf(
                    willNotAuthorizePolicy,
                    willNotAuthorizePolicy,
                    willAuthorizePolicy,
                    willNotAuthorizePolicy
                )
            ).checkAuthorized(accessRequest)
        )
    }

    @Test
    fun checkAuthorized_allAuthorized() = runBlockingTest {
        assertEquals(
            granted,
            DecisionPointLocal(
                allow = listOf(
                    willAuthorizePolicy,
                    willAuthorizePolicy,
                    willAuthorizePolicy
                )
            ).checkAuthorized(accessRequest)
        )
    }

    @Test
    fun checkAuthorized_NoneAuthorized() = runBlockingTest {
        assertEquals(
            denial,
            DecisionPointLocal(
                allow = listOf(
                    willNotAuthorizePolicy,
                    willNotAuthorizePolicy,
                    willNotAuthorizePolicy
                )
            ).checkAuthorized(accessRequest)
        )
    }

    @Test
    fun checkAuthorized_atLeastOneAuthorized_alsoDenied() = runBlockingTest {
        assertEquals(
            denial,
            DecisionPointLocal(
                allow = listOf(
                    willNotAuthorizePolicy,
                    willNotAuthorizePolicy,
                    willAuthorizePolicy,
                    willNotAuthorizePolicy
                ),
                deny = listOf(
                    willAuthorizePolicy
                )
            ).checkAuthorized(accessRequest)
        )
    }

    @Test
    fun checkAuthorized_atLeastOneAuthorized_notDenied() = runBlockingTest {
        assertEquals(
            granted,
            DecisionPointLocal(
                allow = listOf(
                    willNotAuthorizePolicy,
                    willNotAuthorizePolicy,
                    willAuthorizePolicy,
                    willNotAuthorizePolicy
                ),
                deny = listOf(
                    willNotAuthorizePolicy
                )
            ).checkAuthorized(accessRequest)
        )
    }

    @Test
    fun checkAuthorizedBatch() {
        val resource1 = attributesFixture()
        val resource2 = attributesFixture()
        val request = AccessRequestBatch(
            subject = attributesFixture(),
            action = attributesFixture(),
            environment = attributesFixture(),
            resources = listOf(resource1, resource2)
        )

        val request1 = AccessRequest(
            subject = request.subject,
            action = request.action,
            environment = request.environment,
            resource = resource1
        )
        val response1 = AccessResponse(access = Access.Granted(), request1)

        val request2 = AccessRequest(
            subject = request.subject,
            action = request.action,
            environment = request.environment,
            resource = resource2
        )
        val response2 = AccessResponse(access = Access.Denied(), request2)

        val allowResource1Policy = mockk<Policy> {
            every { checkAuthorized(request1) } returns response1
            every { checkAuthorized(request2) } returns response2
        }

        val testObj = DecisionPointLocal(
            allow = listOf(allowResource1Policy)
        )

        runBlockingTest {
            val actual = testObj.checkAuthorizedBatch(request)

            assertThat(actual).isEqualTo(listOf(response1, response2))
        }
    }
}
