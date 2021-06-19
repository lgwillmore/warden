package codes.laurence.warden.information

import codes.laurence.warden.AccessRequest

interface InformationPoint {
    suspend fun enrich(request: AccessRequest): AccessRequest
}
