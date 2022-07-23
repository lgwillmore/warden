package codes.laurence.warden.atts

/**
 * Something that has [Atts]
 */
interface HasAttsI {
    fun atts(): Atts
}

/**
 * A Map of key values.
 */
typealias Atts = Map<String, Any?>
