package codes.laurence.warden.information

import codes.laurence.warden.AccessRequest

/**
 * [InformationPoint] allows for the enrichment of [AccessRequest] attributes before being evaluated by a [codes.laurence.warden.decision.DecisionPoint]
 */
interface InformationPoint {

    /**
     * @return Enriched [AccessRequest]
     */
    suspend fun enrich(request: AccessRequest): AccessRequest
}
