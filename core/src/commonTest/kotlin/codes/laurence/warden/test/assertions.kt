package codes.laurence.warden.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

fun assertDenied(response: AccessResponse, expectedRequest: AccessRequest? = null) {
    assertThat(response.access).isInstanceOf(Access.Denied::class)
    expectedRequest?.let {
        assertThat(response.request).isEqualTo(expectedRequest)
    }
}

fun assertGranted(response: AccessResponse, expectedRequest: AccessRequest? = null) {
    assertThat(response.access).isInstanceOf(Access.Granted::class)
    expectedRequest?.let {
        assertThat(response.request).isEqualTo(expectedRequest)
    }
}

