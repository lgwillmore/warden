package codes.laurence.warden.policy.members

import codes.laurence.warden.policy.PolicyDSL
import codes.laurence.warden.policy.boolean.allOf
import codes.laurence.warden.policy.expression.OperatorBuilderBase
import codes.laurence.warden.policy.expression.subjectVal

@PolicyDSL
class MemberCollectionBuilder {
    fun attribute(pathRoot: String, vararg pathRest: String): OperatorBuilderBase {
        TODO()
    }
}

@PolicyDSL
class MemberBuilder {

    fun allOf(builder: MemberCollectionBuilder.() -> Unit) {
        TODO()
    }

    fun anyOf(builder: MemberCollectionBuilder.() -> Unit) {
        TODO()
    }

}

fun main() {
    allOf {
        subject("foo") forAnyMember {
            allOf {
                attribute("bar", "doe") equalTo subjectVal("wack")
            }
        }

        resource("foo") forAllMembers {
            anyOf {
                attribute("bar", "doe") equalTo subjectVal("wack")
            }
        }
    }
}