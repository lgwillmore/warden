package codes.laurence.warden.docs

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status

class Http4kAppTest {

  private val users =
      mapOf(
          "ada" to User(id = "ada", roles = listOf("Author")),
          "bob" to User(id = "bob", roles = emptyList()),
      )
  private val articles = listOf(Article(id = "a1", title = "Warden", authorId = "ada"))

  // http4k handlers are functions, so the whole application is tested in memory.
  private val app = blogApp(users, ArticleStore(articles))

  @Test
  fun `a route that enforces authorization responds normally`() {
    val response = app(Request(GET, "/articles/a1").header("X-User", "bob"))

    assertThat(response.status).isEqualTo(Status.OK)
    assertThat(response.bodyString()).isEqualTo("Warden")
  }

  @Test
  fun `a denied request gets the response from CatchNotAuthorized`() {
    val response = app(Request(DELETE, "/articles/a1").header("X-User", "bob"))

    assertThat(response.status).isEqualTo(Status.FORBIDDEN)
    assertThat(response.bodyString()).isEqualTo("Not Authorized")
  }

  @Test
  fun `a route that forgets to enforce authorization is refused by the filter`() {
    val response = app(Request(GET, "/drafts").header("X-User", "ada"))

    assertThat(response.status).isEqualTo(Status.FORBIDDEN)
    assertThat(response.bodyString()).isEqualTo("Not Authorized: EnforcementPoint not enforced")
  }

  @Test
  fun `routes left open respond without authorization`() {
    assertThat(app(Request(GET, "/health")).status).isEqualTo(Status.OK)
    assertThat(app(Request(GET, "/about")).status).isEqualTo(Status.OK)
  }
}
