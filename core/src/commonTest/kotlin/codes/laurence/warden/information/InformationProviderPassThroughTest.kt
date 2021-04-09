package codes.laurence.warden.information

import assertk.assertThat
import assertk.assertions.isSameAs
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.coroutines.runBlockingTest
import kotlin.test.Test

class InformationProviderPassThroughTest {

    @Test
    fun enrich() = runBlockingTest {
        val request = AccessRequest(
            subject = mapOf("1" to 1),
            action = mapOf("1" to 2),
            resource = mapOf("1" to 3),
            environment = mapOf("1" to 4),
        )
        val testObj = InformationProviderPassThrough()

        val actual = testObj.enrich(request)

        assertThat(actual).isSameAs(request)

    }

}