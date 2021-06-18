package codes.laurence.warden.information

import codes.laurence.warden.AccessRequest

class InformationProviderPassThrough : InformationProvider {

    override suspend fun enrich(request: AccessRequest): AccessRequest {
        return request
    }
}
