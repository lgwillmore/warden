package codes.laurence.warden.attributes

import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties

fun attributesOf(thing: Any): Attributes {
    val attributes: MutableMap<String, Any?> = mutableMapOf()
    for (prop in thing::class.declaredMemberProperties) {
        if (prop.visibility == KVisibility.PUBLIC) {
            var value = prop.getter.call(thing)
            value = convertToAttributeForm(value)
            attributes[prop.name] = value
        }
    }
    return attributes
}
