# Warden

Warden is attribute based access control (ABAC) for Kotlin on the JVM. You describe who may do what as policies over
the attributes of a request: the subject, the action, the resource and the environment. Warden evaluates them and
stops execution when access is denied. Policies are plain Kotlin, decoupled from any web framework, so the same rules
guard a service method, a message handler or an HTTP route.

## What it gives you

- **A policy DSL.** Policies read like the rule they implement: `subject("id") equalTo resourceVal("authorId")`.
  Expressions combine with `allOf`, `anyOf` and `not`, and can range over the members of a collection.
- **Decision, enforcement and information as separate parts.** A decision point evaluates allow and deny policies. An
  enforcement point turns a denial into an exception. An information point fetches attributes a request did not
  arrive with.
- **Attributes as maps.** Any part of a request can be a subset of attributes, assembled from several sources.
  `warden-atts` converts your own classes with one line.
- **Filtering as well as checking.** Ask which of a list of resources a user may act on, in one call.
- **Web adapters that fail closed.** With the Ktor plugin, the http4k filter or the Spring WebFlux filter installed,
  every route must be authorized or explicitly left open, or it answers `403`.

## Modules

| Artifact | Contents |
|----------|----------|
| `codes.laurence.warden:warden-core` | Access requests, the policy DSL, decision, information and enforcement points |
| `codes.laurence.warden:warden-atts` | Turns your domain objects into attribute maps |
| `codes.laurence.warden:warden-ktor3` | The Ktor 3 plugin and an enforcement point that works with it |
| `codes.laurence.warden:warden-http4k6` | The http4k 6 filter and an enforcement point that works with it |
| `codes.laurence.warden:warden-springboot4-webflux` | The Spring WebFlux filter for Spring Boot 4, auto-configured |
| `codes.laurence.warden:warden-springboot4` | Shared by the Spring adapters: route configuration, an enforcement point and denial handling |

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("codes.laurence.warden:warden-core:%warden_version%")
    implementation("codes.laurence.warden:warden-atts:%warden_version%")
    implementation("codes.laurence.warden:warden-ktor3:%warden_version%") // Ktor applications only
    implementation("codes.laurence.warden:warden-http4k6:%warden_version%") // http4k applications only
    implementation("codes.laurence.warden:warden-springboot4-webflux:%warden_version%") // Spring WebFlux applications only
}
```

Warden needs JDK 17 or later; `warden-http4k6` needs JDK 21, as http4k 6 does. The Kotlin packages are
`codes.laurence.warden`, `codes.laurence.warden.atts`, `codes.laurence.warden.ktor`, `codes.laurence.warden.http4k` and
`codes.laurence.warden.spring` with `codes.laurence.warden.spring.webflux`.

## Where to start

- [](concepts.md) explains the parts of an access request and how allow and deny policies are evaluated.
- [](getting-started.md) protects a small blog service with three policies.
- [](policies.md) lists every operator of the DSL and shows how to write your own policy.
- [](decision-and-enforcement.md) covers policy sources, information points and the decision point.
- [](attributes.md) covers `warden-atts`.
- [](ktor.md) installs the plugin and authorizes HTTP routes and websockets.
- [](http4k.md) installs the filter and bridges handlers to Warden's suspending API.
- [](spring-webflux.md) installs the filter in a Spring Boot application with suspending handlers.
- [](testing.md) tests policies, services and routes.

Every code sample in these guides comes from one small blog application that is compiled and tested with the library.
Its source is in [`docs/guides/snippets`](https://github.com/lgwillmore/warden/tree/main/docs/guides/snippets).
