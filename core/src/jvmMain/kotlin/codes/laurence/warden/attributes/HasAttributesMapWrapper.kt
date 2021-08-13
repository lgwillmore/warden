package codes.laurence.warden.attributes

class HasAttributesMapWrapper(
    private val attributes: Map<String, Any?>
) : HasAttributesI {

    override fun attributes(): Map<String, Any?> {
        return attributes
    }
}
