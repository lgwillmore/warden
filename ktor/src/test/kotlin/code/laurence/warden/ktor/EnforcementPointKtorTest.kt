package code.laurence.warden.ktor

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.laurence.warden.FilterAccessRequest
import codes.laurence.warden.ResourceAttributePair
import codes.laurence.warden.enforce.EnforcementPoint
import codes.laurence.warden.test.accessRequestFixture
import codes.laurence.warden.test.attributesFixture
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class EnforcementPointKtorTest {

    @Test
    fun enforceAuthorization() {
        runBlocking {
            val request = accessRequestFixture()
            val internalEnforcementPointMock = mockk<EnforcementPoint> {
                coEvery { enforceAuthorization(request) } returns Unit
            }
            val testObj = EnforcementPointKtor(internalEnforcementPointMock)

            testObj.enforceAuthorization(request)

            coVerify { internalEnforcementPointMock.enforceAuthorization(request) }
        }
    }

    @Test
    fun filterAuthorization() {
        runBlocking {
            val request = FilterAccessRequest(
                subject = attributesFixture(),
                action = attributesFixture(),
                environment = attributesFixture(),
                resources = List(2) { index ->
                    ResourceAttributePair(
                        resource = ArbitraryResource(index),
                        attributesFixture()
                    )
                }
            )
            val result = request.resources.map { it.resource }
            val internalEnforcementPointMock = mockk<EnforcementPoint> {
                coEvery { filterAuthorization(request) } returns result
            }
            val testObj = EnforcementPointKtor(internalEnforcementPointMock)

            val actual = testObj.filterAuthorization(request)

            assertThat(actual).isEqualTo(result)
        }
    }
}

private data class ArbitraryResource(
    val counter: Int
)
