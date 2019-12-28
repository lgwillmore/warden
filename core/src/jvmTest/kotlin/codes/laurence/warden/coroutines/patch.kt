package codes.laurence.warden.coroutines

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.enforce.NotAuthorizedException
import kotlinx.coroutines.runBlocking

actual fun runBlockingTest(testBlock: suspend ()->Unit){
    runBlocking{
        testBlock()
    }
}