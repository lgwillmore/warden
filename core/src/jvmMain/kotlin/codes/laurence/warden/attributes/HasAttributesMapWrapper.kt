package codes.laurence.warden.attributes

/**
 * Wraps a set of [Attributes] in a [HasAttributesI]
 */
class HasAttributesMapWrapper(
    private val attributes: Map<String, Any?>
) : HasAttributesI {

    override fun attributes(): Attributes {
        return attributes
    }
}
