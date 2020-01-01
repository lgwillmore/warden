package codes.laurence.warden.decision

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

/**
 * Something that is capable of making a decision as to whether a particular request is Authorized.
 */
interface DecisionPoint{
    suspend fun checkAuthorized(request: AccessRequest): AccessResponse
}