package codes.laurence.warden.attributes

interface HasAttributesI {
    fun attributes(): Attributes
}

typealias Attributes = Map<String, Any?>
