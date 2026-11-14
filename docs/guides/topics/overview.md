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
- **A Ktor plugin that fails closed.** Every route must be authorized or explicitly left open, or it answers `403`.

## Modules

| Artifact | Contents |
|----------|----------|
| `codes.laurence.warden:warden-core` | Access requests, the policy DSL, decision, information and enforcement points |
| `codes.laurence.warden:warden-atts` | Turns your domain objects into attribute maps |
| `codes.laurence.warden:warden-ktor` | The Ktor plugin and an enforcement point that works with it |

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("codes.laurence.warden:warden-core:%warden_version%")
    implementation("codes.laurence.warden:warden-atts:%warden_version%")
    implementation("codes.laurence.warden:warden-ktor:%warden_version%") // Ktor applications only
}
```

Warden needs JDK 17 or later. The Kotlin packages are `codes.laurence.warden`, `codes.laurence.warden.atts` and
`codes.laurence.warden.ktor`.

## Where to start

- [](concepts.md) explains the parts of an access request and how allow and deny policies are evaluated.
- [](getting-started.md) protects a small blog service with three policies.
- [](policies.md) lists every operator of the DSL and shows how to write your own policy.
- [](decision-and-enforcement.md) covers policy sources, information points and the decision point.
- [](attributes.md) covers `warden-atts`.
- [](ktor.md) installs the plugin and authorizes HTTP routes and websockets.
- [](testing.md) tests policies, services and routes.

Every code sample in these guides comes from one small blog application that is compiled and tested with the library.
Its source is in [`docs/guides/snippets`](https://github.com/lgwillmore/warden/tree/main/docs/guides/snippets).
