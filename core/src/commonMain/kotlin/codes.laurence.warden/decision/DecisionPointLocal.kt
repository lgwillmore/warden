package codes.laurence.warden.decision

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessRequestBatch
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.information.InformationPoint
import codes.laurence.warden.information.InformationPointPassThrough
import codes.laurence.warden.policy.Policy
import codes.laurence.warden.policy.PolicySource
import codes.laurence.warden.policy.PolicySourceInMemory
import codes.laurence.warden.policy.boolean.AnyOf

/**
 * An in memory Decision point.
 *
 * Operates on a access list and deny list basis:
 *  - if there is not policy that grants access, there is no access.
 *  - if there is a policy that grants access, but the is also a policy that denies access, there is no access.
 *  - if there is a policy that grants access and no policy that denies, there is access.
 */
class DecisionPointLocal(
    private val policySource: PolicySource,
    private val informationPoint: InformationPoint = InformationPointPassThrough()
) : DecisionPoint {

    constructor(
        allow: List<Policy>,
        deny: List<Policy> = emptyList(),
        informationProvider: InformationPoint = InformationPointPassThrough()
    ) : this(
        policySource = PolicySourceInMemory(
            allow,
            deny
        ),
        informationPoint = informationProvider
    )

    override suspend fun checkAuthorized(request: AccessRequest): AccessResponse {
        val enriched = informationPoint.enrich(request)
        val policies = policySource.policies(request)
        val anyOfAccessContainer = AnyOf(policies.allow)
        val anyOfDenyContainer = AnyOf(policies.deny)
        val accessResult = anyOfAccessContainer.checkAuthorized(enriched)
        return when (accessResult.access) {
            is Access.Granted -> {
                val denyResult = anyOfDenyContainer.checkAuthorized(enriched)
                when (denyResult.access) {
                    is Access.Granted -> accessResult.copy(
                        access = Access.Denied()
                    )
                    is Access.Denied -> accessResult
                }
            }
            is Access.Denied -> {
                accessResult
            }
        }
    }

    override suspend fun checkAuthorizedBatch(request: AccessRequestBatch): List<AccessResponse> {
        val baseRequest = AccessRequest(
            subject = request.subject,
            action = request.action,
            environment = request.environment,
            resource = emptyMap()
        )
        return request.resources.map { resource ->
            checkAuthorized(baseRequest.copy(resource = resource))
        }
    }
}
