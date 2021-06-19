package codes.laurence.warden.information

import codes.laurence.warden.AccessRequest

class InformationPointPassThrough : InformationPoint {

    override suspend fun enrich(request: AccessRequest): AccessRequest {
        return request
    }
}
