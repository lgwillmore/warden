package codes.laurence.warden.attributes

data class AttributeType(
    val type: String,
    val typeKeyword: String = "attributeType"
) {

    fun asHasAttributes(additionalAttributes: Attributes): HasAttributesI {
        return HasAttributesMapWrapper(withAttributes(additionalAttributes))
    }

    fun withAttributes(additionalAttributes: Attributes): Attributes {
        val map: MutableMap<String, Any?> = mutableMapOf(typeKeyword to type)
        additionalAttributes.forEach { key, value ->
            map[key] = convertToAttributeForm(value)
        }
        return map
    }
}
