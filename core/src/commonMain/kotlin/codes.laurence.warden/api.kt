package codes.laurence.warden

interface AuthorizationDecider {
    fun checkAuthorization(request: AccessRequest): AccessResponse
}

data class AccessRequest(
    val subject: Map<String, Any?> = emptyMap(),
    val action: Map<String, Any?> = emptyMap(),
    val resource: Map<String, Any?> = emptyMap(),
    val environment: Map<String, Any?> = emptyMap()
)

data class AccessResponse(
    val access: Access,
    val request: AccessRequest
)

sealed class Access {
    data class Granted(
        val properties: Map<String, Any?> = emptyMap()
    ) : Access()

    data class Denied(
        val properties: Map<String, Any?> = emptyMap(),
    ) : Access()
}
