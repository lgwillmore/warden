# Warden

Warden is an attribute based access control (ABAC) library for Kotlin/JVM. It owns the access request model, the
policy DSL and its evaluation, decision points, information points and enforcement points, plus thin adapters for
web frameworks. Keep the core small and framework-free; the rules themselves, and where policies are stored, belong to
the consuming application.

The library is a dependency applications take on in their business layer, so it must stay lightweight: `warden-core`
depends on nothing but the Kotlin standard library, and an adapter brings only the framework it adapts. Persistence
and CRUD of policies, policy administration UIs, authentication, and session or token handling are outside the
library. A feature belongs in core only if it is needed to express or evaluate a policy against an access request.

## Repository layout

| Module | Artifact | Packages | Contents |
| --- | --- | --- | --- |
| `core` | `warden-core` | `codes.laurence.warden.*` | `AccessRequest`/`AccessResponse`, the policy DSL (`policy.expression`, `policy.bool`, `policy.collections`, `policy.members`), `PolicySource`, `InformationPoint`, `DecisionPoint`, `EnforcementPoint` and their default implementations. No dependencies. |
| `atts` | `warden-atts` | `codes.laurence.warden.atts` | `HasAttsI`/`HasAtts`, `AttType`, `attsOf` and `withAtts`: reflection-based conversion of domain objects to attribute maps. Depends on `kotlin-reflect`. Independent of core. |
| `ktor` | `warden-ktor` | `codes.laurence.warden.ktor` | The `Warden` Ktor server plugin, `warded`/`unwarded`/`beforeEach` route blocks, `EnforcementPointKtor`, route configuration. `wardedCall`/`unwardedCall` are `PipelineContext` extensions that no longer apply inside Ktor 3 route handlers (`RoutingContext`); they only work in `intercept` blocks. Depends on core and `ktor-server-core` through `api`. |
| `docs/guides/snippets` (`:docs-snippets`) | not published | `codes.laurence.warden.docs` | The blog application the guides quote, with its tests. |

- Published under the Maven group `codes.laurence.warden`, which is also the root Kotlin package. Keep the two in
  step.
- The guides are a Writerside project in `docs/guides` (`topics/*.md`, `warden.tree`). Topics quote
  `docs/guides/snippets` by symbol name (`include-symbol`), so renaming a quoted declaration there means updating the
  topic. Change a guide's code in the snippets module, never inline in the topic, and keep prose claims in step with
  the library. The Docs workflow builds the guides and the Dokka API reference and publishes both to GitHub Pages on
  each release.
- Core never depends on an adapter or on `warden-atts`. An adapter depends on core through `api`, since its public
  types implement core contracts. Public API changes are breaking for every consumer; prefer additions.

## Access requests and attributes

- An `AccessRequest` is four maps of attributes: `subject`, `action`, `resource`, `environment`. Attributes are
  `Map<String, Any?>` on purpose: a check may use a partial object, merge attributes from several sources, or carry
  attributes serialized from another system. Do not introduce generics over domain types inside the evaluation
  boundary.
- Nested maps are addressed by path (`subject("address", "country")`). A missing attribute anywhere on the path is
  `NoSuchAttributeException` inside evaluation and a `Denied` response outside it, never a crash.
- `warden-atts` is the only place reflection is used. `attsOf` reads public member properties and converts nested
  `HasAttsI`, collections and maps recursively. `AttType` supplies a shared `type` attribute (key configurable via
  `typeKeyword`) so one domain type can be identified across several classes.

## Policies and evaluation

- A `Policy` is `checkAuthorized(AccessRequest): AccessResponse`, synchronous and side-effect free. The response
  carries `Access.Granted` or `Access.Denied`, each with a `properties` map, and the request that was evaluated.
- `ExpressionPolicy` compares two `ValueReference`s (`AttributeReference` into the request, or `PassThroughReference`
  for a literal) with an `OperatorType`: `EQUAL`, the four comparisons, `CONTAINS`, `CONTAINS_ALL`, `CONTAINS_ANY`,
  `IS_IN`. Operands of the wrong type throw `BadExpressionException`: that is a policy authoring error, not a denial.
- `AllOf` grants only if every child grants and there is at least one child; an empty `AllOf` denies. `AnyOf` grants
  if any child grants; an empty `AnyOf` denies. `Not` inverts. `ForAnyMemberPolicy`/`ForAllMembersPolicy` evaluate
  member policies against each element of a collection attribute, which must be a collection of maps. They bind the
  current member into a mutable field on `MemberAttributeReference` during evaluation, so one policy instance is not
  safe to evaluate from several threads at once (roadmap).
- The DSL (`allOf {}`, `anyOf {}`, `not()`, `Exp.subject(...) equalTo ...`, `subjectVal(...)` and friends,
  `forAnyMember {}`) builds exactly these classes. Keep the DSL a thin builder: no evaluation logic lives in it.
- `DecisionPointLocal` enriches the request through its `InformationPoint`, fetches `Policies` (allow and deny lists)
  from its `PolicySource` for the original, unenriched request, then grants only if some allow policy grants and no
  deny policy grants. A deny that fires replaces the response's access with an empty `Denied`, so the deny policy's
  properties are not propagated; `Not` drops properties the same way. Batch checks evaluate one request per resource with the shared subject, action and environment.
- `EnforcementPointDefault` throws `NotAuthorizedException(request, deniedProperties)` on denial and otherwise
  returns. `filterAuthorization` keeps the resources whose batch decision was granted, in order.
- `InformationPoint.enrich` and `PolicySource.policies` are `suspend`, since they may call out. Policies are not.

## Ktor plugin

- `install(Warden)` intercepts the send pipeline. If a call finishes with a 2xx or 101 status, is not inside an
  `unwarded` block or an `IGNORE` route in `routePriorityStack`, and no `EnforcementPointKtor` was invoked for it,
  the response is replaced with 403 and `NOT_ENFORCED_MESSAGE`. OPTIONS requests are never intercepted. Non-success
  responses pass through so other errors stay visible.
- `EnforcementPointKtor` marks the call as enforced through a `WardenCoroutineContext` element that `warded {}`
  installs; `beforeEach` runs inside the enclosing `warded {}`. Enforcing outside such a block cannot reach the call and the route still
  answers 403; this is logged at debug level.
- `routePriorityStack` is matched top down by regex and HTTP method; the first match wins, and no match means
  `ENFORCE`.
- `beforeEach` exists for websockets, where the handler runs after the upgrade and a denial can no longer produce a
  clean HTTP status.

## Testing

- `./gradlew test` runs everything, including the guide snippets. Nothing needs external services.
- Tests use `kotlin.test`, `assertk` and `mockk`, with full function bodies and nested `runBlocking` for suspend
  calls. Ktor tests use `testApplication`.
- Random fixtures live in each module's `test/.../fixtures.kt` and `random.kt`.
- Before submitting a PR, run `./gradlew ktfmtFormat`; CI runs `ktfmtCheck`.

Outstanding work is tracked in [`docs/roadmap.md`](docs/roadmap.md).
