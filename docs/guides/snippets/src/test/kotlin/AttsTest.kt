package codes.laurence.warden.docs

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class AttsTest {

  private val ada = Account(id = "ada", email = "ada@example.com", verified = true)

  @Test
  fun `HasAtts maps public properties and adds the type`() {
    assertThat(ada.atts())
        .isEqualTo(
            mapOf(
                "type" to "Account",
                "id" to "ada",
                "email" to "ada@example.com",
                "verified" to true,
            ))
  }

  @Test
  fun `attsOf does the same for a class with another superclass`() {
    val signup = Signup(email = "bob@example.com", handle = "bob")

    assertThat(signup.atts())
        .isEqualTo(mapOf("type" to "Account", "email" to "bob@example.com", "handle" to "bob"))
  }

  @Test
  fun `nested HasAttsI values become nested maps`() {
    val team = Team(name = "Docs", owner = ada, members = listOf(ada))

    assertThat(team.atts())
        .isEqualTo(mapOf("name" to "Docs", "owner" to ada.atts(), "members" to listOf(ada.atts())))
  }

  @Test
  fun `attributes can be merged and built without an instance`() {
    assertThat(accountInSession(ada, "s1")).isEqualTo(ada.atts() + ("sessionId" to "s1"))
    assertThat(accountReference("ada").atts()).isEqualTo(mapOf("type" to "Account", "id" to "ada"))
    assertThat(accountFromClaims(mapOf("sub" to "ada")).atts()).isEqualTo(mapOf("sub" to "ada"))
    assertThat(AccountType.atts()).isEqualTo(mapOf("type" to "Account"))
  }
}
