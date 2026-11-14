package codes.laurence.warden.docs

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlin.test.Test
import kotlin.test.fail

class KtorAppTest {

  private val users =
      mapOf(
          "ada" to User(id = "ada", roles = listOf("Author")),
          "bob" to User(id = "bob", roles = emptyList()),
      )
  private val articles = listOf(Article(id = "a1", title = "Warden", authorId = "ada"))

  @Test
  fun `a route that enforces authorization responds normally`() = testApplication {
    application { blogModule(users, ArticleStore(articles)) }

    val response = client.get("/articles/a1") { header("X-User", "bob") }

    assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    assertThat(response.bodyAsText()).isEqualTo("Warden")
  }

  @Test
  fun `a denied request gets the status from StatusPages`() = testApplication {
    application { blogModule(users, ArticleStore(articles)) }

    val response = client.delete("/articles/a1") { header("X-User", "bob") }

    assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    assertThat(response.bodyAsText()).isEqualTo("Not Authorized")
  }

  @Test
  fun `a route that forgets to enforce authorization is refused by the plugin`() = testApplication {
    application { blogModule(users, ArticleStore(articles)) }

    val response = client.get("/drafts") { header("X-User", "ada") }

    assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    assertThat(response.bodyAsText()).isEqualTo("Not Authorized: EnforcementPoint not enforced")
  }

  @Test
  fun `routes left open respond without authorization`() = testApplication {
    application { blogModule(users, ArticleStore(articles)) }

    assertThat(client.get("/health").status).isEqualTo(HttpStatusCode.OK)
    assertThat(client.get("/about").status).isEqualTo(HttpStatusCode.OK)
  }

  @Test
  fun `a websocket is authorized before the upgrade`() = testApplication {
    application { blogModule(users, ArticleStore(articles)) }
    val client = createClient { install(WebSockets) }

    client.webSocket("/feed", { header("X-User", "bob") }) {
      assertThat((incoming.receive() as Frame.Text).readText()).isEqualTo("Warden")
    }
    try {
      client.webSocket("/feed") {}
      fail("The handshake should fail without a user")
    } catch (_: Exception) {}
  }
}
