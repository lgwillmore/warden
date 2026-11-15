# Spring WebFlux

Warden keeps authorization rules out of the HTTP layer, but the HTTP layer has one authorization concern of its own:
no route should be left open by accident. The `warden-springboot4-webflux` filter makes every handler behind it fail
with `403` unless it was authorized through an `EnforcementPointSpring` or explicitly left open. The artifact is named
after the Spring Boot major version it targets; the packages are `codes.laurence.warden.spring` and
`codes.laurence.warden.spring.webflux` regardless of the Spring version.

```kotlin
dependencies {
    implementation("codes.laurence.warden:warden-core:%warden_version%")
    implementation("codes.laurence.warden:warden-springboot4-webflux:%warden_version%")
}
```

The filter brings `warden-springboot4` with it: the route configuration, the enforcement point and the denial
handling that any Spring adapter shares.

## How it works

`EnforcementPointSpring` is an `EnforcementPoint` that also marks the current request as enforced when it is asked
for a decision. It wraps any enforcement point, or builds one from a decision point or from policies, so your services
take it in place of `EnforcementPointDefault`.

`WardenWebFilter` inspects every response that comes back through it. A response with a `2xx` or `101` status whose
request was neither marked as enforced nor left open is replaced by `403 Not Authorized: EnforcementPoint not
enforced`. Other statuses pass through, since something else already stopped the request. `OPTIONS` requests are
never checked. The filter decides when the first byte of the body is written, or when a bodiless response completes,
so streaming responses are not buffered.

The filter carries the request into the coroutine context of a suspending handler and into the Reactor context of a
reactive one. The enforcement point finds it there, however deep in your code it is called and whichever dispatcher
the coroutine has moved to. Nothing has to be wrapped or passed along.

## Install

With the artifact on the classpath, Spring Boot auto-configures a `WardenWebFilter` with an empty route stack and
answers denials with an empty `403`. The sample defines two beans to configure both, and declares the enforcement
point and the service from [](getting-started.md):

```kotlin
```
{src="src/main/kotlin/SpringWebFluxApp.kt" include-symbol="Users,BlogApplication,BlogConfiguration,BlogController,BlogErrorHandling"}

Three things to notice:

- The `WardenWebFilter` bean carries the route priority stack. The first `WardenRoute` whose regular expression
  matches the whole request path and whose methods include the request method decides whether the route is enforced
  or ignored. A request that matches nothing is enforced. The `ENFORCE` entry above is therefore redundant, and shows
  how to carve exceptions out of an ignored prefix.
- The `NotAuthorizedResponse` bean decides what a denial is answered with. `WardenNotAuthorizedAdvice`, a controller
  advice the auto-configuration registers, turns the `NotAuthorizedException` an enforcement point throws into that
  response. The status is yours to choose, as is what the denial's properties reveal.
- The handlers are `suspend` functions calling the suspending service, and the filter follows the coroutine. A
  handler that returns a `Mono` or `Flux` instead reaches the enforcement point through a `mono { }` block from
  `kotlinx-coroutines-reactor`, which runs with the request's Reactor context.

## Leaving routes open

Three ways, the first two shown above: match the route in the route priority stack with `IGNORE`, annotate a handler
method or a whole controller with `@Unwarded`, or call `unwarded()` inside a suspending handler. A reactive handler
that has the `ServerWebExchange` calls `exchange.unwarded()`. All of them take effect only behind a
`WardenWebFilter`, so none opens a route by accident elsewhere.

## Testing

`@SpringBootTest` starts the application in process, and a `WebTestClient` bound to the application context calls it
without a server:

```kotlin
```
{src="src/test/kotlin/SpringWebFluxAppTest.kt" include-symbol="SpringWebFluxAppTest"}

## Websockets

A websocket handshake is completed by the server's upgrade strategy and never passes through the filter, so a
`WebSocketHandler` is not refused when it forgets to enforce. Enforce authorization inside the handler, before it
sends anything, with the enforcement point. Refusing the upgrade for an unauthorized request as the Ktor plugin does
is on the roadmap.

## What the filter does not do

Authentication. The sample reads a header; a real application resolves the user with Spring Security and builds the
subject from the principal. Spring Security's own `401` and `403` responses are not successes, so they pass through
the filter untouched.
