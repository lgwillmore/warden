package codes.laurence.warden.spring

/**
 * Lets the annotated handler method, or every handler method of the annotated controller, respond
 * without authorization being enforced. It only takes effect behind a Warden filter.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Unwarded
