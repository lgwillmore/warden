package codes.laurence.warden.attributes

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
