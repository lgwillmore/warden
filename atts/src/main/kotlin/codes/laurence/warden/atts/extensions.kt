package codes.laurence.warden.atts

/**
 * Lets as us merge the attributes of something that [HasAttsI] with additional attributes.
 */
fun HasAttsI.withAtts(vararg additionalAttributes: Pair<String, Any?>): Atts {
    return atts() + additionalAttributes.map { it.first to convertToAttsForm(it.second) }
}
