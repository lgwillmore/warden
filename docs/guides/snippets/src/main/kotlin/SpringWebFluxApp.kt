package codes.laurence.warden.docs

import codes.laurence.warden.enforce.EnforcementPoint
import codes.laurence.warden.spring.EnforcementPointSpring
import codes.laurence.warden.spring.NotAuthorizedResponse
import codes.laurence.warden.spring.Unwarded
import codes.laurence.warden.spring.WardenRoute
import codes.laurence.warden.spring.WardenRouteBehaviour
import codes.laurence.warden.spring.webflux.WardenWebFilter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

/** Authentication is not Warden's concern. This sample trusts a header. */
class Users(private val users: Map<String, User>) {
  fun current(request: ServerHttpRequest): User =
      users[request.headers.getFirst("X-User")] ?: throw NotAuthenticatedException()
}

@SpringBootApplication(proxyBeanMethods = false) class BlogApplication

@Configuration(proxyBeanMethods = false)
class BlogConfiguration {

  /** Without this bean, the auto-configuration registers a filter with an empty route stack. */
  @Bean
  fun wardenWebFilter() =
      WardenWebFilter(
          listOf(
              // The first matching entry wins. Health checks are open for GET only.
              WardenRoute("/health", WardenRouteBehaviour.IGNORE, setOf(HttpMethod.GET)),
              // Everything else is enforced, which is also the default when nothing matches.
              WardenRoute("/.*", WardenRouteBehaviour.ENFORCE),
          ))

  /** Without this bean, a denial is answered with an empty 403. */
  @Bean
  fun notAuthorizedResponse() = NotAuthorizedResponse {
    ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not Authorized")
  }

  @Bean fun enforcementPoint(): EnforcementPoint = EnforcementPointSpring(articlePolicies)

  @Bean
  fun articleService(articles: ArticleStore, enforcementPoint: EnforcementPoint) =
      ArticleService(articles, enforcementPoint)
}

@RestController
class BlogController(private val service: ArticleService, private val users: Users) {

  // Open through the filter configuration above.
  @GetMapping("/health") fun health() = "ok"

  // Open through the annotation.
  @Unwarded @GetMapping("/about") fun about() = "A blog guarded by Warden"

  // The service is suspending, and so are the handlers. Warden follows the coroutine.
  @GetMapping("/articles")
  suspend fun listArticles(request: ServerHttpRequest): String =
      service.listArticles(users.current(request)).joinToString("\n") { it.title }

  @GetMapping("/articles/{id}")
  suspend fun readArticle(request: ServerHttpRequest, @PathVariable id: String): String =
      service.readArticle(users.current(request), id).title

  @DeleteMapping("/articles/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  suspend fun deleteArticle(request: ServerHttpRequest, @PathVariable id: String) {
    service.deleteArticle(users.current(request), id)
  }

  // Nothing here calls the enforcement point, so Warden answers 403 instead of the text.
  @GetMapping("/drafts") fun drafts() = "Unpublished drafts"
}

@RestControllerAdvice
class BlogErrorHandling {
  @ExceptionHandler(NotAuthenticatedException::class)
  fun notAuthenticated(): ResponseEntity<String> =
      ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Authenticated")

  @ExceptionHandler(NoSuchElementException::class)
  fun notFound(): ResponseEntity<Void> = ResponseEntity.notFound().build()
}
