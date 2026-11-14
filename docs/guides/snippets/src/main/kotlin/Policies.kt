package codes.laurence.warden.docs

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.policy.Policy
import codes.laurence.warden.policy.bool.allOf
import codes.laurence.warden.policy.bool.anyOf
import codes.laurence.warden.policy.bool.not
import codes.laurence.warden.policy.expression.Exp
import codes.laurence.warden.policy.expression.environmentVal
import codes.laurence.warden.policy.expression.resourceVal
import codes.laurence.warden.policy.expression.subjectVal

val comparisonPolicies: List<Policy> =
    listOf(
        Exp.subject("name") equalTo "Ada",
        Exp.subject("age") greaterThan 17,
        Exp.subject("age") greaterThanEqual 18,
        Exp.resource("sizeInBytes") lessThan 1_000_000,
        Exp.resource("sizeInBytes") lessThanEqual 999_999,
    )

val collectionPolicies: List<Policy> =
    listOf(
        // The attribute is a single value and the operand is a collection.
        Exp.action("type") isIn listOf("MODIFY", "DELETE"),
        // The attribute is a collection.
        Exp.subject("roles") contains "Author",
        Exp.subject("roles") containsAll listOf("Author", "Reviewer"),
        Exp.subject("roles") containsAny listOf("Author", "Admin"),
    )

val operandPolicies: List<Policy> =
    listOf(
        // Compare two attributes of the request with each other.
        Exp.subject("id") equalTo resourceVal("authorId"),
        Exp.subject("roles") contains resourceVal("requiredRole"),
        // A plain value on the left, an attribute on the right.
        Exp.value("production") equalTo environmentVal("stage"),
    )

/** Attributes nest. Each extra path segment steps into a nested map. */
val nestedPathPolicy: Policy = Exp.subject("profile", "address", "country") equalTo "ZA"

val allOfPolicy: Policy = allOf {
  resource("type") equalTo "Article"
  action("type") equalTo "READ"
}

val anyOfPolicy: Policy = anyOf {
  subject("roles") contains "Admin"
  subject("id") equalTo resourceVal("authorId")
}

val notPolicy: Policy = not(Exp.subject("suspended") equalTo true)

/** Boolean policies nest, and `notAnyOf` / `notAllOf` negate a nested group in place. */
val nestedBooleanPolicy: Policy = allOf {
  resource("type") equalTo "Article"
  anyOf {
    subject("roles") contains "Admin"
    subject("id") equalTo resourceVal("authorId")
  }
  notAnyOf {
    subject("suspended") equalTo true
    resource("locked") equalTo true
  }
}

/** Granted when at least one collaborator is this user and holds the editor role. */
val collaboratorMayEdit: Policy = allOf {
  action("type") equalTo "MODIFY"
  resource("collaborators") forAnyMember
      {
        attribute("id") equalTo subjectVal("id")
        attribute("role") equalTo "editor"
      }
}

/** Granted when every collaborator has been verified. An empty collection is granted. */
val allCollaboratorsVerified: Policy =
    Exp.resource("collaborators") forAllMembers { attribute("verified") equalTo true }

/**
 * A hand-written policy. It reads the hour from the environment and explains a denial through the
 * properties of the [Access].
 */
class BusinessHoursPolicy(private val opensAt: Int = 9, private val closesAt: Int = 17) : Policy {

  override fun checkAuthorized(accessRequest: AccessRequest): AccessResponse {
    val hour =
        accessRequest.environment["hour"] as? Int
            ?: return AccessResponse(
                Access.Denied(mapOf("reason" to "The request carries no hour")), accessRequest)
    return if (hour in opensAt until closesAt) {
      AccessResponse(Access.Granted(), accessRequest)
    } else {
      AccessResponse(Access.Denied(mapOf("reason" to "Outside business hours")), accessRequest)
    }
  }
}
