package codes.laurence.warden.docs

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import kotlin.test.Test
import kotlinx.coroutines.runBlocking

class DecisionsTest {

  private val roles = mapOf("ada" to listOf("Author"))
  private var hour = 10
  private val decisionPoint =
      blogDecisionPoint(rolesOf = { roles[it] ?: emptyList() }, hourOfDay = { hour })

  /** The subject carries only its type and ID; the information point adds the roles. */
  private val adaById = UserType.withAtts("id" to "ada")

  @Test
  fun `the information point supplies what the policy needs`() {
    runBlocking {
      val draft = Article(id = "a2", title = "Draft", authorId = "ada")
      val response =
          decisionPoint.checkAuthorized(
              AccessRequest(
                  subject = adaById,
                  action = ArticleAction.CREATE.atts(),
                  resource = draft.atts(),
              ))

      assertThat(response.access).isInstanceOf(Access.Granted::class)
      assertThat(response.request.subject["roles"]).isEqualTo(listOf("Author"))
    }
  }

  @Test
  fun `a deny policy overrides an allow policy`() {
    runBlocking {
      hour = 22
      val article = Article(id = "a1", title = "Warden", authorId = "ada")
      val response =
          decisionPoint.checkAuthorized(
              AccessRequest(
                  subject = adaById,
                  action = ArticleAction.DELETE.atts(),
                  resource = article.atts(),
              ))

      assertThat(response.access).isInstanceOf(Access.Denied::class)
    }
  }

  @Test
  fun `a resource type without policies is denied`() {
    runBlocking {
      val response =
          decisionPoint.checkAuthorized(
              AccessRequest(
                  subject = adaById,
                  action = ArticleAction.READ.atts(),
                  resource = mapOf("type" to "Comment"),
              ))

      assertThat(response.access).isInstanceOf(Access.Denied::class)
    }
  }
}
