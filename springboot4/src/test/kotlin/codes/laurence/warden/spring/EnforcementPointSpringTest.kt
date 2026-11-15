package codes.laurence.warden.spring

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import codes.laurence.warden.FilterAccessRequest
import codes.laurence.warden.ResourceAttributePair
import codes.laurence.warden.enforce.EnforcementPoint
import codes.laurence.warden.test.accessRequestFixture
import codes.laurence.warden.test.attributesFixture
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.web.context.request.AbstractRequestAttributes
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import reactor.util.context.Context

internal class EnforcementPointSpringTest {

  @AfterTest
  fun tearDown() {
    RequestContextHolder.resetRequestAttributes()
  }

  @Test
  fun enforceAuthorization() {
    runBlocking {
      val request = accessRequestFixture()
      val internalEnforcementPointMock =
          mockk<EnforcementPoint> { coEvery { enforceAuthorization(request) } returns Unit }
      val testObj = EnforcementPointSpring(internalEnforcementPointMock)

      testObj.enforceAuthorization(request)

      coVerify { internalEnforcementPointMock.enforceAuthorization(request) }
    }
  }

  @Test
  fun filterAuthorization() {
    runBlocking {
      val request = filterRequestFixture()
      val result = request.resources.map { it.resource }
      val internalEnforcementPointMock =
          mockk<EnforcementPoint> { coEvery { filterAuthorization(request) } returns result }
      val testObj = EnforcementPointSpring(internalEnforcementPointMock)

      val actual = testObj.filterAuthorization(request)

      assertThat(actual).isEqualTo(result)
    }
  }

  @Test
  fun `enforceAuthorization - marks the call carried by the coroutine, on any thread`() {
    val request = accessRequestFixture()
    val testObj = EnforcementPointSpring(grantingEnforcementPoint(request))
    val call = WardenCall()

    runBlocking(WardenCoroutineContext(call)) {
      withContext(Dispatchers.IO) { testObj.enforceAuthorization(request) }
    }

    assertThat(call.enforced).isTrue()
  }

  @Test
  fun `enforceAuthorization - marks the call carried by the Reactor context`() {
    val request = accessRequestFixture()
    val testObj = EnforcementPointSpring(grantingEnforcementPoint(request))
    val call = WardenCall()

    runBlocking(ReactorContext(Context.of(WardenCall.REACTOR_CONTEXT_KEY, call))) {
      testObj.enforceAuthorization(request)
    }

    assertThat(call.enforced).isTrue()
  }

  @Test
  fun `enforceAuthorization - marks the call of the request bound to the thread`() {
    val request = accessRequestFixture()
    val testObj = EnforcementPointSpring(grantingEnforcementPoint(request))
    val call = WardenCall()
    val attributes = MapRequestAttributes()
    attributes.setAttribute(WardenCall.REQUEST_ATTRIBUTE, call, RequestAttributes.SCOPE_REQUEST)
    RequestContextHolder.setRequestAttributes(attributes)

    runBlocking { testObj.enforceAuthorization(request) }

    assertThat(call.enforced).isTrue()
  }

  @Test
  fun `enforceAuthorization - no call - still enforces`() {
    val request = accessRequestFixture()
    val internalEnforcementPointMock = grantingEnforcementPoint(request)
    val testObj = EnforcementPointSpring(internalEnforcementPointMock)

    runBlocking { testObj.enforceAuthorization(request) }

    coVerify { internalEnforcementPointMock.enforceAuthorization(request) }
  }

  @Test
  fun `wardenCall - nothing in reach - null`() {
    val seen = runBlocking { wardenCall() }

    assertThat(seen).isNull()
  }

  private fun grantingEnforcementPoint(request: codes.laurence.warden.AccessRequest) =
      mockk<EnforcementPoint> { coEvery { enforceAuthorization(request) } returns Unit }

  private fun filterRequestFixture() =
      FilterAccessRequest(
          subject = attributesFixture(),
          action = attributesFixture(),
          environment = attributesFixture(),
          resources =
              List(2) { index ->
                ResourceAttributePair(resource = ArbitraryResource(index), attributesFixture())
              })
}

private data class ArbitraryResource(val counter: Int)

/** Stands in for the request Spring binds to a servlet thread. */
private class MapRequestAttributes : AbstractRequestAttributes() {
  private val attributes = mutableMapOf<String, Any>()

  override fun getAttribute(name: String, scope: Int): Any? = attributes[name]

  override fun setAttribute(name: String, value: Any, scope: Int) {
    attributes[name] = value
  }

  override fun removeAttribute(name: String, scope: Int) {
    attributes.remove(name)
  }

  override fun getAttributeNames(scope: Int): Array<String> = attributes.keys.toTypedArray()

  override fun registerDestructionCallback(name: String, callback: Runnable, scope: Int) {}

  override fun resolveReference(key: String): Any? = null

  override fun getSessionId(): String = "none"

  override fun getSessionMutex(): Any = this

  override fun updateAccessedSessionAttributes() {}
}
