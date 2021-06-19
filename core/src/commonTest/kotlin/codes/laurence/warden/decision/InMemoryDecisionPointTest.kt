package codes.laurence.warden.decision

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.information.InformationPoint
import codes.laurence.warden.policy.Policy
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
val informationProviderMock = mockk<InformationPoint> {
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

class InMemoryDecisionPointTest {

    @Test
    fun `checkAuthorised - enriches before decision`() = runBlockingTest {
        val accessPolicyMock = willAuthorizePolicy
        val denyPolicyMock = willNotAuthorizePolicy

        val testObj = DecisionPointLocal(
            allow = listOf(accessPolicyMock),
            deny = listOf(denyPolicyMock),
            informationProvider = informationProviderMock
        )

        testObj.checkAuthorized(accessRequest)

        coVerifySequence {
            informationProviderMock.enrich(accessRequest)
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
}
