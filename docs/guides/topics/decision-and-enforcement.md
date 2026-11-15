# Decision and enforcement

`EnforcementPointDefault(policies)` from [](getting-started.md) is a shortcut. Behind it are three parts that can be
replaced one at a time: a policy source, an information point and a decision point.

## Policy source

A `PolicySource` returns the allow and deny policies for a request. `PolicySourceInMemory(allow, deny)` holds two lists
and returns them for every request.

Implement the interface to load policies from elsewhere, or to return only the policies that apply. This source keeps a
separate set per resource type. A request for an unknown type gets no policies and is therefore denied:

```kotlin
```
{src="src/main/kotlin/Decisions.kt" include-symbol="PolicySourceByResourceType"}

The source receives the request as it was submitted, before enrichment. Called with `null`, it should return every
policy.

## Information point

An `InformationPoint` enriches a request before it is evaluated. This one fetches the roles of a subject that arrived
with only an ID, and leaves other requests alone:

```kotlin
```
{src="src/main/kotlin/Decisions.kt" include-symbol="RolesInformationPoint"}

Information points can also supply environment attributes, such as the time of day:

```kotlin
```
{src="src/main/kotlin/Decisions.kt" include-symbol="HourInformationPoint"}

`InformationPointAggregate` runs several in order. Each sees what the previous one added:

```kotlin
```
{src="src/main/kotlin/Decisions.kt" include-symbol="blogInformationPoint"}

`InformationPointPassThrough` does nothing and is the default.

## Decision point

`DecisionPointLocal` evaluates in process. For each request it:

1. enriches the request with the information point;
2. fetches the policies for the request from the policy source;
3. evaluates the allow policies, then the deny policies, against the enriched request.

Access is granted only when some allow policy grants and no deny policy does. A deny policy is the place for a rule
that must hold regardless of the allow policies:

```kotlin
```
{src="src/main/kotlin/Decisions.kt" include-symbol="deletionOutsideBusinessHours,blogDecisionPoint"}

The returned `AccessResponse` holds the `Access` and the enriched request that was evaluated, so a caller can see what
the policies saw. `Access.Granted` and `Access.Denied` carry the properties set by the policy that decided; the DSL
policies set none.

`checkAuthorizedBatch` evaluates one subject, action and environment against a list of resources and returns a
response per resource, in order.

## Enforcement point

An `EnforcementPoint` wraps a decision point. `EnforcementPointDefault` throws `NotAuthorizedException` for a denial,
with the evaluated request and the denial's properties:

```kotlin
```
{src="src/main/kotlin/Decisions.kt" include-symbol="blogEnforcementPoint"}

`filterAuthorization` takes a `FilterAccessRequest`, in which each resource is paired with its attributes, and returns
the resources that were granted. `ArticleService.listArticles` in [](getting-started.md) uses it.

Implement `EnforcementPoint` yourself to enforce differently, for example by returning a result instead of throwing.
The Ktor and http4k modules each provide one that also reports to the plugin or filter; see [](ktor.md) and
[](http4k.md).
