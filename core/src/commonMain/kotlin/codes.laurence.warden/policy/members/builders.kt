package codes.laurence.warden.policy.members

import codes.laurence.warden.policy.PolicyDSL
import codes.laurence.warden.policy.boolean.allOf
import codes.laurence.warden.policy.expression.OperatorBuilderBase
import codes.laurence.warden.policy.expression.subjectVal

@PolicyDSL
class MemberBuilder {
    fun attribute(pathRoot: String, vararg pathRest: String): OperatorBuilderBase {
        TODO()
    }
}


fun main() {
    allOf {
        resource("foo") forAny {
            attribute("bar", "doe") equalTo subjectVal("wack")
        }

        resource("foo") forAll {
            attribute("bar", "doe") equalTo subjectVal("wack")
        }

        resource("foo") forAny {
            attribute("bar")
        }
    }
}