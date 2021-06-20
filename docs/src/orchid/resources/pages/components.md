---
title: 'Components'
---

## Policy Source

The `PolicySource` interface alleviates the responsibility of retrieving available policies from the `DecisionPoint`.
They could be in memory, or stored in a DB or available from another service.

A `PolicySource` defines a single method for retrieving a set of `deny` and `allow` policies based on
an `AccessRequest` (or left null to fetch all policies).

The simplest Policy source is `PolicySourceInMemory`.

```kotlin
val policySource = PolicySourceInMemory(
    allow = listOf(
        // Some set of policies that allow access.
    ),
    deny = listOf(
        // Some set of policies that deny access.
    )
)
```

## Information Point

An `InformationPoint` is used by the `DecisionPoint` and it lets us enrich partially complete attributes of
an `AccessRequest` with more attributes. Possible scenarios are:

- There may be other entities or resources associated with a given resource, but which we want to use in our policy
  rules. We can fetch and merge those attributes into the existing attributes of the primary resource.
- When you fetch a resource by ID, generally all you know is the ID and the type. A workaround is to only check the
  authorization after you have retrieved the resource, but what if you want to just check permissions?

The `InformationPoint` enrich function gets full access to the request, and so you can implement the retrieval and
enrichment of attributes as you wish.

**Example of an InformationPoint that enriches User subjects with user roles**

```kotlin
class InformationPointUserRoles : InformationPoint {

    /**
     * Add user roles to user if missing
     */
    override suspend fun enrich(request: AccessRequest): AccessRequest {
        val subjectType = request.subject["type"] ?: "not_user"
        val userID = request.subject["id"]
        val subjectRoles = request.subject["roles"]
        if (subjectType == "User" && userID != null && subjectRoles == null) {
            val userRoles = getUserRolesFromSomewhere(userID)
            return request.copy(
                subject = request.subject + mapOf("roles" to userRoles)
            )
        }
        return request
    }
}
```

## Decision Point

The `DecisionPoint` is where most of the work is done for allowing/denying an `AccessRequest`.

1) The `DecisionPoint` will enrich the attributes of the `AccessRequest` with the `InformationPoint`
2) The `DecisionPoint` will retrieve Policies based on the access request itself from a `PolicySource`.
3) The `DecisionPoint` will evaluate all allow and deny policies for the `AccessRequest` and return a result.

The evaluation works over a set of `allow` policies and a set of `deny` policies provided by the `InformationPoint`. The
result of the evaluation will depend on:

- if there is not an `allow` policy that grants access, there is no access.
- if there is an `allow` policy that grants access, but the is also a `deny` policy that grants access, there is no
  access.
- if there is an `allow` policy that grants access and no `deny` policy that grants access, there is access.

**Example of wiring up a `DecisionPointLocal` with an in memory `PolicySource` and our `InformationPoint` from the
previous example.**

```kotlin

val allowPolicies = listOf<Policy>(
    // Some set of policies
)
val denyPolicies = listOf<Policy>(
    // Some set of policies
)

val decisionPoint = DecisionPointLocal(
    policySource = PolicySourceInMemory(
        allow = allowPolicies,
        deny = denyPolicies
    ),
    informationPoint = InformationPointUserRoles()
)
```

## Enforcement Point

An `EnforcementPoint` converts the result of a `DesicionPoint` evaluation into a thrown `NotAuthorizedException` if
access was denied.

The purpose of the `EnforcementPoint` call is to prevent any further processing of the `AccessRequest`

**Example of wiring up the default `EnforcementPoint` with the `DecisionPoint` from our previous example**

```kotlin
val enforcementPoint = EnforcementPointDefault(
    decisionPoint = decisionPoint
)
```

...but if we want to get up and running with an `EnforcementPoint` without worrying about everything else, it has a
convenience constructor that will take the `allow` and `deny` policies.

```kotlin
val allowPolicies = listOf<Policy>(
    // Some set of policies
)
val denyPolicies = listOf<Policy>(
    // Some set of policies
)

val enforcementPointFromPolicies = EnforcementPointDefault(
    allow = allowPolicies,
    deny = denyPolicies
)
```


