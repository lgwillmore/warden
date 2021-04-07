package codes.laurence.warden.decision

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.policy.Policy
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

val accessRequest = AccessRequest().copy(
    action = mapOf("foo" to "bar")
)

val denial = AccessResponse(Access.Denied(), accessRequest)
val granted = AccessResponse(Access.Granted(), accessRequest)

val willAuthorizePolicy = mockk<Policy> {
    every { checkAuthorized(accessRequest) } returns AccessResponse(Access.Granted(), accessRequest)
}
val willNotAuthorizePolicy = mockk<Policy> {
    every { checkAuthorized(accessRequest) } returns denial
}

class InMemoryDecisionPointTest {

    @Test
    fun checkAuthorized_emptyPolicies() = runBlockingTest {
        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            ),
            DecisionPointInMemory(
                accessPolicies = emptyList()
            ).checkAuthorized(accessRequest)
        )
    }


    @Test
    fun checkAuthorized_atLeastOneAuthorized() = runBlockingTest {
        assertEquals(
            granted,
            DecisionPointInMemory(
                accessPolicies = listOf(
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
            DecisionPointInMemory(
                accessPolicies = listOf(
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
            DecisionPointInMemory(
                accessPolicies = listOf(
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
            DecisionPointInMemory(
                accessPolicies = listOf(
                    willNotAuthorizePolicy,
                    willNotAuthorizePolicy,
                    willAuthorizePolicy,
                    willNotAuthorizePolicy
                ),
                denyPolicies = listOf(
                    willAuthorizePolicy
                )
            ).checkAuthorized(accessRequest)
        )
    }

    @Test
    fun checkAuthorized_atLeastOneAuthorized_notDenied() = runBlockingTest {
        assertEquals(
            granted,
            DecisionPointInMemory(
                accessPolicies = listOf(
                    willNotAuthorizePolicy,
                    willNotAuthorizePolicy,
                    willAuthorizePolicy,
                    willNotAuthorizePolicy
                ),
                denyPolicies = listOf(
                    willNotAuthorizePolicy
                )
            ).checkAuthorized(accessRequest)
        )
    }

}