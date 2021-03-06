package codes.laurence.warden

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

data class AccessRequestBatch(
    val subject: Map<String, Any?> = emptyMap(),
    val action: Map<String, Any?> = emptyMap(),
    val resources: List<Map<String, Any?>> = emptyList(),
    val environment: Map<String, Any?> = emptyMap()
)

data class FilterAccessRequest<RESOURCE>(
    val subject: Map<String, Any?> = emptyMap(),
    val action: Map<String, Any?> = emptyMap(),
    val resources: List<ResourceAttributePair<RESOURCE>> = emptyList(),
    val environment: Map<String, Any?> = emptyMap()
)

data class ResourceAttributePair<RESOURCE>(
    val resource: RESOURCE,
    val attributes: Map<String, Any?>
)

sealed class Access {
    data class Granted(
        val properties: Map<String, Any?> = emptyMap()
    ) : Access()

    data class Denied(
        val properties: Map<String, Any?> = emptyMap(),
    ) : Access()
}
