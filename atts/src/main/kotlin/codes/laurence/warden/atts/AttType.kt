package codes.laurence.warden.atts

/**
 * Provides a definition of a "Type" attribute which is a common attribute required for subjects, actions and resources.
 *
 * @param type: The type of the subject, action or resource.
 * @param typeKeyword: Allows aliasing of the type key in order to avoid conflicts with other attributes.
 */
data class AttType(
    val type: String,
    val typeKeyword: String = "attributeType"
) : HasAttsI {

    /**
     * Merge type attribute with provided [Atts] in a new [HasAttsI]
     */
    fun asHasAtts(additionalAtts: Atts): HasAttsI {
        return HasAttsMapWrapper(withAtts(additionalAtts))
    }

    /**
     * Merge type attribute with provided [Atts] in a new set of [Atts]
     */
    fun withAtts(additionalAtts: Atts): Atts {
        val map: MutableMap<String, Any?> = mutableMapOf(typeKeyword to type)
        additionalAtts.forEach { key, value ->
            map[key] = convertToAttsForm(value)
        }
        return map
    }

    /**
     * Merge type attribute with provided pairs in a new set of [Atts]
     */
    fun withAtts(vararg additionalAttributes: Pair<String, Any?>): Atts {
        val map: MutableMap<String, Any?> = mutableMapOf(typeKeyword to type)
        additionalAttributes.forEach { attribute ->
            map[attribute.first] = convertToAttsForm(attribute.second)
        }
        return map
    }

    /**
     * Provides the [Atts] of this type with a single mapping from the [typeKeyword] to the [type].
     */
    override fun atts(): Atts {
        return mapOf(typeKeyword to type)
    }
}
