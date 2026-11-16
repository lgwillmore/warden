package codes.laurence.warden.spring

import codes.laurence.warden.enforce.NotAuthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/** Decides what a denied request is answered with. */
fun interface NotAuthorizedResponse {
  fun toResponse(cause: NotAuthorizedException): ResponseEntity<*>

  companion object {
    /** `403` with no body. */
    val FORBIDDEN = NotAuthorizedResponse {
      ResponseEntity.status(HttpStatus.FORBIDDEN).build<Any>()
    }
  }
}

/**
 * Turns the [NotAuthorizedException] an enforcement point throws into a response, `403` with no
 * body unless [response] says otherwise. Without it, or an exception handler like it, a denial is
 * answered as an unhandled exception.
 */
@RestControllerAdvice
class WardenNotAuthorizedAdvice(
    private val response: NotAuthorizedResponse = NotAuthorizedResponse.FORBIDDEN
) {

  @ExceptionHandler(NotAuthorizedException::class)
  fun handle(cause: NotAuthorizedException): ResponseEntity<*> = response.toResponse(cause)
}
