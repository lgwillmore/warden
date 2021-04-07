package codes.laurence.warden.policy.members

import codes.laurence.warden.policy.PolicyDSL
import codes.laurence.warden.policy.expression.ExpressionPolicy
import codes.laurence.warden.policy.expression.OperatorBuilderBase

@PolicyDSL
class MemberBuilder {

    internal val memberPolicies = mutableListOf<MemberPolicy>()

    fun attribute(pathRoot: String, vararg pathRest: String) = OperatorBuilderBase(
        MemberAttributeReference(listOf(pathRoot) + pathRest.toList())
    ) { policy ->
        if (policy is ExpressionPolicy) {
            memberPolicies.add(
                MemberExpressionPolicy(
                    leftOperand = MemberAttributeReference(listOf(pathRoot) + pathRest.toList()),
                    operatorType = policy.operatorType,
                    rightOperand = policy.rightOperand
                )
            )
        } else {
            throw IllegalStateException("Unhandled Policy Type ${policy::class.simpleName}")
        }
    }

}