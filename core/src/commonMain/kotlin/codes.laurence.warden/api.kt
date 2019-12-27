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
    // The attributes that were originally submitted.
    val originalRequest: AccessRequest,
    // The full set of attributes that were evaluated. This may be more than the submitted attributes if more information was retrieved.
    val enhancedRequest: AccessRequest
)

sealed class Access {
    object Granted : Access()
    data class Denied(
        val denyingPolicyID: String,
        val properties: Map<String, Any?> = emptyMap()
    ) : Access()
}