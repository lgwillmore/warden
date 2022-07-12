package codes.laurence.warden.attributes

/**
 * Lets as us merge the attributes of something that [HasAttributesI] with additional attributes.
 */
fun HasAttributesI.withAttributes(vararg additionalAttributes: Pair<String, Any?>): Attributes {
    return attributes() + additionalAttributes.map { it.first to convertToAttributeForm(it.second) }
}
