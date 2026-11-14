# Roadmap

Status: backlog assembled on 2026-11-14 while reviving public maintenance. Current implementation rules live in
[`AGENTS.md`](../AGENTS.md).

## Before the first public release

The Kotlin packages and the Maven group agree on `codes.laurence.warden`, the public home is
`github.com/lgwillmore/warden`, and the release tags up to `0.4.0` match what Maven Central holds. What remains is
operational and listed in [`PUBLISHING_SETUP.md`](../PUBLISHING_SETUP.md): the Central and signing secrets, GitHub
Pages enabled with the GitHub Actions source, the old Netlify documentation site retired, and the private interim
repository archived.

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
9. **Other adapters.** A Spring enforcement point was planned in the original project. Keep adapters as separate
   artifacts depending on core through `api`.

## Documentation

10. **Versioned documentation.** Keep the guides and API reference of earlier releases available once the API settles.
