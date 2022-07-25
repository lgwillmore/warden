package codes.laurence.warden.atts

/**
 * A base implementation of [HasAttsI]. Will build attributes using [attsOf].
 *
 * @param attType: Optional [AttType] that will be added to the attributes defined by object properties.
 */
open class HasAtts(
    private val attType: AttType? = null
) : HasAttsI {

    override fun atts(): Atts {
        val map: MutableMap<String, Any?> =
            if (attType != null) mutableMapOf(attType.typeKeyword to attType.type) else mutableMapOf()
        map.putAll(attsOf(this))
        return map
    }
}
