package codes.laurence.warden.enforce

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class EnforcementPointTest {

    @Test
    fun exception() {

        println(
            NotAuthorizedException(
                AccessResponse(
                    access = Access.Denied("blah"),
                    originalRequest = AccessRequest(),
                    enhancedRequest = AccessRequest()
                )
            )
        )

        assertEquals(1, 1)

    }
}