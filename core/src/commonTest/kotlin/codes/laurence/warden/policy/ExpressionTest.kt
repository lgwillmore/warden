package codes.laurence.warden.policy

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.test.assertDenied
import codes.laurence.warden.test.assertGranted
import kotlin.test.Test


class ComparisonOperatorsTest {
    val request17 = AccessRequest().copy(
        subject = mapOf(Pair("age", 17)),
        resource = mapOf(Pair("limit", 18))
    )
    val request18 = AccessRequest().copy(
        subject = mapOf(Pair("age", 18)),
        resource = mapOf(Pair("limit", 18))
    )
    val request19 = AccessRequest().copy(
        subject = mapOf(Pair("age", 19)),
        resource = mapOf(Pair("limit", 18))
    )

    @Test
    fun greaterThan() {
        val greaterThan18: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "age"),
            OperatorType.GREATER_THAN,
            PassThroughReference(18)
        )
        assertDenied(greaterThan18.checkAuthorized(request17), request17)
        assertDenied(greaterThan18.checkAuthorized(request18), request18)
        assertGranted(greaterThan18.checkAuthorized(request19), request19)
    }

    @Test
    fun greaterThanEqual() {
        val greaterThanEqual18: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "age"),
            OperatorType.GREATER_THAN_EQUAL,
            PassThroughReference(18)
        )
        assertDenied(greaterThanEqual18.checkAuthorized(request17))
        assertGranted(greaterThanEqual18.checkAuthorized(request18))
        assertGranted(greaterThanEqual18.checkAuthorized(request19))
    }

    @Test
    fun equal() {
        val equal18: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "age"),
            OperatorType.EQUAL,
            PassThroughReference(18)
        )
        assertDenied(equal18.checkAuthorized(request17))
        assertGranted(equal18.checkAuthorized(request18))
        assertDenied(equal18.checkAuthorized(request19))
    }

    @Test
    fun lessThan() {
        val lessThan18: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "age"),
            OperatorType.LESS_THAN,
            PassThroughReference(18)
        )
        assertGranted(lessThan18.checkAuthorized(request17))
        assertDenied(lessThan18.checkAuthorized(request18))
        assertDenied(lessThan18.checkAuthorized(request19))
    }

    @Test
    fun lessThanEqual() {
        val lessThanEqual18: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "age"),
            OperatorType.LESS_THAN_EQUAL,
            PassThroughReference(18)
        )
        assertGranted(lessThanEqual18.checkAuthorized(request17))
        assertGranted(lessThanEqual18.checkAuthorized(request18))
        assertDenied(lessThanEqual18.checkAuthorized(request19))
    }

    @Test
    fun compareNonExistentKey() {
        val requestWithMissingKeys = AccessRequest()

        val somePolicy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "not_here"),
            OperatorType.LESS_THAN_EQUAL,
            AttributeReference(AttributeType.RESOURCE, "not_here")
        )
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

        val mustBeOverResourceLimitPolicy: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "age"),
            OperatorType.GREATER_THAN_EQUAL,
            AttributeReference(AttributeType.RESOURCE, "limit")
        )
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

        val policy: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.RESOURCE, "expires"),
            OperatorType.GREATER_THAN_EQUAL,
            AttributeReference(AttributeType.ENVIRONMENT, "timeOfRequest")
        )
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

        val subjectMustBeOnTheirSitePolicy: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.ENVIRONMENT, "requestSiteID"),
            OperatorType.EQUAL,
            AttributeReference(AttributeType.SUBJECT, "siteID")
        )
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

        val userMustHaveActionPermission: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.ACTION, "type"),
            OperatorType.EQUAL,
            AttributeReference(AttributeType.SUBJECT, "permission")
        )
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

        val policy: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "roles"),
            OperatorType.CONTAINS_ALL,
            PassThroughReference(listOf("ACCOUNT_MANAGER", "SUPERVISOR"))
        )
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

        val policy: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "roles"),
            OperatorType.CONTAINS_ANY,
            PassThroughReference(listOf("ACCOUNT_MANAGER", "SUPERVISOR"))
        )
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

        val policy: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "roles"),
            OperatorType.CONTAINS,
            PassThroughReference("ACCOUNT_MANAGER")
        )

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

        val policy: Policy = ExpressionPolicy(
            AttributeReference(AttributeType.SUBJECT, "role"),
            OperatorType.IS_IN,
            PassThroughReference(listOf("ACCOUNT_MANAGER", "SUPERVISOR"))
        )
        assertGranted(policy.checkAuthorized(managerRequest))
        assertGranted(policy.checkAuthorized(supervisorRequest))
        assertDenied(policy.checkAuthorized(engineerRequest))
    }
}