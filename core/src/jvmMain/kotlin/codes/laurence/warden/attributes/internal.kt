package codes.laurence.warden.attributes

internal fun convertToAttributeForm(value: Any?): Any? {
    var converted = value
    if (converted is HasAttributesI) {
        converted = converted.attributes()
    }
    converted = when (converted) {
        is Map<*, *> -> {
            converted.entries.map {
                it.key to when (val nestedValue = it.value) {
                    is HasAttributesI -> nestedValue.attributes()
                    else -> nestedValue
                }
            }.toMap()
        }
        is Collection<*> -> {
            converted.map { valueMember ->
                if (valueMember is HasAttributesI) {
                    valueMember.attributes()
                } else {
                    valueMember
                }
            }
        }
        else -> converted
    }
    return converted
}
