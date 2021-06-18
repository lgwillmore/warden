package codes.laurence.warden.information

import codes.laurence.warden.AccessRequest

interface InformationProvider {
    suspend fun enrich(request: AccessRequest): AccessRequest
}
