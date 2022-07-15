package codes.laurence.warden.atts

/**
 * A base implementation of [HasAttributesI]. Will build attributes using [attributesOf].
 *
 * @param attributeType: Optional [AttributeType] that will be added to the attributes defined by object properties.
 */
open class HasAttributes(
    private val attributeType: AttributeType? = null
) : HasAttributesI {

    override fun attributes(): Attributes {
        val map: MutableMap<String, Any?> =
            if (attributeType != null) mutableMapOf(attributeType.typeKeyword to attributeType.type) else mutableMapOf()
        map.putAll(attributesOf(this))
        return map
    }
}
