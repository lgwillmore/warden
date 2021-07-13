package codes.laurence.warden.decision

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessRequestBatch
import codes.laurence.warden.AccessResponse

/**
 * Something that is capable of making a decision as to whether a particular request is Authorized.
 */
interface DecisionPoint {
    suspend fun checkAuthorized(request: AccessRequest): AccessResponse

    /**
     * A batch check of access requests for a collection of resources.
     *
     * @return The corresponding [AccessResponse] for each resource.
     */
    suspend fun checkAuthorizedBatch(request: AccessRequestBatch): List<AccessResponse>
}
