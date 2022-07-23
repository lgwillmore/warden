package codes.laurence.warden.atts

/**
 * Recursive and Reflective conversion of nested [HasAttsI] properties to [Atts]
 */
internal fun convertToAttsForm(value: Any?): Any? {
    var converted = value
    if (converted is HasAttsI) {
        converted = converted.atts()
    }
    converted = when (converted) {
        is Map<*, *> -> {
            converted.entries.map {
                it.key to when (val nestedValue = it.value) {
                    is HasAttsI -> nestedValue.atts()
                    else -> nestedValue
                }
            }.toMap()
        }
        is Collection<*> -> {
            converted.map { valueMember ->
                if (valueMember is HasAttsI) {
                    valueMember.atts()
                } else {
                    valueMember
                }
            }
        }
        else -> converted
    }
    return converted
}
