package codes.laurence.warden.decision

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse

class InMemoryDecisionPoint: DecisionPoint {

    override suspend fun checkAuthorized(request: AccessRequest): AccessResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}