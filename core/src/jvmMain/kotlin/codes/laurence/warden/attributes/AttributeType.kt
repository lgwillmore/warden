package codes.laurence.warden.attributes

data class AttributeType(
    val type: String,
    val typeKeyword: String = "attributeType"
) : HasAttributesI {

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

    fun withAttributes(vararg additionalAttributes: Pair<String, Any?>): Attributes {
        val map: MutableMap<String, Any?> = mutableMapOf(typeKeyword to type)
        additionalAttributes.forEach { attribute ->
            map[attribute.first] = convertToAttributeForm(attribute.second)
        }
        return map
    }

    override fun attributes(): Attributes {
        return mapOf(typeKeyword to type)
    }
}
