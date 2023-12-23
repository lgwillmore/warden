package codes.laurence.warden.policy.members

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.policy.expression.AttributeReference
import codes.laurence.warden.policy.expression.AttributeType
import codes.laurence.warden.policy.expression.NoSuchAttributeException
import codes.laurence.warden.policy.expression.OperatorType
import codes.laurence.warden.policy.expression.ValueReference
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.eq
import io.mockative.every
import io.mockative.mock
import io.mockative.ne
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

private val MEMBER_1 = mapOf("1" to 1)
private val MEMBER_2 = mapOf("1" to 2)

private val MEMBERS =
    listOf(
        MEMBER_1,
        MEMBER_2,
    )

private val accessRequest = AccessRequest()

@Mock
private val DENY_ALL_MEMBER_POLICY = mock(classOf<MemberPolicy>())

@Mock
private val GRANTED_ALL_MEMBER_POLICY = mock(classOf<MemberPolicy>())

@Mock
private val GRANTED_MEMBER_1_POLICY = mock(classOf<MemberPolicy>())

@Mock
private val GRANTED_MEMBER_2_POLICY = mock(classOf<MemberPolicy>())

@Mock
val memberSource = mock(classOf<ValueReference>())

@Mock
val memberPolicy = mock(classOf<MemberPolicy>())

fun mockClasses() =
    runBlocking {
        every { DENY_ALL_MEMBER_POLICY.checkAuthorized(any(), any()) }.returns(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
        )

        every { GRANTED_ALL_MEMBER_POLICY.checkAuthorized(any(), any()) }.returns(
            AccessResponse(
                access = Access.Granted(),
                request = accessRequest,
            ),
        )

        every { GRANTED_MEMBER_1_POLICY.checkAuthorized(eq(MEMBER_1), any()) }.returns(
            AccessResponse(
                access = Access.Granted(),
                request = accessRequest,
            ),
        )

        every { GRANTED_MEMBER_1_POLICY.checkAuthorized(ne(MEMBER_1), any()) }.returns(

            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
        )

        every { GRANTED_MEMBER_2_POLICY.checkAuthorized(eq(MEMBER_2), any()) }.returns(
            AccessResponse(
                access = Access.Granted(),
                request = accessRequest,
            ),
        )
        every { GRANTED_MEMBER_2_POLICY.checkAuthorized(ne(MEMBER_2), any()) }.returns(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
        )
    }

class ForAnyMemberPolicyTest {
    @BeforeTest
    fun beforeEachTest() = mockClasses()

    @Test
    fun `initialise - empty member policies`() {
        val memberPolicies: List<MemberPolicy> = emptyList()

        try {
            ForAnyMemberPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )
            fail("Should be invalid state")
        } catch (e: IllegalArgumentException) {
            // All good
        }
    }

    @Test
    fun `initialise - member policies`() {
        val memberPolicies: List<MemberPolicy> = listOf(memberPolicy)

        try {
            ForAnyMemberPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )
        } catch (e: IllegalArgumentException) {
            fail("Should be fine")
        }
    }

    @Test
    fun `check - member source not a collection`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                GRANTED_ALL_MEMBER_POLICY,
                GRANTED_ALL_MEMBER_POLICY,
            )

        every { memberSource.get(accessRequest) }.returns("MEMBERS")

        val testObj =
            ForAnyMemberPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            actual,
        )
    }

    @Test
    fun `check - member source present`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                GRANTED_ALL_MEMBER_POLICY,
                GRANTED_ALL_MEMBER_POLICY,
            )

        every { memberSource.get(accessRequest) }.throws(NoSuchAttributeException())

        val testObj =
            ForAnyMemberPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            actual,
        )
    }

    @Test
    fun `check - members not maps`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                GRANTED_ALL_MEMBER_POLICY,
                GRANTED_ALL_MEMBER_POLICY,
            )

        every { memberSource.get(accessRequest) }.returns(listOf("member"))

        val testObj =
            ForAnyMemberPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            actual,
        )
    }

    @Test
    fun `check - some members granted by all policies - expect granted`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                GRANTED_ALL_MEMBER_POLICY,
                GRANTED_MEMBER_2_POLICY,
            )

        every { memberSource.get(accessRequest) }.returns(MEMBERS)

        val testObj =
            ForAnyMemberPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Granted(),
                request = accessRequest,
            ),
            actual,
        )
    }

    @Test
    fun `check - members granted by some policies but no member for all policies - expect denied`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                GRANTED_MEMBER_1_POLICY,
                GRANTED_MEMBER_2_POLICY,
            )
        every { memberSource.get(accessRequest) }.returns(MEMBERS)

        val testObj =
            ForAnyMemberPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            actual,
        )
    }

    @Test
    fun `check - all deny - expect deny`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                DENY_ALL_MEMBER_POLICY,
                DENY_ALL_MEMBER_POLICY,
            )
        every { memberSource.get(accessRequest) }.returns(MEMBERS)

        val testObj =
            ForAnyMemberPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            actual,
        )
    }
}

class ForAllMembersPolicyTest {
    @BeforeTest
    fun beforeEachTest() = mockClasses()

    @Test
    fun `initialise - empty member policies`() {
        val memberPolicies: List<MemberPolicy> = emptyList()

        try {
            ForAllMembersPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )
            fail("Should be invalid state")
        } catch (e: IllegalArgumentException) {
            // All good
        }
    }

    @Test
    fun `initialise - member policies`() {
        val memberPolicies: List<MemberPolicy> = listOf(memberPolicy)

        try {
            ForAllMembersPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )
        } catch (e: IllegalArgumentException) {
            fail("Should be fine")
        }
    }

    @Test
    fun `check - member source not a collection`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                GRANTED_ALL_MEMBER_POLICY,
                GRANTED_ALL_MEMBER_POLICY,
            )
        every { memberSource.get(accessRequest) }.returns("MEMBERS")

        val testObj =
            ForAllMembersPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            actual,
        )
    }

    @Test
    fun `check - member source not present`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                GRANTED_ALL_MEMBER_POLICY,
                GRANTED_ALL_MEMBER_POLICY,
            )
        every { memberSource.get(accessRequest) }.throws(NoSuchAttributeException())

        val testObj =
            ForAllMembersPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            actual,
        )
    }

    @Test
    fun `check - members not maps`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                GRANTED_ALL_MEMBER_POLICY,
                GRANTED_ALL_MEMBER_POLICY,
            )
        every { memberSource.get(accessRequest) }.returns(listOf("member"))

        val testObj =
            ForAllMembersPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            actual,
        )
    }

    @Test
    fun `check - some members granted by all policies - expect denied`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                GRANTED_ALL_MEMBER_POLICY,
                GRANTED_MEMBER_2_POLICY,
            )
        every { memberSource.get(accessRequest) }.returns(MEMBERS)

        val testObj =
            ForAllMembersPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            actual,
        )
    }

    @Test
    fun `check - all deny - expect deny`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                DENY_ALL_MEMBER_POLICY,
                DENY_ALL_MEMBER_POLICY,
            )
        every { memberSource.get(accessRequest) }.returns(MEMBERS)

        val testObj =
            ForAllMembersPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest,
            ),
            actual,
        )
    }

    @Test
    fun `check - all members granted by all policies - expect granted`() {
        val memberPolicies: List<MemberPolicy> =
            listOf(
                GRANTED_ALL_MEMBER_POLICY,
                GRANTED_ALL_MEMBER_POLICY,
            )
        every { memberSource.get(accessRequest) }.returns(MEMBERS)

        val testObj =
            ForAllMembersPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies,
            )

        val actual = testObj.checkAuthorized(accessRequest)

        assertEquals(
            AccessResponse(
                access = Access.Granted(),
                request = accessRequest,
            ),
            actual,
        )
    }
}

class MemberAttributeReferenceTest {
    @BeforeTest
    fun beforeEachTest() = mockClasses()

    @Test
    fun `cannot instantiate with empty list`() {
        try {
            MemberAttributeReference(
                path = emptyList(),
            )
        } catch (e: Exception) {
            assertIs<IllegalArgumentException>(e)
        }
    }

    @Test
    fun `get - 1 link path`() {
        val path = listOf("foo")
        val value = "bar"

        val member =
            mapOf(
                "foo" to "bar",
            )

        val testObj =
            MemberAttributeReference(
                path = path,
            )
        testObj.member = member

        assertEquals(value, testObj.get(accessRequest))
    }

    @Test
    fun `get - member not set`() {
        val path = listOf("foo")

        val testObj =
            MemberAttributeReference(
                path = path,
            )

        try {
            testObj.get(accessRequest)
            fail("Should be invalid")
        } catch (e: IllegalStateException) {
            // all good
        }
    }

    @Test
    fun `get - nested path`() {
        val path = listOf("wack", "foo")
        val value = "bar"

        val member =
            mapOf(
                "wack" to
                    mapOf(
                        "foo" to value,
                    ),
            )

        val testObj =
            MemberAttributeReference(
                path = path,
            )
        testObj.member = member

        assertEquals(value, testObj.get(accessRequest))
    }

    @Test
    fun `get - nested path - nested value is not a map`() {
        val path = listOf("wack", "foo")

        val member =
            mapOf(
                "wack" to "foo",
            )

        val testObj =
            MemberAttributeReference(
                path = path,
            )
        testObj.member = member

        try {
            testObj.get(accessRequest)
            fail("Should be exception")
        } catch (e: Exception) {
            assertIs<NoSuchAttributeException>(e)
        }
    }

    @Test
    fun `get - nested path - nested map does not contain key`() {
        val path = listOf("wack", "foo")
        val value = "bar"

        val member =
            mapOf(
                "wack" to
                    mapOf(
                        "notFoo" to value,
                    ),
            )

        val testObj =
            MemberAttributeReference(
                path = path,
            )
        testObj.member = member

        try {
            testObj.get(accessRequest)
            fail("Should be exception")
        } catch (e: Exception) {
            assertIs<NoSuchAttributeException>(e)
        }
    }
}

class MemberExpressionPolicyTest {
    @BeforeTest
    fun beforeEachTest() = mockClasses()

    @Test
    fun initialization() {
        val leftOperand = MemberAttributeReference(listOf("foo"))
        val operator = OperatorType.values().random()
        val rightOperand = AttributeReference(AttributeType.values().random(), listOf("bar"))

        val testObj =
            MemberExpressionPolicy(
                leftOperand = leftOperand,
                operatorType = operator,
                rightOperand = rightOperand,
            )

        assertEquals(leftOperand, testObj.internalExpressionPolicy.leftOperand)
        assertEquals(operator, testObj.internalExpressionPolicy.operatorType)
        assertEquals(rightOperand, testObj.internalExpressionPolicy.rightOperand)
    }
}
