package codes.laurence.warden.spring.webflux

import codes.laurence.warden.spring.NotAuthorizedResponse
import codes.laurence.warden.spring.WardenNotAuthorizedAdvice
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean

/**
 * Installs Warden in a reactive Spring Boot application: a [WardenWebFilter] with an empty route
 * stack and a [WardenNotAuthorizedAdvice] answering denials with [NotAuthorizedResponse.FORBIDDEN].
 * Define a bean of the same type to configure any of the three.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class WardenWebFluxAutoConfiguration {

  @Bean @ConditionalOnMissingBean fun wardenWebFilter(): WardenWebFilter = WardenWebFilter()

  @Bean
  @ConditionalOnMissingBean
  fun wardenNotAuthorizedResponse(): NotAuthorizedResponse = NotAuthorizedResponse.FORBIDDEN

  @Bean
  @ConditionalOnMissingBean
  fun wardenNotAuthorizedAdvice(response: NotAuthorizedResponse): WardenNotAuthorizedAdvice =
      WardenNotAuthorizedAdvice(response)
}
