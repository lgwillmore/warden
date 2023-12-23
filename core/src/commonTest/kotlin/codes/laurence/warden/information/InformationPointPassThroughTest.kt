package codes.laurence.warden.information

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.coroutines.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertSame

class InformationPointPassThroughTest {
    @Test
    fun enrich() =
        runBlockingTest {
            val request =
                AccessRequest(
                    subject = mapOf("1" to 1),
                    action = mapOf("1" to 2),
                    resource = mapOf("1" to 3),
                    environment = mapOf("1" to 4),
                )
            val testObj = InformationPointPassThrough()

            val actual = testObj.enrich(request)

            assertSame(request, actual)
        }
}
