package codes.laurence.warden.http4k

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/** What Warden knows about the request a [WardenFilter] is currently passing through. */
internal class WardenCall {
  @Volatile var enforced = false
  @Volatile var ignored = false
}

/** Carries the [WardenCall] into coroutines, which may leave the request thread. */
internal class WardenCoroutineContext(val call: WardenCall) :
    AbstractCoroutineContextElement(WardenCoroutineContext) {
  companion object Key : CoroutineContext.Key<WardenCoroutineContext>
}

/** http4k handles a request on one thread, so the filter publishes the call to that thread. */
internal val currentWardenCall = ThreadLocal<WardenCall?>()

internal inline fun <T> withWardenCall(call: WardenCall, block: () -> T): T {
  val previous = currentWardenCall.get()
  currentWardenCall.set(call)
  try {
    return block()
  } finally {
    if (previous == null) currentWardenCall.remove() else currentWardenCall.set(previous)
  }
}

/** The call of the current coroutine if it carries one, otherwise the one of the current thread. */
internal suspend fun wardenCall(): WardenCall? =
    coroutineContext[WardenCoroutineContext]?.call ?: currentWardenCall.get()
