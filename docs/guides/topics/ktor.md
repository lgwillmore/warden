# Ktor

Warden keeps authorization rules out of the HTTP layer, but the HTTP layer has one authorization concern of its own:
no route should be left open by accident. The `warden-ktor3` plugin makes every route fail with `403` unless it was
authorized through an `EnforcementPointKtor` or explicitly left open. The artifact is named after the Ktor major
version it targets; the package is `codes.laurence.warden.ktor` regardless of the Ktor version.

```kotlin
dependencies {
    implementation("codes.laurence.warden:warden-core:%warden_version%")
    implementation("codes.laurence.warden:warden-ktor3:%warden_version%")
}
```

## How it works

`EnforcementPointKtor` is an `EnforcementPoint` that also marks the current call as enforced when it is asked for a
decision.
It wraps any enforcement point, or builds one from a decision point or from policies, so your services take it in
place of `EnforcementPointDefault`.

The `Warden` plugin inspects every response. A response with a `2xx` or `101` status whose call was neither marked as
enforced nor left open is replaced by `403 Not Authorized: EnforcementPoint not enforced`. Other statuses pass
through, since something else already stopped the request. `OPTIONS` requests are never checked.

The enforcement point can only mark a call it can see. Routes must therefore be declared inside a `warded` block,
which carries the call to the enforcement point through the coroutine context. A route outside any `warded` block is
refused even when it calls the enforcement point.

## Install

```kotlin
```
{src="src/main/kotlin/KtorApp.kt" include-symbol="NotAuthenticatedException,currentUser,blogModule"}

Three things happen in `blogModule`:

- `install(Warden)` with a `routePriorityStack`. The first `WardenRoute` whose regular expression matches the request
  URI and whose methods include the request method decides whether the route is enforced or ignored. A request that
  matches nothing is enforced. The `ENFORCE` entry above is therefore redundant, and shows how to carve exceptions
  out of an ignored prefix.
- `install(StatusPages)` maps `NotAuthorizedException` to `403`. Without it Ktor answers `500`. The status is yours to
  choose, as is what the denial's properties reveal.
- `routing` declares the guarded routes inside `warded`, and the open ones outside it.

## Leaving routes open

Two ways, both shown above: match the route in `routePriorityStack` with `IGNORE`, or declare it in an `unwarded`
block. `unwarded` blocks can be nested inside `warded` ones, so a public corner of a private API stays next to it.

## Websockets

Once a websocket is open there is no response to replace, so authorization has to happen before the upgrade. A
`beforeEach` block runs for every route declared inside it, before the handler. Put the enforcement there, and the
handshake fails for a denied request:

```kotlin
```
{src="src/test/kotlin/KtorAppTest.kt" include-symbol="KtorAppTest"}

The `/feed` route in `blogModule` is authorized this way. `beforeEach` works for ordinary routes too, when several
share a check.

## What the plugin does not do

Authentication. The sample reads a header; a real application resolves the user with Ktor's authentication plugin and
builds the subject from the principal. Warden only decides what that subject may do.
