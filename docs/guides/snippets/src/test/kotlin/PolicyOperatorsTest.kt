package codes.laurence.warden.docs

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.policy.bool.AllOf
import codes.laurence.warden.policy.expression.BadExpressionException
import codes.laurence.warden.policy.expression.Exp
import kotlin.test.Test
import kotlin.test.assertFailsWith

class PolicyOperatorsTest {

  @Test
  fun `every comparison and collection operator grants for a matching request`() {
    val request =
        AccessRequest(
            subject =
                mapOf(
                    "name" to "Ada",
                    "age" to 18,
                    "roles" to listOf("Author", "Reviewer"),
                    "id" to "ada",
                ),
            action = mapOf("type" to "MODIFY"),
            resource =
                mapOf("sizeInBytes" to 999_999, "authorId" to "ada", "requiredRole" to "Author"),
            environment = mapOf("stage" to "production"),
        )

    val all = AllOf(comparisonPolicies + collectionPolicies + operandPolicies)

    assertThat(all.checkAuthorized(request).access).isEqualTo(Access.Granted())
  }

  @Test
  fun `nested paths step into nested maps`() {
    val request =
        AccessRequest(subject = mapOf("profile" to mapOf("address" to mapOf("country" to "ZA"))))

    assertThat(nestedPathPolicy.checkAuthorized(request).access).isEqualTo(Access.Granted())
  }

  @Test
  fun `a missing attribute is denied, not an error`() {
    val request =
        AccessRequest(subject = mapOf("profile" to mapOf("address" to emptyMap<String, Any?>())))

    assertThat(nestedPathPolicy.checkAuthorized(request).access).isEqualTo(Access.Denied())
  }

  @Test
  fun `an operand of the wrong type is an error`() {
    val policy = Exp.subject("roles") contains "Author"
    val request = AccessRequest(subject = mapOf("roles" to "Author"))

    assertFailsWith<BadExpressionException> { policy.checkAuthorized(request) }
  }

  @Test
  fun `member policies evaluate each member of a collection attribute`() {
    val request =
        AccessRequest(
            subject = mapOf("id" to "ada"),
            action = mapOf("type" to "MODIFY"),
            resource =
                mapOf(
                    "collaborators" to
                        listOf(
                            mapOf("id" to "bob", "role" to "viewer", "verified" to true),
                            mapOf("id" to "ada", "role" to "editor", "verified" to false),
                        )),
        )

    assertThat(collaboratorMayEdit.checkAuthorized(request).access).isEqualTo(Access.Granted())
    assertThat(allCollaboratorsVerified.checkAuthorized(request).access).isEqualTo(Access.Denied())
  }

  @Test
  fun `a hand-written policy can explain its denial`() {
    val response =
        BusinessHoursPolicy().checkAuthorized(AccessRequest(environment = mapOf("hour" to 22)))

    assertThat(response.access)
        .isEqualTo(Access.Denied(mapOf("reason" to "Outside business hours")))
  }
}
