package codes.laurence.warden.atts

/**
 * Something that has [Attributes]
 */
interface HasAttributesI {
    fun attributes(): Attributes
}

/**
 * A Map of key values.
 */
typealias Attributes = Map<String, Any?>
