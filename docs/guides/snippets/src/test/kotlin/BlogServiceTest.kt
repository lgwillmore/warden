package codes.laurence.warden.docs

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import codes.laurence.warden.enforce.EnforcementPointDefault
import codes.laurence.warden.enforce.NotAuthorizedException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.runBlocking

class BlogServiceTest {

  private val ada = User(id = "ada", roles = listOf("Author"))
  private val bob = User(id = "bob", roles = emptyList())
  private val adasArticle = Article(id = "a1", title = "Warden", authorId = "ada")
  private val bobsArticle = Article(id = "b1", title = "Ktor", authorId = "bob")

  private val articles = ArticleStore(listOf(adasArticle, bobsArticle))
  private val service = ArticleService(articles, EnforcementPointDefault(articlePolicies))

  @Test
  fun `a granted request proceeds`() {
    runBlocking {
      val article = service.readArticle(bob, "a1")

      assertThat(article).isEqualTo(adasArticle)
    }
  }

  @Test
  fun `a denied request throws before anything happens`() {
    runBlocking {
      assertFailsWith<NotAuthorizedException> { service.deleteArticle(bob, "a1") }

      assertThat(articles.all()).containsExactly(adasArticle, bobsArticle)
    }
  }

  @Test
  fun `filtering keeps only the resources the user may act on`() {
    runBlocking { assertThat(service.listArticles(bob)).containsExactly(adasArticle, bobsArticle) }
  }
}
