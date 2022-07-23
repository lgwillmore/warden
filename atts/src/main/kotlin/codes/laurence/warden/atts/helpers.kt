package codes.laurence.warden.atts

import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties

/**
 * A helper that that will use reflection to convert the properties of an instance to [Atts].
 */
fun attsOf(thing: Any): Atts {
    val attributes: MutableMap<String, Any?> = mutableMapOf()
    for (prop in thing::class.declaredMemberProperties) {
        if (prop.visibility == KVisibility.PUBLIC) {
            var value = prop.getter.call(thing)
            value = convertToAttsForm(value)
            attributes[prop.name] = value
        }
    }
    return attributes
}
