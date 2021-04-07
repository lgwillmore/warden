package codes.laurence.warden.policy.members

import codes.laurence.warden.Access
import codes.laurence.warden.AccessRequest
import codes.laurence.warden.AccessResponse
import codes.laurence.warden.policy.Policy
import codes.laurence.warden.policy.PolicyDSL
import codes.laurence.warden.policy.expression.ExpressionPolicy
import codes.laurence.warden.policy.expression.OperatorType
import codes.laurence.warden.policy.expression.ValueReference
import codes.laurence.warden.policy.expression.getValueFromAttributes

interface MemberPolicy {
    fun checkAuthorized(member: Map<*, *>, accessRequest: AccessRequest): AccessResponse
}

@PolicyDSL
class ForAnyMemberPolicy(
    val memberSource: ValueReference,
    val memberPolicies: List<MemberPolicy>
) : Policy {

    init {
        require(memberPolicies.isNotEmpty()) { "Member policies must not be empty" }
    }

    override fun checkAuthorized(accessRequest: AccessRequest): AccessResponse {
        try {
            val members = getMembers(accessRequest)
            members.forEach { member ->
                if (member !is Map<*, *>) {
                    throw InvalidMemberException("Members must be a Map")
                }
                var grantedByAllPolicies = true
                memberPolicies.forEach { policy ->
                    val response = policy.checkAuthorized(member, accessRequest)
                    if (response.access is Access.Denied) {
                        grantedByAllPolicies = false
                    }
                }
                if (grantedByAllPolicies) {
                    return AccessResponse(
                        access = Access.Granted(),
                        request = accessRequest
                    )
                }
            }
            return AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        } catch (e: InvalidMemberException) {
            return AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        }
    }

    private fun getMembers(accessRequest: AccessRequest): Collection<*> {
        val members = memberSource.get(accessRequest)
        if (members is Collection<*>) {
            return members
        }
        throw InvalidMemberException("Target attribute must be a collection")
    }

}

@PolicyDSL
class ForAllMembersPolicy(
    val memberSource: ValueReference,
    val memberPolicies: List<MemberPolicy>
) : Policy {

    init {
        require(memberPolicies.isNotEmpty()) { "Member policies must not be empty" }
    }

    override fun checkAuthorized(accessRequest: AccessRequest): AccessResponse {
        try {
            val members = getMembers(accessRequest)
            members.forEach { member ->
                if (member !is Map<*, *>) {
                    throw InvalidMemberException("Members must be a Map")
                }
                memberPolicies.forEach { policy ->
                    val response = policy.checkAuthorized(member, accessRequest)
                    if (response.access is Access.Denied) {
                        return response
                    }
                }
            }
            return AccessResponse(
                access = Access.Granted(),
                request = accessRequest
            )
        } catch (e: InvalidMemberException) {
            return AccessResponse(
                access = Access.Denied(),
                request = accessRequest
            )
        }
    }

    private fun getMembers(accessRequest: AccessRequest): Collection<*> {
        val members = memberSource.get(accessRequest)
        if (members is Collection<*>) {
            return members
        }
        throw InvalidMemberException("Target attribute must be a collection")
    }

}

class InvalidMemberException(message: String) : Exception(message)

data class MemberAttributeReference(
    val path: List<String>
) : ValueReference {

    init {
        require(path.isNotEmpty()) { "path cannot be empty" }
    }

    internal var member: Map<*, *>? = null

    override fun get(accessRequest: AccessRequest): Any? {
        return getValueFromAttributes(
            path,
            member ?: throw IllegalStateException("Member must be set before getting")
        )
    }
}

class MemberExpressionPolicy(
    val leftOperand: MemberAttributeReference,
    operatorType: OperatorType,
    rightOperand: ValueReference
) : MemberPolicy {

    internal var internalExpressionPolicy = ExpressionPolicy(
        leftOperand = leftOperand,
        operatorType = operatorType,
        rightOperand = rightOperand
    )

    override fun checkAuthorized(member: Map<*, *>, accessRequest: AccessRequest): AccessResponse {
        leftOperand.member = member
        return internalExpressionPolicy.checkAuthorized(accessRequest)
    }
}

