package codes.laurence.warden.decision

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.policy.AllOf
import codes.laurence.warden.policy.Policy
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

val accessRequest = AccessRequest().copy(
    action = mapOf("foo" to "bar")
)
val willAuthorizePolicy = mockk<Policy> {
    every { checkAuthorized(accessRequest) } returns AccessResponse(Access.Granted, accessRequest)
}
val denial = AccessResponse(Access.Denied(mapOf("arbitrary" to "denial")), accessRequest)
val granted = AccessResponse(Access.Granted, accessRequest)
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
            InMemoryDecisionPoint(emptyList()).checkAuthorized(accessRequest)
        )
    }


    @Test
    fun checkAuthorized_onlySomeAuthorized() = runBlockingTest {
        assertEquals(
            denial,
            InMemoryDecisionPoint(
                listOf(
                    willAuthorizePolicy,
                    willAuthorizePolicy,
                    willNotAuthorizePolicy,
                    willAuthorizePolicy
                )
            ).checkAuthorized(accessRequest)
        )
    }

    @Test
    fun checkAuthorized_allAuthorized() = runBlockingTest {
        assertEquals(
            granted,
            InMemoryDecisionPoint(
                listOf(
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
            InMemoryDecisionPoint(
                listOf(
                    willNotAuthorizePolicy,
                    willNotAuthorizePolicy,
                    willNotAuthorizePolicy
                )
            ).checkAuthorized(accessRequest)
        )
    }

}