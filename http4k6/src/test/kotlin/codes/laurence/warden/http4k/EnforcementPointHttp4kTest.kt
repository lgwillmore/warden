package codes.laurence.warden.http4k

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import assertk.assertions.isTrue
import codes.laurence.warden.FilterAccessRequest
import codes.laurence.warden.ResourceAttributePair
import codes.laurence.warden.enforce.EnforcementPoint
import codes.laurence.warden.test.accessRequestFixture
import codes.laurence.warden.test.attributesFixture
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class EnforcementPointHttp4kTest {

  @Test
  fun enforceAuthorization() {
    runBlocking {
      val request = accessRequestFixture()
      val internalEnforcementPointMock =
          mockk<EnforcementPoint> { coEvery { enforceAuthorization(request) } returns Unit }
      val testObj = EnforcementPointHttp4k(internalEnforcementPointMock)

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
      val testObj = EnforcementPointHttp4k(internalEnforcementPointMock)

      val actual = testObj.filterAuthorization(request)

      assertThat(actual).isEqualTo(result)
    }
  }

  @Test
  fun `enforceAuthorization - marks the call of the current thread`() {
    val request = accessRequestFixture()
    val testObj =
        EnforcementPointHttp4k(
            mockk<EnforcementPoint> { coEvery { enforceAuthorization(request) } returns Unit })
    val call = WardenCall()

    withWardenCall(call) { runBlocking { testObj.enforceAuthorization(request) } }

    assertThat(call.enforced).isTrue()
  }

  @Test
  fun `enforceAuthorization - marks the call carried by the coroutine on another thread`() {
    val request = accessRequestFixture()
    val testObj =
        EnforcementPointHttp4k(
            mockk<EnforcementPoint> { coEvery { enforceAuthorization(request) } returns Unit })
    val call = WardenCall()

    runBlocking(WardenCoroutineContext(call)) {
      withContext(Dispatchers.IO) { testObj.enforceAuthorization(request) }
    }

    assertThat(call.enforced).isTrue()
  }

  @Test
  fun `enforceAuthorization - no call - still enforces`() {
    val request = accessRequestFixture()
    val internalEnforcementPointMock =
        mockk<EnforcementPoint> { coEvery { enforceAuthorization(request) } returns Unit }
    val testObj = EnforcementPointHttp4k(internalEnforcementPointMock)

    runBlocking { testObj.enforceAuthorization(request) }

    coVerify { internalEnforcementPointMock.enforceAuthorization(request) }
  }

  @Test
  fun enforceAuthorizationBlocking() {
    val request = accessRequestFixture()
    val internalEnforcementPointMock =
        mockk<EnforcementPoint> { coEvery { enforceAuthorization(request) } returns Unit }
    val testObj = EnforcementPointHttp4k(internalEnforcementPointMock)
    val call = WardenCall()

    withWardenCall(call) { testObj.enforceAuthorizationBlocking(request) }

    coVerify { internalEnforcementPointMock.enforceAuthorization(request) }
    assertThat(call.enforced).isTrue()
  }

  @Test
  fun filterAuthorizationBlocking() {
    val request = filterRequestFixture()
    val result = request.resources.map { it.resource }
    val internalEnforcementPointMock =
        mockk<EnforcementPoint> { coEvery { filterAuthorization(request) } returns result }
    val testObj = EnforcementPointHttp4k(internalEnforcementPointMock)
    val call = WardenCall()

    val actual = withWardenCall(call) { testObj.filterAuthorizationBlocking(request) }

    assertThat(actual).isEqualTo(result)
    assertThat(call.enforced).isTrue()
  }

  @Test
  fun `warded - carries the call of the current thread to other dispatchers`() {
    val call = WardenCall()

    val seen = withWardenCall(call) { warded { withContext(Dispatchers.IO) { wardenCall() } } }

    assertThat(seen).isNotNull().isSameAs(call)
  }

  @Test
  fun `warded - no call - runs the block anyway`() {
    val seen = warded { wardenCall() }

    assertThat(seen).isNull()
  }

  @Test
  fun `withWardenCall - restores the previous call`() {
    val outer = WardenCall()

    withWardenCall(outer) {
      withWardenCall(WardenCall()) {}
      assertThat(currentWardenCall.get()).isNotNull().isSameAs(outer)
    }

    assertThat(currentWardenCall.get()).isNull()
  }

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
