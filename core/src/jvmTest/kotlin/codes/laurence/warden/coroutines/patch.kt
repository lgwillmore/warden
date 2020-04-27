package codes.laurence.warden.coroutines

import kotlinx.coroutines.runBlocking

actual fun runBlockingTest(testBlock: suspend () -> Unit) {
    runBlocking {
        testBlock()
    }
}