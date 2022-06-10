package codes.laurence.warden.enforce

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessRequestBatch
import codes.laurence.warden.FilterAccessRequest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.decision.DecisionPointLocal
import codes.laurence.warden.policy.Policy

class EnforcementPointDefault(val decisionPoint: DecisionPoint) : EnforcementPoint {

    constructor(allow: List<Policy>, deny: List<Policy> = emptyList()) : this(DecisionPointLocal(allow, deny))

    override suspend fun enforceAuthorization(request: AccessRequest) {
        val response = decisionPoint.checkAuthorized(request)
        when (val access = response.access) {
            is Access.Denied -> {
                throw NotAuthorizedException(response.request, access.properties)
            }
            else -> {}
        }
    }

    override suspend fun <RESOURCE> filterAuthorization(request: FilterAccessRequest<RESOURCE>): List<RESOURCE> {
        val decisions = decisionPoint.checkAuthorizedBatch(
            AccessRequestBatch(
                subject = request.subject,
                action = request.action,
                environment = request.environment,
                resources = request.resources.map { it.attributes }
            )
        )
        return request.resources.mapIndexedNotNull { index, resource ->
            if (decisions[index].access is Access.Granted) {
                resource.resource
            } else {
                null
            }
        }
    }
}
