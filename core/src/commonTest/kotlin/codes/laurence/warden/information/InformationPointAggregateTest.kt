package codes.laurence.warden.information

import codes.laurence.warden.coroutines.runBlockingTest
import codes.laurence.warden.test.accessRequestFixture
import io.mockative.Mock
import io.mockative.classOf
import io.mockative.coEvery
import io.mockative.mock
import kotlin.test.Test
import kotlin.test.assertSame

class InformationPointAggregateTest {

    @Mock
    val informationPoint1Mock = mock(classOf<InformationPoint>())

    @Mock
    val informationPoint2Mock = mock(classOf<InformationPoint>())

    @Test
    fun enrich() =
        runBlockingTest {
            val originalRequest = accessRequestFixture()
            val enrichedBy1Request = accessRequestFixture()
            val enrichedBy2Request = accessRequestFixture()

            coEvery { informationPoint1Mock.enrich(originalRequest) }.returns(enrichedBy1Request)

            coEvery { informationPoint2Mock.enrich(enrichedBy1Request) }.returns(enrichedBy2Request)

            val testObj =
                InformationPointAggregate(
                    listOf(informationPoint1Mock, informationPoint2Mock),
                )

            val actual = testObj.enrich(originalRequest)

            assertSame(enrichedBy2Request, actual)
        }
}
