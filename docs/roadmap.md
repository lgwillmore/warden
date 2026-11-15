# Roadmap

Status: backlog assembled on 2026-11-14 while reviving public maintenance; `0.5.0` is released and the publishing
pipeline in [`PUBLISHING_SETUP.md`](../PUBLISHING_SETUP.md) is in place. Current implementation rules live in
[`AGENTS.md`](../AGENTS.md).

## Library

1. **Member policies are not thread-safe.** `MemberExpressionPolicy` binds the current member into a mutable field on
   `MemberAttributeReference` while evaluating, so a `forAnyMember`/`forAllMembers` policy shared between concurrent
   requests (the normal case in a server) can read another request's member. Bind the member through a per-call
   reference instead; no public API needs to change.
2. **Dead Ktor API.** `wardedCall`/`unwardedCall` are `PipelineContext` extensions and do not compile inside Ktor 3
   route handlers, whose receiver is `RoutingContext`. Either provide `RoutingContext` overloads or remove them at the
   next major version.
3. **Denial properties are dropped.** When a deny policy fires, `DecisionPointLocal` returns an empty `Denied`, and
   `Not` discards the inner response's properties when flipping. Decide whether properties should flow through.
4. **Policy source sees the unenriched request.** `DecisionPointLocal` enriches the request but passes the original
   to `PolicySource.policies`. Documented as-is in the guides; decide whether sources should see enriched attributes.
5. **Informative decisions.** `Access.Granted`/`Denied` carry a `properties` map, but no built-in policy populates it,
   so a caller cannot tell which policy granted or denied. Decide the shape (policy names, matched expression) and
   populate it from `AllOf`/`AnyOf`/`ExpressionPolicy`.
6. **Policy naming and serialization.** Policies are plain objects with no identity. Naming them, and a stable
   serialized form, is the prerequisite for storing policies as data and for a remote `DecisionPoint`.
7. **Policy source matching.** `PolicySource.policies(request)` receives the request so a source can return a
   relevant subset, but `PolicySourceInMemory` ignores it. Provide an indexed in-memory source, or document the
   pattern for one.
8. **Ktor plugin tidy-up.** `KtorEnforcementPointException` is unused, `exit()` is a no-op, and `WARDEN_IGNORED` is
   public while `WARDEN_ENFORCED` is internal. Decide what is API and remove the rest at the next major version.
9. **Spring MVC adapter.** `warden-springboot4-webmvc` on the `warden-springboot4` base, mirroring the WebFlux
   filter with a servlet filter. `wardenCall()` already reads the request attribute Spring binds to the servlet
   thread, so the filter stores the `WardenCall` there. It must run on async dispatches too and judge the response on
   the final one, and decide at the first byte written rather than buffer. Suspending controllers lose the request
   once the coroutine leaves the servlet thread, as in http4k, so a `warded {}` bridge is needed there. A
   `warden-ktor2` is only worth adding if someone asks for it.
10. **Websockets in http4k and WebFlux.** Neither `WardenFilter` nor `WardenWebFilter` sees a websocket handshake.
    Decide how a websocket request is marked as enforced, or refused before the upgrade as Ktor's `beforeEach` does.

## Documentation

11. **Versioned documentation.** Keep the guides and API reference of earlier releases available once the API settles.
