package codes.laurence.warden.docs

import kotlin.test.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@Import(SpringWebFluxAppTest.Fixtures::class)
class SpringWebFluxAppTest(@Autowired context: ApplicationContext) {

  /** The sample's users and articles. A real application would load them from somewhere. */
  @TestConfiguration(proxyBeanMethods = false)
  class Fixtures {
    @Bean
    fun users() =
        Users(
            mapOf(
                "ada" to User(id = "ada", roles = listOf("Author")),
                "bob" to User(id = "bob", roles = emptyList()),
            ))

    @Bean
    fun articles() = ArticleStore(listOf(Article(id = "a1", title = "Warden", authorId = "ada")))
  }

  // The whole application runs in process, without a server.
  private val client = WebTestClient.bindToApplicationContext(context).build()

  @Test
  fun `a route that enforces authorization responds normally`() {
    client
        .get()
        .uri("/articles/a1")
        .header("X-User", "bob")
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(String::class.java)
        .isEqualTo("Warden")
  }

  @Test
  fun `a denied request gets the response from NotAuthorizedResponse`() {
    client
        .delete()
        .uri("/articles/a1")
        .header("X-User", "bob")
        .exchange()
        .expectStatus()
        .isForbidden
        .expectBody(String::class.java)
        .isEqualTo("Not Authorized")
  }

  @Test
  fun `a route that forgets to enforce authorization is refused by the filter`() {
    client
        .get()
        .uri("/drafts")
        .header("X-User", "ada")
        .exchange()
        .expectStatus()
        .isForbidden
        .expectBody(String::class.java)
        .isEqualTo("Not Authorized: EnforcementPoint not enforced")
  }

  @Test
  fun `routes left open respond without authorization`() {
    client.get().uri("/health").exchange().expectStatus().isOk
    client.get().uri("/about").exchange().expectStatus().isOk
  }
}
