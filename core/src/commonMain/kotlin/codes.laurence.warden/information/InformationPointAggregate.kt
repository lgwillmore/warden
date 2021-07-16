package codes.laurence.warden.information

import codes.laurence.warden.AccessRequest

/**
 * Applies an aggregate of [InformationPoint] enrichment in order to a given [AccessRequest].
 */
class InformationPointAggregate(
    private val informationPoints: List<InformationPoint>
) : InformationPoint {

    override suspend fun enrich(request: AccessRequest): AccessRequest {
        var enriched = request
        informationPoints.forEach { enriched = it.enrich(enriched) }
        return enriched
    }
}
