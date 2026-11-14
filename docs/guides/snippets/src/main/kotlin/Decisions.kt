package codes.laurence.warden.docs

import codes.laurence.warden.AccessRequest
import codes.laurence.warden.decision.DecisionPoint
import codes.laurence.warden.decision.DecisionPointLocal
import codes.laurence.warden.enforce.EnforcementPoint
import codes.laurence.warden.enforce.EnforcementPointDefault
import codes.laurence.warden.information.InformationPoint
import codes.laurence.warden.information.InformationPointAggregate
import codes.laurence.warden.policy.Policies
import codes.laurence.warden.policy.Policy
import codes.laurence.warden.policy.PolicySource
import codes.laurence.warden.policy.bool.allOf
import codes.laurence.warden.policy.bool.not

/** Looks up the roles of a subject that arrived with only an ID. */
class RolesInformationPoint(private val rolesOf: suspend (userId: String) -> List<String>) :
    InformationPoint {

  override suspend fun enrich(request: AccessRequest): AccessRequest {
    val subject = request.subject
    if (subject["type"] != "User" || subject.containsKey("roles")) return request
    val userId = subject["id"] as? String ?: return request
    return request.copy(subject = subject + ("roles" to rolesOf(userId)))
  }
}

/** Adds the current hour to the environment, for policies that depend on the time of day. */
class HourInformationPoint(private val hourOfDay: () -> Int) : InformationPoint {

  override suspend fun enrich(request: AccessRequest): AccessRequest =
      request.copy(environment = request.environment + ("hour" to hourOfDay()))
}

/** Information points run in order, each seeing the previous one's additions. */
fun blogInformationPoint(
    rolesOf: suspend (userId: String) -> List<String>,
    hourOfDay: () -> Int,
): InformationPoint =
    InformationPointAggregate(
        listOf(
            RolesInformationPoint(rolesOf),
            HourInformationPoint(hourOfDay),
        ))

/** Serves each resource type its own policies, so unrelated policies are never evaluated. */
class PolicySourceByResourceType(private val byType: Map<String, Policies>) : PolicySource {

  override suspend fun policies(request: AccessRequest?): Policies {
    val type = request?.resource?.get("type") ?: return everything()
    return byType[type] ?: Policies(allow = emptyList(), deny = emptyList())
  }

  private fun everything() =
      Policies(
          allow = byType.values.flatMap { it.allow },
          deny = byType.values.flatMap { it.deny },
      )
}

/** A deny policy: nobody deletes outside business hours, whatever the allow policies say. */
val deletionOutsideBusinessHours: Policy = allOf {
  action("type") equalTo "DELETE"
  add(not(BusinessHoursPolicy()))
}

fun blogDecisionPoint(
    rolesOf: suspend (userId: String) -> List<String>,
    hourOfDay: () -> Int,
): DecisionPoint =
    DecisionPointLocal(
        policySource =
            PolicySourceByResourceType(
                mapOf(
                    "Article" to
                        Policies(
                            allow = articlePolicies,
                            deny = listOf(deletionOutsideBusinessHours),
                        ))),
        informationPoint = blogInformationPoint(rolesOf, hourOfDay),
    )

fun blogEnforcementPoint(
    rolesOf: suspend (userId: String) -> List<String>,
    hourOfDay: () -> Int,
): EnforcementPoint = EnforcementPointDefault(blogDecisionPoint(rolesOf, hourOfDay))
