package codes.laurence.warden.decision

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

interface DecisionPoint{
    suspend fun checkAuthorization(request: AccessRequest): AccessResponse
}