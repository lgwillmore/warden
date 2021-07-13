package codes.laurence.warden.test

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.policy.expression.AttributeReference
import codes.laurence.warden.policy.expression.ExpressionPolicy
import codes.laurence.warden.policy.expression.PassThroughReference
import codes.laurence.warden.policy.expression.ValueReference

fun expressionPolicyFixture(): ExpressionPolicy {
    return ExpressionPolicy(
        leftOperand = valueReferenceFixture(),
        operatorType = randEnum(),
        rightOperand = valueReferenceFixture()
    )
}

fun valueReferenceFixture(): ValueReference {
    return listOf(
        AttributeReference(
            type = randEnum(),
            path = listOf(randString())
        ),
        PassThroughReference(randString())
    ).random()
}

fun accessRequestFixture(): AccessRequest {
    return AccessRequest(
        subject = attributesFixture(),
        action = attributesFixture(),
        resource = attributesFixture(),
        environment = attributesFixture(),
    )
}

fun attributesFixture(): Map<String, Any?> = mapOf(randString() to randString())
