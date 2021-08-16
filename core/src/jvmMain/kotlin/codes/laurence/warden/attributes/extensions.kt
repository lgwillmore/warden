package codes.laurence.warden.attributes

fun HasAttributesI.withAttributes(vararg additionalAttributes: Pair<String, Any?>): Attributes {
    return attributes() + additionalAttributes.map { it.first to convertToAttributeForm(it.second) }
}
