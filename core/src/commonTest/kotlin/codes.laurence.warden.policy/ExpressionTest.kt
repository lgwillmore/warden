package codes.laurence.warden.policy

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.test.assertDenied
import codes.laurence.warden.test.assertGranted
import kotlin.test.Test

class ComparisonOperatorsTest {
    val request17 = AccessRequest().copy(
        subject = mapOf(Pair("age", 17))
    )
    val request18 = AccessRequest().copy(
        subject = mapOf(Pair("age", 18))
    )
    val request19 = AccessRequest().copy(
        subject = mapOf(Pair("age", 19))
    )

    @Test
    fun greaterThan() {
        var greaterThan18: Policy = allOf { subject("age") greaterThan 18 }
        assertDenied(greaterThan18.checkAuthorized(request17), request17)
        assertDenied(greaterThan18.checkAuthorized(request18), request18)
        assertGranted(greaterThan18.checkAuthorized(request19), request19)
        greaterThan18 = Exp.subject("age") greaterThan 18
        assertDenied(greaterThan18.checkAuthorized(request17), request17)
        assertDenied(greaterThan18.checkAuthorized(request18), request18)
        assertGranted(greaterThan18.checkAuthorized(request19), request19)
    }

    @Test
    fun greaterThanEqual() {
        var greaterThanEqual18: Policy = allOf { subject("age") greaterThanEqual 18 }
        assertDenied(greaterThanEqual18.checkAuthorized(request17))
        assertGranted(greaterThanEqual18.checkAuthorized(request18))
        assertGranted(greaterThanEqual18.checkAuthorized(request19))
        greaterThanEqual18 = Exp.subject("age") greaterThanEqual 18
        assertDenied(greaterThanEqual18.checkAuthorized(request17))
        assertGranted(greaterThanEqual18.checkAuthorized(request18))
        assertGranted(greaterThanEqual18.checkAuthorized(request19))
    }

    @Test
    fun equal() {
        var equal18: Policy = allOf { subject("age") equalTo 18 }
        assertDenied(equal18.checkAuthorized(request17))
        assertGranted(equal18.checkAuthorized(request18))
        assertDenied(equal18.checkAuthorized(request19))
        equal18 = Exp.subject("age") equalTo 18
        assertDenied(equal18.checkAuthorized(request17))
        assertGranted(equal18.checkAuthorized(request18))
        assertDenied(equal18.checkAuthorized(request19))
    }

    @Test
    fun lessThan() {
        var lessThan18: Policy = allOf { subject("age") lessThan 18 }
        assertGranted(lessThan18.checkAuthorized(request17))
        assertDenied(lessThan18.checkAuthorized(request18))
        assertDenied(lessThan18.checkAuthorized(request19))
        lessThan18 = Exp.subject("age") lessThan 18
        assertGranted(lessThan18.checkAuthorized(request17))
        assertDenied(lessThan18.checkAuthorized(request18))
        assertDenied(lessThan18.checkAuthorized(request19))
    }

    @Test
    fun lessThanEqual() {
        var lessThanEqual18: Policy = allOf { subject("age") lessThanEqual 18 }
        assertGranted(lessThanEqual18.checkAuthorized(request17))
        assertGranted(lessThanEqual18.checkAuthorized(request18))
        assertDenied(lessThanEqual18.checkAuthorized(request19))
        lessThanEqual18 = Exp.subject("age") lessThanEqual 18
        assertGranted(lessThanEqual18.checkAuthorized(request17))
        assertGranted(lessThanEqual18.checkAuthorized(request18))
        assertDenied(lessThanEqual18.checkAuthorized(request19))
    }

    @Test
    fun compareNonExistentKey() {
        val requestWithMissingKeys = AccessRequest()

        val somePolicy = Exp.subject("not_here") equalTo resourceVal("not_here")
        assertDenied(somePolicy.checkAuthorized(requestWithMissingKeys))
    }

    @Test
    fun compareSubjectToResource() {
        val request17 = AccessRequest().copy(
            subject = mapOf(Pair("age", 17)),
            resource = mapOf(Pair("limit", 18))
        )
        val request18 = AccessRequest().copy(
            subject = mapOf(Pair("age", 18)),
            resource = mapOf(Pair("limit", 18))
        )

        var mustBeOverResourceLimitPolicy: Policy =
            Exp.subject("age") greaterThanEqual resourceVal("limit")
        assertGranted(mustBeOverResourceLimitPolicy.checkAuthorized(request18))
        assertDenied(mustBeOverResourceLimitPolicy.checkAuthorized(request17))
        mustBeOverResourceLimitPolicy = allOf { subject("age") greaterThanEqual resourceVal("limit") }
        assertGranted(mustBeOverResourceLimitPolicy.checkAuthorized(request18))
        assertDenied(mustBeOverResourceLimitPolicy.checkAuthorized(request17))
    }

    @Test
    fun compareResourceToEnvironment() {
        val expired = AccessRequest().copy(
            resource = mapOf(Pair("expires", 1)),
            environment = mapOf(Pair("timeOfRequest", 3))
        )
        val notExpired = AccessRequest().copy(
            resource = mapOf(Pair("expires", 3)),
            environment = mapOf(Pair("timeOfRequest", 1))
        )

        var policy: Policy = Exp.resource("expires") greaterThanEqual environmentVal("timeOfRequest")
        assertDenied(policy.checkAuthorized(expired))
        assertGranted(policy.checkAuthorized(notExpired))
        policy = allOf { resource("expires") greaterThanEqual environmentVal("timeOfRequest") }
        assertDenied(policy.checkAuthorized(expired))
        assertGranted(policy.checkAuthorized(notExpired))
    }

    @Test
    fun compareEnvironmentToSubject() {
        val notOnSite = AccessRequest().copy(
            subject = mapOf(Pair("siteID", 3)),
            environment = mapOf(Pair("requestSiteID", 4))
        )
        val onSite = AccessRequest().copy(
            subject = mapOf(Pair("siteID", 3)),
            environment = mapOf(Pair("requestSiteID", 3))
        )

        var subjectMustBeOnTheirSitePolicy: Policy =
            Exp.environment("requestSiteID") equalTo subjectVal("siteID")
        assertDenied(subjectMustBeOnTheirSitePolicy.checkAuthorized(notOnSite))
        assertGranted(subjectMustBeOnTheirSitePolicy.checkAuthorized(onSite))
        subjectMustBeOnTheirSitePolicy = anyOf { environment("requestSiteID") equalTo subjectVal("siteID") }
        assertDenied(subjectMustBeOnTheirSitePolicy.checkAuthorized(notOnSite))
        assertGranted(subjectMustBeOnTheirSitePolicy.checkAuthorized(onSite))
    }

    @Test
    fun compareActionToSubject() {
        val hasReadPermission = AccessRequest().copy(
            subject = mapOf(Pair("permission", "READ")),
            action = mapOf(Pair("type", "READ"))
        )
        val noReadPermission = AccessRequest().copy(
            subject = mapOf(Pair("permission", "NONE")),
            action = mapOf(Pair("type", "READ"))
        )

        var userMustHaveActionPermission: Policy = Exp.action("type") equalTo subjectVal("permission")
        assertDenied(userMustHaveActionPermission.checkAuthorized(noReadPermission))
        assertGranted(userMustHaveActionPermission.checkAuthorized(hasReadPermission))
        userMustHaveActionPermission = anyOf { action("type") equalTo subjectVal("permission") }
        assertDenied(userMustHaveActionPermission.checkAuthorized(noReadPermission))
        assertGranted(userMustHaveActionPermission.checkAuthorized(hasReadPermission))
    }
}

class CollectionOperatorsTest {
    @Test
    fun checkAttributeContainsAllOf() {
        val allRoles = AccessRequest().copy(
            subject = mapOf(Pair("roles", listOf("USER", "ACCOUNT_MANAGER", "SUPERVISOR")))
        )
        val someRoles = AccessRequest().copy(
            subject = mapOf(Pair("roles", listOf("USER", "ACCOUNT_MANAGER")))
        )
        val noRoles = AccessRequest().copy(
            subject = mapOf(Pair("roles", listOf("USER")))
        )

        var policy: Policy = Exp.subject("roles") containsAll listOf("ACCOUNT_MANAGER", "SUPERVISOR")
        assertGranted(policy.checkAuthorized(allRoles))
        assertDenied(policy.checkAuthorized(someRoles))
        assertDenied(policy.checkAuthorized(noRoles))
        policy = allOf { subject("roles") containsAll listOf("ACCOUNT_MANAGER", "SUPERVISOR") }
        assertGranted(policy.checkAuthorized(allRoles))
        assertDenied(policy.checkAuthorized(someRoles))
        assertDenied(policy.checkAuthorized(noRoles))
    }

    @Test
    fun checkAttributeContainsAnyOf() {
        val managerRequest = AccessRequest().copy(
            subject = mapOf(Pair("roles", listOf("USER", "ACCOUNT_MANAGER")))
        )
        val supervisorRequest = AccessRequest().copy(
            subject = mapOf(Pair("roles", listOf("ADMIN", "SUPERVISOR")))
        )
        val neitherRequest = AccessRequest().copy(
            subject = mapOf(Pair("roles", listOf("USER", "ADMIN")))
        )

        var policy: Policy = Exp.subject("roles") containsAny listOf("ACCOUNT_MANAGER", "SUPERVISOR")
        assertGranted(policy.checkAuthorized(managerRequest))
        assertGranted(policy.checkAuthorized(supervisorRequest))
        assertDenied(policy.checkAuthorized(neitherRequest))
        policy = allOf { subject("roles") containsAny listOf("ACCOUNT_MANAGER", "SUPERVISOR") }
        assertGranted(policy.checkAuthorized(managerRequest))
        assertGranted(policy.checkAuthorized(supervisorRequest))
        assertDenied(policy.checkAuthorized(neitherRequest))
    }


    @Test
    fun checkAttributeContains() {
        val managerRequest = AccessRequest().copy(
            subject = mapOf(Pair("roles", listOf("USER", "ACCOUNT_MANAGER")))
        )
        val supervisorRequest = AccessRequest().copy(
            subject = mapOf(Pair("roles", listOf("USER", "SUPERVISOR")))
        )

        var policy: Policy = Exp.subject("roles") contains "ACCOUNT_MANAGER"
        assertGranted(policy.checkAuthorized(managerRequest))
        assertDenied(policy.checkAuthorized(supervisorRequest))
        policy = allOf { subject("roles") contains "ACCOUNT_MANAGER" }
        assertGranted(policy.checkAuthorized(managerRequest))
        assertDenied(policy.checkAuthorized(supervisorRequest))
    }

    @Test
    fun checkAttributeIsIn() {
        val managerRequest = AccessRequest().copy(
            subject = mapOf(Pair("role", "ACCOUNT_MANAGER"))
        )
        val supervisorRequest = AccessRequest().copy(
            subject = mapOf(Pair("role", "SUPERVISOR"))
        )
        val engineerRequest = AccessRequest().copy(
            subject = mapOf(Pair("role", "ENGINEER"))
        )

        var policy: Policy = Exp.subject("role") isIn listOf("ACCOUNT_MANAGER", "SUPERVISOR")
        assertGranted(policy.checkAuthorized(managerRequest))
        assertGranted(policy.checkAuthorized(supervisorRequest))
        assertDenied(policy.checkAuthorized(engineerRequest))
        policy = allOf { subject("role") isIn listOf("ACCOUNT_MANAGER", "SUPERVISOR") }
        assertGranted(policy.checkAuthorized(managerRequest))
        assertGranted(policy.checkAuthorized(supervisorRequest))
        assertDenied(policy.checkAuthorized(engineerRequest))
    }
}