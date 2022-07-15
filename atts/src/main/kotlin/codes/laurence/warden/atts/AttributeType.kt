package codes.laurence.warden.atts

/**
 * Provides a definition of a "Type" attribute which is a common attribute required for subjects, actions and resources.
 *
 * @param type: The type of the subject, action or resource.
 * @param typeKeyword: Allows aliasing of the type key in order to avoid conflicts with other attributes.
 */
data class AttributeType(
    val type: String,
    val typeKeyword: String = "attributeType"
) : HasAttributesI {

    /**
     * Merge type attribute with provided [Attributes] in a new [HasAttributesI]
     */
    fun asHasAttributes(additionalAttributes: Attributes): HasAttributesI {
        return HasAttributesMapWrapper(withAttributes(additionalAttributes))
    }

    /**
     * Merge type attribute with provided [Attributes] in a new set of [Attributes]
     */
    fun withAttributes(additionalAttributes: Attributes): Attributes {
        val map: MutableMap<String, Any?> = mutableMapOf(typeKeyword to type)
        additionalAttributes.forEach { key, value ->
            map[key] = convertToAttributeForm(value)
        }
        return map
    }

    /**
     * Merge type attribute with provided pairs in a new set of [Attributes]
     */
    fun withAttributes(vararg additionalAttributes: Pair<String, Any?>): Attributes {
        val map: MutableMap<String, Any?> = mutableMapOf(typeKeyword to type)
        additionalAttributes.forEach { attribute ->
            map[attribute.first] = convertToAttributeForm(attribute.second)
        }
        return map
    }

    /**
     * Provides the [Attributes] of this type with a single mapping from the typeKeyword to the type.
     */
    override fun attributes(): Attributes {
        return mapOf(typeKeyword to type)
    }
}
