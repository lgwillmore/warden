package codes.laurence.warden.policy.members

import codes.laurence.warden.policy.boolean.allOf
import codes.laurence.warden.policy.collections.resource
import codes.laurence.warden.policy.expression.OperatorBuilder
import codes.laurence.warden.policy.expression.subjectVal

class MemberBuilder {
    fun attribute(pathRoot: String, vararg pathRest: String): OperatorBuilder {
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
            attribute("bar") forAll {
                attribute("doe") equalTo subjectVal("wack")
            }
        }
    }
}