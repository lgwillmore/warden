package codes.laurence.warden.information

import codes.laurence.warden.AccessRequest

/**
 * A do nothing [InformationPoint] that simply returns the same [AccessRequest].
 */
class InformationPointPassThrough : InformationPoint {

    override suspend fun enrich(request: AccessRequest): AccessRequest {
        return request
    }
}
