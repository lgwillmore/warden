package codes.laurence.warden.policy.members

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.policy.expression.*
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.fail

private val MEMBER_1 = mapOf("1" to 1)
private val MEMBER_2 = mapOf("1" to 2)

private val MEMBERS = listOf(
    MEMBER_1,
    MEMBER_2
)

private val DENY_ALL_MEMBER_POLICY = mockk<MemberPolicy> {
    every { checkAuthorized(any(), any()) } answers {
        AccessResponse(
            access = Access.Denied(),
            request = arg(1)
        )
    }
}

private val GRANTED_ALL_MEMBER_POLICY = mockk<MemberPolicy> {
    every { checkAuthorized(any(), any()) } answers {
        AccessResponse(
            access = Access.Granted(),
            request = arg(1)
        )
    }
}

private val GRANTED_MEMBER_1_POLICY = mockk<MemberPolicy> {
    every { checkAuthorized(any(), any()) } answers {
        if (arg<Map<*, *>>(0) == MEMBER_1) {
            AccessResponse(
                access = Access.Granted(),
                request = arg(1)
            )
        } else {
            AccessResponse(
                access = Access.Denied(),
                request = arg(1)
            )
        }
    }
}

private val GRANTED_MEMBER_2_POLICY = mockk<MemberPolicy> {
    every { checkAuthorized(any(), any()) } answers {
        if (arg<Map<*, *>>(0) == MEMBER_2) {
            AccessResponse(
                access = Access.Granted(),
                request = arg(1)
            )
        } else {
            AccessResponse(
                access = Access.Denied(),
                request = arg(1)
            )
        }
    }
}

class ForAnyMemberPolicyTest {

    @Test
    fun `initialise - empty member policies`() {
        val memberPolicies: List<MemberPolicy> = emptyList()
        val memberSource: ValueReference = mockk()

        try {
            ForAnyMemberPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies
            )
            fail("Should be invalid state")
        } catch (e: IllegalArgumentException) {
            // All good
        }
    }

    @Test
    fun `initialise - member policies`() {
        val memberPolicies: List<MemberPolicy> = listOf(mockk())
        val memberSource: ValueReference = mockk()

        try {
            ForAnyMemberPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies
            )
        } catch (e: IllegalArgumentException) {
            fail("Should be fine")
        }
    }

    @Test
    fun `check - member source not a collection`() {
        val memberPolicies: List<MemberPolicy> = listOf(
            GRANTED_ALL_MEMBER_POLICY,
            GRANTED_ALL_MEMBER_POLICY,
        )
        val accessRequest: AccessRequest = mockk()
        val memberSource: ValueReference = mockk {
            every { get(accessRequest) } returns "MEMBERS"
        }

        val testObj = ForAnyMemberPolicy(
            memberSource = memberSource,
            memberPolicies = memberPolicies
        )

        val actual = testObj.checkAuthorized(accessRequest)

        assertThat(actual).isEqualTo(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        )
    }

    @Test
    fun `check - members not maps`() {
        val memberPolicies: List<MemberPolicy> = listOf(
            GRANTED_ALL_MEMBER_POLICY,
            GRANTED_ALL_MEMBER_POLICY,
        )
        val accessRequest: AccessRequest = mockk()
        val memberSource: ValueReference = mockk {
            every { get(accessRequest) } returns listOf("member")
        }

        val testObj = ForAnyMemberPolicy(
            memberSource = memberSource,
            memberPolicies = memberPolicies
        )

        val actual = testObj.checkAuthorized(accessRequest)

        assertThat(actual).isEqualTo(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        )
    }

    @Test
    fun `check - some members granted by all policies - expect granted`() {
        val memberPolicies: List<MemberPolicy> = listOf(
            GRANTED_ALL_MEMBER_POLICY,
            GRANTED_MEMBER_2_POLICY,
        )
        val accessRequest: AccessRequest = mockk()
        val memberSource: ValueReference = mockk {
            every { get(accessRequest) } returns MEMBERS
        }

        val testObj = ForAnyMemberPolicy(
            memberSource = memberSource,
            memberPolicies = memberPolicies
        )

        val actual = testObj.checkAuthorized(accessRequest)

        assertThat(actual).isEqualTo(
            AccessResponse(
                access = Access.Granted(),
                request = accessRequest
            )
        )
    }

    @Test
    fun `check - members granted by some policies but no member for all policies - expect denied`() {
        val memberPolicies: List<MemberPolicy> = listOf(
            GRANTED_MEMBER_1_POLICY,
            GRANTED_MEMBER_2_POLICY,
        )
        val accessRequest: AccessRequest = mockk()
        val memberSource: ValueReference = mockk {
            every { get(accessRequest) } returns MEMBERS
        }

        val testObj = ForAnyMemberPolicy(
            memberSource = memberSource,
            memberPolicies = memberPolicies
        )

        val actual = testObj.checkAuthorized(accessRequest)

        assertThat(actual).isEqualTo(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        )
    }

    @Test
    fun `check - all deny - expect deny`() {
        val memberPolicies: List<MemberPolicy> = listOf(
            DENY_ALL_MEMBER_POLICY,
            DENY_ALL_MEMBER_POLICY,
        )
        val accessRequest: AccessRequest = mockk()
        val memberSource: ValueReference = mockk {
            every { get(accessRequest) } returns MEMBERS
        }

        val testObj = ForAnyMemberPolicy(
            memberSource = memberSource,
            memberPolicies = memberPolicies
        )

        val actual = testObj.checkAuthorized(accessRequest)

        assertThat(actual).isEqualTo(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        )
    }
}

class ForAllMembersPolicyTest {

    @Test
    fun `initialise - empty member policies`() {
        val memberPolicies: List<MemberPolicy> = emptyList()
        val memberSource: ValueReference = mockk()

        try {
            ForAllMembersPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies
            )
            fail("Should be invalid state")
        } catch (e: IllegalArgumentException) {
            // All good
        }
    }

    @Test
    fun `initialise - member policies`() {
        val memberPolicies: List<MemberPolicy> = listOf(mockk())
        val memberSource: ValueReference = mockk()

        try {
            ForAllMembersPolicy(
                memberSource = memberSource,
                memberPolicies = memberPolicies
            )
        } catch (e: IllegalArgumentException) {
            fail("Should be fine")
        }
    }

    @Test
    fun `check - member source not a collection`() {
        val memberPolicies: List<MemberPolicy> = listOf(
            GRANTED_ALL_MEMBER_POLICY,
            GRANTED_ALL_MEMBER_POLICY,
        )
        val accessRequest: AccessRequest = mockk()
        val memberSource: ValueReference = mockk {
            every { get(accessRequest) } returns "MEMBERS"
        }

        val testObj = ForAllMembersPolicy(
            memberSource = memberSource,
            memberPolicies = memberPolicies
        )

        val actual = testObj.checkAuthorized(accessRequest)

        assertThat(actual).isEqualTo(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        )
    }

    @Test
    fun `check - members not maps`() {
        val memberPolicies: List<MemberPolicy> = listOf(
            GRANTED_ALL_MEMBER_POLICY,
            GRANTED_ALL_MEMBER_POLICY,
        )
        val accessRequest: AccessRequest = mockk()
        val memberSource: ValueReference = mockk {
            every { get(accessRequest) } returns listOf("member")
        }

        val testObj = ForAllMembersPolicy(
            memberSource = memberSource,
            memberPolicies = memberPolicies
        )

        val actual = testObj.checkAuthorized(accessRequest)

        assertThat(actual).isEqualTo(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        )
    }

    @Test
    fun `check - some members granted by all policies - expect denied`() {
        val memberPolicies: List<MemberPolicy> = listOf(
            GRANTED_ALL_MEMBER_POLICY,
            GRANTED_MEMBER_2_POLICY,
        )
        val accessRequest: AccessRequest = mockk()
        val memberSource: ValueReference = mockk {
            every { get(accessRequest) } returns MEMBERS
        }

        val testObj = ForAllMembersPolicy(
            memberSource = memberSource,
            memberPolicies = memberPolicies
        )

        val actual = testObj.checkAuthorized(accessRequest)

        assertThat(actual).isEqualTo(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        )
    }

    @Test
    fun `check - all deny - expect deny`() {
        val memberPolicies: List<MemberPolicy> = listOf(
            DENY_ALL_MEMBER_POLICY,
            DENY_ALL_MEMBER_POLICY,
        )
        val accessRequest: AccessRequest = mockk()
        val memberSource: ValueReference = mockk {
            every { get(accessRequest) } returns MEMBERS
        }

        val testObj = ForAllMembersPolicy(
            memberSource = memberSource,
            memberPolicies = memberPolicies
        )

        val actual = testObj.checkAuthorized(accessRequest)

        assertThat(actual).isEqualTo(
            AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        )
    }

    @Test
    fun `check - all members granted by all policies - expect granted`() {
        val memberPolicies: List<MemberPolicy> = listOf(
            GRANTED_ALL_MEMBER_POLICY,
            GRANTED_ALL_MEMBER_POLICY,
        )
        val accessRequest: AccessRequest = mockk()
        val memberSource: ValueReference = mockk {
            every { get(accessRequest) } returns MEMBERS
        }

        val testObj = ForAllMembersPolicy(
            memberSource = memberSource,
            memberPolicies = memberPolicies
        )

        val actual = testObj.checkAuthorized(accessRequest)

        assertThat(actual).isEqualTo(
            AccessResponse(
                access = Access.Granted(),
                request = accessRequest
            )
        )
    }
}

class MemberAttributeReferenceTest {

    @Test
    fun `cannot instantiate with empty list`() {
        try {
            MemberAttributeReference(
                path = emptyList()
            )
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(IllegalArgumentException::class)
        }
    }

    @Test
    fun `get - 1 link path`() {
        val path = listOf("foo")
        val value = "bar"

        val member = mapOf(
            "foo" to "bar"
        )

        val testObj = MemberAttributeReference(
            path = path
        )
        testObj.member = member

        assertThat(testObj.get(mockk())).isEqualTo(value)
    }

    @Test
    fun `get - member not set`() {
        val path = listOf("foo")

        val testObj = MemberAttributeReference(
            path = path
        )

        try {
            testObj.get(mockk())
            fail("Should be invalid")
        } catch (e: IllegalStateException) {
            // all good
        }
    }

    @Test
    fun `get - nested path`() {
        val path = listOf("wack", "foo")
        val value = "bar"

        val member = mapOf(
            "wack" to mapOf(
                "foo" to value
            )
        )

        val testObj = MemberAttributeReference(
            path = path
        )
        testObj.member = member

        assertThat(testObj.get(mockk())).isEqualTo(value)
    }

    @Test
    fun `get - nested path - nested value is not a map`() {
        val path = listOf("wack", "foo")

        val member = mapOf(
            "wack" to "foo"
        )

        val testObj = MemberAttributeReference(
            path = path
        )
        testObj.member = member

        try {
            testObj.get(mockk())
            assertk.fail("Should be exception")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(NoSuchAttributeException::class)
        }
    }

    @Test
    fun `get - nested path - nested map does not contain key`() {
        val path = listOf("wack", "foo")
        val value = "bar"

        val member = mapOf(
            "wack" to mapOf(
                "notFoo" to value
            )
        )

        val testObj = MemberAttributeReference(
            path = path
        )
        testObj.member = member

        try {
            testObj.get(mockk())
            assertk.fail("Should be exception")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(NoSuchAttributeException::class)
        }
    }
}

class MemberExpressionPolicyTest {

    @Test
    fun initialization() {
        val leftOperand = MemberAttributeReference(listOf("foo"))
        val operator = OperatorType.values().random()
        val rightOperand = AttributeReference(AttributeType.values().random(), listOf("bar"))

        val testObj = MemberExpressionPolicy(
            leftOperand = leftOperand,
            operatorType = operator,
            rightOperand = rightOperand
        )

        assertThat(testObj.internalExpressionPolicy.leftOperand).isEqualTo(leftOperand)
        assertThat(testObj.internalExpressionPolicy.operatorType).isEqualTo(operator)
        assertThat(testObj.internalExpressionPolicy.rightOperand).isEqualTo(rightOperand)
    }

    @Test
    fun checkAuthorized() {
        val leftOperand = mockk<MemberAttributeReference>(relaxed = true)
        val operator = OperatorType.values().random()
        val rightOperand = AttributeReference(AttributeType.values().random(), listOf("bar"))

        val testObj = MemberExpressionPolicy(
            leftOperand = leftOperand,
            operatorType = operator,
            rightOperand = rightOperand
        )

        val request = mockk<AccessRequest> {}
        val result = AccessResponse(
            access = listOf(Access.Denied(), Access.Granted()).random(),
            request = request
        )
        val member = mapOf("1" to 1)
        testObj.internalExpressionPolicy = mockk {
            every { checkAuthorized(request) } returns result
        }

        val actual = testObj.checkAuthorized(member, request)

        assertThat(actual).isEqualTo(result)
        coVerifyOrder {
            leftOperand.member = member
            testObj.internalExpressionPolicy.checkAuthorized(request)
        }
    }
}
