package codes.laurence.warden.attributes

import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties

open class HasAttributes(
    private val type: String? = null,
    private val typeKeyword: String = "type"
) : HasAttributesI {

    override fun toAttributes(): Map<String, Any?> {
        val map: MutableMap<String, Any?> = if (type != null) mutableMapOf(typeKeyword to type) else mutableMapOf()
        map.putAll(allProps())
        return map
    }

    private fun allProps(): Map<String, Any?> {
        val attributes: MutableMap<String, Any?> = mutableMapOf()
        for (prop in this::class.declaredMemberProperties) {
            if (prop.visibility == KVisibility.PUBLIC) {
                var value = prop.getter.call(this)
                if (value is HasAttributesI) {
                    value = value.toAttributes()
                }
                value = when (value) {
                    is Map<*, *> -> {
                        value.entries.map {
                            it.key to when (val value = it.value) {
                                is HasAttributesI -> value.toAttributes()
                                else -> value
                            }
                        }.toMap()
                    }
                    is Collection<*> -> {
                        value.map { valueMember ->
                            if (valueMember is HasAttributesI) {
                                valueMember.toAttributes()
                            } else {
                                valueMember
                            }
                        }
                    }
                    else -> value
                }
                attributes[prop.name] = value
            }
        }
        return attributes
    }
}
