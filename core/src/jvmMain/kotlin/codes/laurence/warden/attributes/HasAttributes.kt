package codes.laurence.warden.attributes

import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties

open class HasAttributes(
    private val attributeType: AttributeType? = null
) : HasAttributesI {

    override fun attributes(): Map<String, Any?> {
        val map: MutableMap<String, Any?> =
            if (attributeType != null) mutableMapOf(attributeType.typeKeyword to attributeType.type) else mutableMapOf()
        map.putAll(allProps())
        return map
    }

    private fun allProps(): Map<String, Any?> {
        val attributes: MutableMap<String, Any?> = mutableMapOf()
        for (prop in this::class.declaredMemberProperties) {
            if (prop.visibility == KVisibility.PUBLIC) {
                var value = prop.getter.call(this)
                value = convertToAttributeMap(value)
                attributes[prop.name] = value
            }
        }
        return attributes
    }
}
