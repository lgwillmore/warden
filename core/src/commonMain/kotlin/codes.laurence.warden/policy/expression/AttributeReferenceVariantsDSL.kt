package codes.laurence.warden.policy.expression

import codes.laurence.warden.policy.collections.toPathSegments

/**
 * SUBJECT
 */
fun subjectVal(arg0: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.SUBJECT,
        arg0, *pathRest
    )
}

fun subjectVal(arg0: String, arg1: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.SUBJECT,
        arg0, arg1, *pathRest
    )
}

fun subjectVal(arg0: String, arg1: String, arg2: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.SUBJECT,
        arg0, arg1, arg2, *pathRest
    )
}

fun subjectVal(
    arg0: String,
    arg1: String,
    arg2: String,
    arg3: AttributeReference,
    vararg pathRest: String
): AttributeReference {
    return AttributeReference(
        AttributeType.SUBJECT,
        arg0, arg1, arg2, arg3, *pathRest
    )
}

/**
 * ACTION
 */

fun actionVal(arg0: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.ACTION,
        arg0, *pathRest
    )
}

fun actionVal(arg0: String, arg1: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.ACTION,
        arg0, arg1, *pathRest
    )
}

fun actionVal(arg0: String, arg1: String, arg2: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.ACTION,
        arg0, arg1, arg2, *pathRest
    )
}

fun actionVal(
    arg0: String,
    arg1: String,
    arg2: String,
    arg3: AttributeReference,
    vararg pathRest: String
): AttributeReference {
    return AttributeReference(
        AttributeType.ACTION,
        arg0, arg1, arg2, arg3, *pathRest
    )
}

/**
 * RESOURCE
 */

fun resourceVal(arg0: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.RESOURCE,
        arg0, *pathRest
    )
}

fun resourceVal(arg0: String, arg1: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.RESOURCE,
        arg0, arg1, *pathRest
    )
}

fun resourceVal(arg0: String, arg1: String, arg2: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.RESOURCE,
        listOf(arg0, arg1).toPathSegments() +
            listOf(AttributePathSegment.AReference(arg2)) + pathRest.toList().toPathSegments()
    )
}

fun resourceVal(
    arg0: String,
    arg1: String,
    arg2: String,
    arg3: AttributeReference,
    vararg pathRest: String
): AttributeReference {
    return AttributeReference(
        AttributeType.RESOURCE,
        arg0, arg1, arg2, arg3, *pathRest
    )
}

/**
 * ENVIRONMENT
 */

fun environmentVal(arg0: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.ENVIRONMENT,
        arg0, *pathRest
    )
}

fun environmentVal(arg0: String, arg1: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.ENVIRONMENT,
        arg0, arg1, *pathRest
    )
}

fun environmentVal(arg0: String, arg1: String, arg2: AttributeReference, vararg pathRest: String): AttributeReference {
    return AttributeReference(
        AttributeType.ENVIRONMENT,
        arg0, arg1, arg2, *pathRest
    )
}

fun environmentVal(
    arg0: String,
    arg1: String,
    arg2: String,
    arg3: AttributeReference,
    vararg pathRest: String
): AttributeReference {
    return AttributeReference(
        AttributeType.ENVIRONMENT,
        arg0, arg1, arg2, arg3, *pathRest
    )
}

