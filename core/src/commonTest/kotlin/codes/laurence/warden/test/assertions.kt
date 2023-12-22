package codes.laurence.warden.test

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import kotlin.test.assertEquals
import kotlin.test.assertIs

fun assertDenied(
    response: AccessResponse,
    expectedRequest: AccessRequest? = null,
) {
    assertIs<Access.Denied>(response.access)
    expectedRequest?.let {
        assertEquals(expectedRequest, response.request)
    }
}

fun assertGranted(
    response: AccessResponse,
    expectedRequest: AccessRequest? = null,
) {
    assertIs<Access.Granted>(response.access)
    expectedRequest?.let {
        assertEquals(expectedRequest, response.request)
    }
}
