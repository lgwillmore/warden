package codes.laurence.warden.policy.bool

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.policy.Policy
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.every
import io.mockative.mock
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

val accessRequest =
    AccessRequest().copy(
        action = mapOf("foo" to "bar"),
    )

@Mock
val willAuthorizePolicy = mock(classOf<Policy>())
val denial = AccessResponse(Access.Denied(mapOf("arbitrary" to "denial")), accessRequest)
val granted = AccessResponse(Access.Granted(), accessRequest)

@Mock
val willNotAuthorizePolicy = mock(classOf<Policy>())

class AllOfTest {

    @BeforeTest
    fun beforeEachTest() =
        runBlocking {
            every { willAuthorizePolicy.checkAuthorized(accessRequest) }.returns(
                AccessResponse(
                    Access.Granted(),
                    accessRequest,
                ),
            )

            every { willNotAuthorizePolicy.checkAuthorized(accessRequest) }.returns(denial)
        }

    @Test
    fun checkAuthorized_emptyPolicies() {
        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            AllOf().checkAuthorized(accessRequest),
        )
    }

    @Test
    fun checkAuthorized_onlySomeAuthorized() {
        assertEquals(
            denial,
            AllOf(
                willAuthorizePolicy,
                willAuthorizePolicy,
                willNotAuthorizePolicy,
                willAuthorizePolicy,
            ).checkAuthorized(accessRequest),
        )
    }

    @Test
    fun checkAuthorized_allAuthorized() {
        assertEquals(
            granted,
            AllOf(
                willAuthorizePolicy,
                willAuthorizePolicy,
                willAuthorizePolicy,
            ).checkAuthorized(accessRequest),
        )
    }

    @Test
    fun checkAuthorized_NoneAuthorized() {
        assertEquals(
            denial,
            AllOf(
                willNotAuthorizePolicy,
                willNotAuthorizePolicy,
                willNotAuthorizePolicy,
            ).checkAuthorized(accessRequest),
        )
    }
}

class AnyOfTest {
    @Test
    fun checkAuthorized_emptyPolicies() {
        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            AnyOf().checkAuthorized(accessRequest),
        )
    }

    @Test
    fun checkAuthorized_onlySomeAuthorized() {
        assertEquals(
            granted,
            AnyOf(
                willAuthorizePolicy,
                willAuthorizePolicy,
                willNotAuthorizePolicy,
                willAuthorizePolicy,
            ).checkAuthorized(accessRequest),
        )
    }

    @Test
    fun checkAuthorized_allAuthorized() {
        assertEquals(
            granted,
            AnyOf(
                willAuthorizePolicy,
                willAuthorizePolicy,
                willAuthorizePolicy,
            ).checkAuthorized(accessRequest),
        )
    }

    @Test
    fun checkAuthorized_NoneAuthorized() {
        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            AnyOf(
                willNotAuthorizePolicy,
                willNotAuthorizePolicy,
                willNotAuthorizePolicy,
            ).checkAuthorized(accessRequest),
        )
    }
}

class NotTest {
    @Test
    fun checkAuthorized_isWhenInnerIsNot() {
        assertEquals(
            denial.copy(access = Access.Granted()),
            Not(willNotAuthorizePolicy).checkAuthorized(accessRequest),
        )
    }

    @Test
    fun checkAuthorized_isNotWhenInnerIs() {
        assertEquals(
            granted.copy(access = Access.Denied()),
            Not(willAuthorizePolicy).checkAuthorized(accessRequest),
        )
    }
}
