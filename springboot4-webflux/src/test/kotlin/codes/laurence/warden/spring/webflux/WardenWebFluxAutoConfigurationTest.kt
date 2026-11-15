package codes.laurence.warden.spring.webflux

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.enforce.NotAuthorizedException
import codes.laurence.warden.spring.NotAuthorizedResponse
import codes.laurence.warden.spring.WardenNotAuthorizedAdvice
import codes.laurence.warden.spring.WardenRoute
import codes.laurence.warden.spring.WardenRouteBehaviour
import kotlin.test.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class WardenWebFluxAutoConfigurationTest {

  private val runner =
      ReactiveWebApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(WardenWebFluxAutoConfiguration::class.java))

  @Test
  fun `registers a filter with an empty route stack and the forbidden advice`() {
    runner.run { context ->
      assertThat(context.getBean(WardenWebFilter::class.java).routeStack).isEqualTo(emptyList())
      val response = context.getBean(WardenNotAuthorizedAdvice::class.java).handle(denial())
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
      assertThat(response.body).isEqualTo(null)
    }
  }

  @Test
  fun `backs off when the application defines the beans`() {
    runner.withUserConfiguration(UserConfiguration::class.java).run { context ->
      assertThat(context.getBean(WardenWebFilter::class.java)).isSameAs(UserConfiguration.filter)
      val response = context.getBean(WardenNotAuthorizedAdvice::class.java).handle(denial())
      assertThat(response.statusCode).isEqualTo(HttpStatus.I_AM_A_TEAPOT)
    }
  }

  private fun denial() = NotAuthorizedException(AccessRequest(), emptyMap())

  @Configuration(proxyBeanMethods = false)
  class UserConfiguration {
    companion object {
      val filter = WardenWebFilter(listOf(WardenRoute("/health", WardenRouteBehaviour.IGNORE)))
    }

    @Bean fun wardenWebFilter(): WardenWebFilter = filter

    @Bean
    fun notAuthorizedResponse() = NotAuthorizedResponse {
      ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).build<Any>()
    }
  }
}
