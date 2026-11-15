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
| `ktor3` | `warden-ktor3` | `codes.laurence.warden.ktor` | The `Warden` Ktor 3 server plugin, `warded`/`unwarded`/`beforeEach` route blocks, `EnforcementPointKtor`, route configuration. `wardedCall`/`unwardedCall` are `PipelineContext` extensions that no longer apply inside Ktor 3 route handlers (`RoutingContext`); they only work in `intercept` blocks. Depends on core and `ktor-server-core` through `api`. |
| `http4k6` | `warden-http4k6` | `codes.laurence.warden.http4k` | `WardenFilter`, `Unwarded`/`unwarded()`, `CatchNotAuthorized`, `EnforcementPointHttp4k`, route configuration, and the blocking bridges `warded {}` and the `...Blocking` extension functions. Depends on core and `http4k-core` through `api`. Built for JDK 21, as http4k 6 is. |
| `springboot4` | `warden-springboot4` | `codes.laurence.warden.spring` | Shared by the Spring adapters: route configuration on Spring's `HttpMethod`, `WardenCall` with its coroutine context element and `wardenCall()`, `EnforcementPointSpring`, `@Unwarded`, `NotAuthorizedResponse` and `WardenNotAuthorizedAdvice`. Depends on core and `spring-web` through `api`. |
| `springboot4-webflux` | `warden-springboot4-webflux` | `codes.laurence.warden.spring.webflux` | `WardenWebFilter` (a `CoWebFilter`), `unwarded()` and `ServerWebExchange.unwarded()`, and `WardenWebFluxAutoConfiguration`. Depends on the base module, `spring-webflux` and `kotlinx-coroutines-reactor` through `api`. |
| `docs/guides/snippets` (`:docs-snippets`) | not published | `codes.laurence.warden.docs` | The blog application the guides quote, with its tests. |

- Published under the Maven group `codes.laurence.warden`, which is also the root Kotlin package. Keep the two in
  step.
- The guides are a Writerside project in `docs/guides` (`topics/*.md`, `warden.tree`). Topics quote
  `docs/guides/snippets` by symbol name (`include-symbol`), so renaming a quoted declaration there means updating the
  topic. Change a guide's code in the snippets module, never inline in the topic, and keep prose claims in step with
  the library. The Docs workflow builds the guides and the Dokka API reference and publishes both to GitHub Pages on
  each release.
- Framework adapters are named after the framework's major version (`warden-ktor3`, `warden-http4k6`,
  `warden-springboot4-webflux`), so each framework major can have its own artifact while all artifacts share Warden's
  version. The package stays `codes.laurence.warden.ktor`, `codes.laurence.warden.http4k` or
  `codes.laurence.warden.spring` without the version, so moving between adapters is a dependency swap, not an import
  change. The Spring adapters share `warden-springboot4`, since Spring MVC and WebFlux both build on `spring-web`.
- Every adapter implements the same contract as the Ktor plugin: a successful response (2xx or 101) whose request was
  neither enforced through the adapter's enforcement point nor explicitly left open becomes 403 with
  `NOT_ENFORCED_MESSAGE`; OPTIONS is never checked; a regex-and-method route priority stack is matched top down with
  enforce as the default; there is an inline opt-out; enforcing where the adapter cannot see the request still
  enforces and logs at debug; non-success responses pass through. Route config and its evaluator are copied per
  adapter because each uses the framework's own method type; the Spring adapters share theirs through the base module.
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

## http4k filter

- `WardenFilter` is an http4k `Filter`. It publishes a `WardenCall` to the request thread in a `ThreadLocal` for the
  duration of the handler, then inspects the returned `Response` and substitutes the 403 by the shared contract. The
  route stack is matched against `request.uri.path`, so a query string cannot defeat it.
- `EnforcementPointHttp4k` finds the call through a `WardenCoroutineContext` element first, then the thread local.
  `warded {}` is `runBlocking` plus that element, so enforcement reached after a `withContext` to another dispatcher
  still marks the request; a bare `runBlocking` only works while the coroutine stays on the request thread. The
  `...Blocking` extension functions on `EnforcementPoint` and `DecisionPoint` are built on `warded {}`.
- `Unwarded` is a `Filter` and `unwarded()` a function; both mark the current call ignored, so they only take effect
  behind a `WardenFilter`. `CatchNotAuthorized` maps `NotAuthorizedException` to a response (403 by default), since
  http4k has no status mapping of its own; it must sit behind `WardenFilter` so the filter sees a status, not an
  exception.
- The filter does not see http4k websocket handlers; websocket enforcement is on the roadmap.

## Spring WebFlux filter

- `WardenWebFilter` extends Spring's `CoWebFilter` and runs at highest precedence. Per request it creates a
  `WardenCall`, stores it as an exchange attribute, and runs the rest of the chain inside
  `WardenCoroutineContext(call)` plus a `ReactorContext` carrying the call under `WardenCall.REACTOR_CONTEXT_KEY`.
  `CoWebFilter` hands that coroutine context to suspending handlers, and `awaitSingleOrNull` injects the Reactor
  context into the chain, so `wardenCall()` finds the call from a suspending handler on any dispatcher and from a
  `mono {}` block in a reactive one. `wardenCall()` also reads the request attribute Spring binds to a servlet
  thread, ready for the MVC adapter.
- The response is judged by a `ServerHttpResponseDecorator` at the first body buffer (`switchOnFirst`), or in
  `setComplete` for a bodiless response, so nothing is buffered and streams pass through. The 403 replaces status,
  content type and length and releases the original buffers. A committed response is never touched.
- Opt-outs: the route stack against `request.uri.path`; `@Unwarded` on the handler method or its controller, read
  from `HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE`; `unwarded()` in a suspending handler or
  `exchange.unwarded()` in a reactive one.
- `WardenWebFluxAutoConfiguration` registers a `WardenWebFilter` with an empty route stack, a
  `NotAuthorizedResponse.FORBIDDEN` and a `WardenNotAuthorizedAdvice`, each behind `@ConditionalOnMissingBean`, so
  an application configures Warden by defining those beans.
- The filter does not see a websocket handshake, which the upgrade strategy completes on the native response;
  websocket enforcement is on the roadmap with http4k's.

## Testing

- `./gradlew test` runs everything, including the guide snippets. Nothing needs external services. `warden-http4k6`
  and the snippets use a JDK 21 toolchain that Gradle downloads; the other modules build with 17.
- Tests use `kotlin.test`, `assertk` and `mockk`, with full function bodies and nested `runBlocking` for suspend
  calls. Ktor tests use `testApplication`; http4k tests call the filtered handler in memory; WebFlux tests use
  `WebTestClient.bindToController` and Boot's `ReactiveWebApplicationContextRunner`. The snippets' `@SpringBootTest`
  needs JUnit Jupiter 6, which the snippets module adds explicitly because kotlin-test alone brings Jupiter 5.
- Random fixtures live in each module's `test/.../fixtures.kt` and `random.kt`.
- Before submitting a PR, run `./gradlew ktfmtFormat`; CI runs `ktfmtCheck`.

Outstanding work is tracked in [`docs/roadmap.md`](docs/roadmap.md).
