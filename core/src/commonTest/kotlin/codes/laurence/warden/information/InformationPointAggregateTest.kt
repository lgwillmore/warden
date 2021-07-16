package codes.laurence.warden.information

import assertk.assertThat
import assertk.assertions.isSameAs
import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.test.accessRequestFixture
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test

class InformationPointAggregateTest {

    @Test
    fun enrich() = runBlockingTest {
        val originalRequest = accessRequestFixture()
        val enrichedBy1Request = accessRequestFixture()
        val enrichedBy2Request = accessRequestFixture()

        val informationPoint1Mock = mockk<InformationPoint> {
            coEvery { enrich(originalRequest) } returns enrichedBy1Request
        }
        val informationPoint2Mock = mockk<InformationPoint> {
            coEvery { enrich(enrichedBy1Request) } returns enrichedBy2Request
        }
        val testObj = InformationPointAggregate(
            listOf(informationPoint1Mock, informationPoint2Mock)
        )

        val actual = testObj.enrich(originalRequest)

        assertThat(actual).isSameAs(enrichedBy2Request)
    }
}
