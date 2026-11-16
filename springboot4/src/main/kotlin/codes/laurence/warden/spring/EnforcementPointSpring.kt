package codes.laurence.warden.spring

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.FilterAccessRequest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.enforce.EnforcementPoint
import codes.laurence.warden.enforce.EnforcementPointDefault
import codes.laurence.warden.policy.Policy
import org.slf4j.LoggerFactory

/**
 * An [EnforcementPoint] that works with the Warden filter of a Spring web adapter.
 *
 * This means that every handler behind the filter must reach an [enforceAuthorization] or
 * [filterAuthorization] call, or be explicitly ignored.
 */
class EnforcementPointSpring(internalEnforcementPoint: EnforcementPoint) : EnforcementPoint {

  companion object {
    private val logger = LoggerFactory.getLogger(EnforcementPointSpring::class.qualifiedName)
  }

  constructor(
      allow: List<Policy>,
      deny: List<Policy> = emptyList()
  ) : this(EnforcementPointDefault(allow, deny))

  constructor(decisionPoint: DecisionPoint) : this(EnforcementPointDefault(decisionPoint))

  private val enforcementPoint: EnforcementPoint = internalEnforcementPoint

  override suspend fun enforceAuthorization(request: AccessRequest) {
    interactWithFilter()
    enforcementPoint.enforceAuthorization(request)
  }

  override suspend fun <RESOURCE> filterAuthorization(
      request: FilterAccessRequest<RESOURCE>
  ): List<RESOURCE> {
    interactWithFilter()
    return enforcementPoint.filterAuthorization(request)
  }

  private suspend fun interactWithFilter() {
    val call = wardenCall()
    if (call == null) {
      logger.debug(
          "Being enforced outside of a Warden filter request, or where the request cannot be reached. Cannot mark the request as enforced")
    } else {
      call.enforced = true
    }
  }
}
