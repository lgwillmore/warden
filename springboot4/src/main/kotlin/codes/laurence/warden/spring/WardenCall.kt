package codes.laurence.warden.spring

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

/**
 * What Warden knows about the request a Warden filter is currently passing through. A filter
 * creates one per request and publishes it where [EnforcementPointSpring] can find it; application
 * code does not touch it.
 */
class WardenCall {
  @Volatile var enforced = false
  @Volatile var ignored = false

  companion object {
    /** The name under which a servlet filter stores the call as a request attribute. */
    const val REQUEST_ATTRIBUTE = "codes.laurence.warden.spring.WardenCall"

    /** The key under which a reactive filter stores the call in the Reactor context. */
    val REACTOR_CONTEXT_KEY: Any = WardenCall::class.java

    /** The fixed body of the 403 a filter answers when a successful response was never enforced. */
    const val NOT_ENFORCED_MESSAGE = "Not Authorized: EnforcementPoint not enforced"
  }
}

/** Carries the [WardenCall] through a coroutine, which may leave the request thread. */
class WardenCoroutineContext(val call: WardenCall) :
    AbstractCoroutineContextElement(WardenCoroutineContext) {
  companion object Key : CoroutineContext.Key<WardenCoroutineContext>
}

/**
 * The call of the current request, or null when none is in reach. Looks in the coroutine context
 * first, then the Reactor context the coroutine was started from, then the request Spring binds to
 * the current servlet thread.
 */
suspend fun wardenCall(): WardenCall? {
  val context = coroutineContext
  return context[WardenCoroutineContext]?.call
      ?: ReactorLookup.call(context)
      ?: RequestContextHolder.getRequestAttributes()
          ?.getAttribute(WardenCall.REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)
          as? WardenCall
}

/**
 * Reads the Reactor context without loading Reactor classes in applications that do not have them,
 * such as a Spring MVC application without WebFlux on the classpath.
 */
private object ReactorLookup {
  private val present =
      try {
        Class.forName("kotlinx.coroutines.reactor.ReactorContext", false, javaClass.classLoader)
        true
      } catch (_: ClassNotFoundException) {
        false
      }

  fun call(context: CoroutineContext): WardenCall? =
      if (present) ReactorContextLookup.call(context) else null
}

private object ReactorContextLookup {
  fun call(context: CoroutineContext): WardenCall? =
      context[kotlinx.coroutines.reactor.ReactorContext]
          ?.context
          ?.getOrEmpty<WardenCall>(WardenCall.REACTOR_CONTEXT_KEY)
          ?.orElse(null)
}
