package codes.laurence.warden.docs

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.policy.bool.AnyOf
import kotlin.test.Test

class BlogPoliciesTest {

  private val ada = User(id = "ada", roles = listOf("Author"))
  private val bob = User(id = "bob", roles = emptyList())
  private val article = Article(id = "a1", title = "Warden", authorId = "ada")

  /**
   * A policy is a plain function of the request; `AnyOf` over the list is what the decision point
   * does.
   */
  private val policy = AnyOf(articlePolicies)

  private fun request(user: User, action: ArticleAction) =
      AccessRequest(subject = user.atts(), action = action.atts(), resource = article.atts())

  @Test
  fun `anyone can read`() {
    assertThat(policy.checkAuthorized(request(bob, ArticleAction.READ)).access)
        .isInstanceOf(Access.Granted::class)
  }

  @Test
  fun `only the author can delete`() {
    assertThat(policy.checkAuthorized(request(ada, ArticleAction.DELETE)).access)
        .isInstanceOf(Access.Granted::class)
    assertThat(policy.checkAuthorized(request(bob, ArticleAction.DELETE)).access)
        .isInstanceOf(Access.Denied::class)
  }

  @Test
  fun `only authors can create`() {
    assertThat(policy.checkAuthorized(request(ada, ArticleAction.CREATE)).access)
        .isEqualTo(Access.Granted())
    assertThat(policy.checkAuthorized(request(bob, ArticleAction.CREATE)).access)
        .isEqualTo(Access.Denied())
  }
}
