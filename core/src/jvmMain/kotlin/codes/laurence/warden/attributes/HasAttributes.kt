package codes.laurence.warden.attributes

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
