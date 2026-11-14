package codes.laurence.warden.information

import codes.laurence.warden.test.accessRequestFixture
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertSame
import kotlinx.coroutines.runBlocking

class InformationPointAggregateTest {

  val informationPoint1Mock = mockk<InformationPoint>()

  val informationPoint2Mock = mockk<InformationPoint>()

  @Test
  fun enrich() = runBlocking {
    val originalRequest = accessRequestFixture()
    val enrichedBy1Request = accessRequestFixture()
    val enrichedBy2Request = accessRequestFixture()

    coEvery { informationPoint1Mock.enrich(originalRequest) } returns enrichedBy1Request

    coEvery { informationPoint2Mock.enrich(enrichedBy1Request) } returns enrichedBy2Request

    val testObj =
        InformationPointAggregate(
            listOf(informationPoint1Mock, informationPoint2Mock),
        )

    val actual = testObj.enrich(originalRequest)

    assertSame(enrichedBy2Request, actual)
  }
}
