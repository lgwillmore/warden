package code.laurence.warden.ktor

import code.laurence.warden.ktor.Warden.Feature.WARDEN_ENFORCED
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.enforce.EnforcementPoint
import codes.laurence.warden.enforce.EnforcementPointDefault
import codes.laurence.warden.policy.Policy
import org.slf4j.LoggerFactory
import kotlin.coroutines.coroutineContext

/**
 * An [EnforcementPoint] that works with the Ktor [Warden] feature.
 *
 * This means that all routes must have an [enforceAuthorization] call or be explicitly ignored.
 */
class EnforcementPointKtor(decisionPoint: DecisionPoint) : EnforcementPoint {

    companion object {
        private val logger = LoggerFactory.getLogger(EnforcementPointKtor::class.qualifiedName)
    }

    constructor(policies: List<Policy>) : this(DecisionPoint(policies))

    private val enforcementPoint: EnforcementPoint = EnforcementPointDefault(decisionPoint)

    override suspend fun enforceAuthorization(request: AccessRequest) {
        val call = coroutineContext[WardenKtorCall.Key]?.call
        if (call == null) {
            logger.debug("Being enforced outside of a `wardenCall` block. Cannot communicate with Warden ktor plugin")
        } else {
            call.attributes.put(WARDEN_ENFORCED, true)
        }
        enforcementPoint.enforceAuthorization(request)
    }
}

class KtorEnforcementPointException(msg: String) : Exception(msg)
