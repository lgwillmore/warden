package codes.laurence.warden.attributes

data class AttributeType(
    val type: String,
    val typeKeyword: String = "attributeType"
) {

    fun asHasAttributes(additionalAttributes: Map<String, Any?>): HasAttributesI {
        return HasAttributesMapWrapper(withAttributes(additionalAttributes))
    }

    fun withAttributes(additionalAttributes: Map<String, Any?>): Map<String, Any?> {
        val map: MutableMap<String, Any?> = mutableMapOf(typeKeyword to type)
        additionalAttributes.forEach { key, value ->
            map[key] = convertToAttributeMap(value)
        }
        return map
    }
}
