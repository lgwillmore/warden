package codes.laurence.warden.atts

/**
 * Wraps a set of [Atts] in a [HasAttsI]
 */
class HasAttsMapWrapper(
    private val atts: Map<String, Any?>
) : HasAttsI {

    override fun atts(): Atts {
        return atts
    }
}
