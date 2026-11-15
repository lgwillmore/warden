# http4k

Warden keeps authorization rules out of the HTTP layer, but the HTTP layer has one authorization concern of its own:
no route should be left open by accident. The `warden-http4k6` filter makes every handler behind it fail with `403`
unless it was authorized through an `EnforcementPointHttp4k` or explicitly left open. The artifact is named after the
http4k major version it targets; the package is `codes.laurence.warden.http4k` regardless of the http4k version.
Like http4k 6 itself, it needs JDK 21.

```kotlin
dependencies {
    implementation("codes.laurence.warden:warden-core:%warden_version%")
    implementation("codes.laurence.warden:warden-http4k6:%warden_version%")
}
```

## How it works

`EnforcementPointHttp4k` is an `EnforcementPoint` that also marks the current request as enforced when it is asked
for a decision. It wraps any enforcement point, or builds one from a decision point or from policies, so your services
take it in place of `EnforcementPointDefault`.

`WardenFilter` inspects every response that comes back through it. A response with a `2xx` or `101` status whose
request was neither marked as enforced nor left open is replaced by `403 Not Authorized: EnforcementPoint not
enforced`. Other statuses pass through, since something else already stopped the request. `OPTIONS` requests are
never checked.

http4k handles a request on one thread, and the filter publishes the request to that thread for the duration of the
handler. The enforcement point finds it there, however deep in your code it is called. Coroutines are the one way to
leave the thread: see [Suspending code](#suspending-code).

## Install

```kotlin
```
{src="src/main/kotlin/Http4kApp.kt" include-symbol="currentUser,blogApp"}

Three things happen in `blogApp`:

- A `WardenFilter` with a route priority stack. The first `WardenRoute` whose regular expression matches the whole
  request path and whose methods include the request method decides whether the route is enforced or ignored. A
  request that matches nothing is enforced. The `ENFORCE` entry above is therefore redundant, and shows how to carve
  exceptions out of an ignored prefix.
- `CatchNotAuthorized` turns the `NotAuthorizedException` an enforcement point throws into a response, `403` with no
  body unless you supply one. http4k has no status mapping of its own, so without it a denial becomes whatever the
  server does with an exception. The status is yours to choose, as is what the denial's properties reveal.
- The routes sit behind both filters. `WardenFilter` must be the outermost of the two, so that it sees the `403`
  rather than the exception.

## Suspending code

Warden's enforcement and decision points are `suspend` functions, and http4k handlers are not. `warded { }` runs a
suspending block from a handler, as `runBlocking` does, and additionally carries the request into the coroutine so
the enforcement point still finds it after a `withContext` moved the work to another dispatcher. A bare `runBlocking`
works only while the coroutine stays on the request thread; a `withContext(Dispatchers.IO)` before the enforcement
point would leave Warden unable to see the request, and the handler would answer `403`.

For code that does not suspend at all, `enforceAuthorizationBlocking`, `filterAuthorizationBlocking`,
`checkAuthorizedBlocking` and `checkAuthorizedBatchBlocking` are extension functions on the core interfaces that do
the same bridging for a single call.

## Leaving routes open

Two ways, both shown above: match the route in the route priority stack with `IGNORE`, or compose the `Unwarded`
filter in front of a handler or a group of routes. `unwarded()` called inside a handler does the same for that request.
`Unwarded` applies only to requests passing through the `WardenFilter` ahead of it, so it never opens a route by
accident elsewhere.

## Testing

http4k applications are functions, so the sample is tested by calling it, with no server and no client:

```kotlin
```
{src="src/test/kotlin/Http4kAppTest.kt" include-symbol="Http4kAppTest"}

## Websockets

`WardenFilter` is an HTTP filter and does not see http4k websocket handlers. Enforce authorization inside the
websocket handler, before it sends anything, with the enforcement point. Marking websockets as enforced, or refusing
the upgrade for an unauthorized request as the Ktor plugin does, is on the roadmap.

## What the filter does not do

Authentication. The sample reads a header; a real application resolves the user with its own authentication filter
and builds the subject from the principal. Warden only decides what that subject may do.
