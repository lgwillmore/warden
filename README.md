# Warden

[![Check](https://github.com/lgwillmore/warden/actions/workflows/check.yml/badge.svg?branch=main)](https://github.com/lgwillmore/warden/actions/workflows/check.yml)
[![Maven Central](https://img.shields.io/maven-central/v/codes.laurence.warden/warden-core)](https://central.sonatype.com/namespace/codes.laurence.warden)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Warden is attribute based access control (ABAC) for Kotlin on the JVM. You write authorization rules as policies over
the attributes of a request: who is asking, what they want to do, what they want to do it to, and in what context.
Warden evaluates them and guards your code, with no dependency on a web framework.

**[Guides](https://lgwillmore.github.io/warden/)** · **[API reference](https://lgwillmore.github.io/warden/api/)**

## What you write

Policies are plain Kotlin values built with a small DSL. They talk about your domain, not your URLs:

```kotlin
val articlePolicies = listOf(
    // Any user can read any article
    allOf {
      resource("type") equalTo "Article"
      action("type") equalTo "READ"
    },
    // The author of an article can modify and delete it
    allOf {
      resource("type") equalTo "Article"
      action("type") isIn listOf("MODIFY", "DELETE")
      subject("id") equalTo resourceVal("authorId")
    },
    // Only authors can create articles
    allOf {
      resource("type") equalTo "Article"
      action("type") equalTo "CREATE"
      subject("roles") contains "Author"
    },
)
```

## What you get

An enforcement point protects any execution path. If no policy grants access, it throws `NotAuthorizedException` and
the code after it never runs:

```kotlin
val enforcementPoint = EnforcementPointDefault(articlePolicies)

suspend fun readArticle(user: User, articleId: String): Article {
  val article = articles.getById(articleId)
  enforcementPoint.enforceAuthorization(
      AccessRequest(
          subject = user.atts(),
          action = mapOf("type" to "READ"),
          resource = article.atts(),
      ))
  return article
}
```

The complete example, with its tests, is in [`docs/guides/snippets`](docs/guides/snippets) and is explained step by
step in [Getting started](https://lgwillmore.github.io/warden/getting-started.html).

- **Rules live with your business logic**, not in routing. One rule protects every entry point that needs it.
- **Attributes are maps.** Subjects, actions, resources and environments are bags of attributes, so a check can use
  a partial object, merge attributes from several sources, or run against data from another service.
- **Decision and enforcement are separate.** A `DecisionPoint` evaluates allow and deny policies; an
  `EnforcementPoint` turns a denial into an exception or filters a collection down to what is permitted.
- **Information points enrich requests.** Load roles, ownership or anything else a policy needs before evaluation,
  without the caller knowing.
- **The Ktor plugin closes the doors you forgot.** Every route must call the enforcement point or be explicitly
  opened, or it answers 403.

## Install

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("codes.laurence.warden:warden-core:<version>")
    implementation("codes.laurence.warden:warden-atts:<version>") // domain objects to attribute maps
    implementation("codes.laurence.warden:warden-ktor3:<version>") // Ktor plugin
}
```

| Artifact | Contents |
| --- | --- |
| `warden-core` | Access requests, the policy DSL, decision, information and enforcement points |
| `warden-atts` | `HasAtts` and `AttType`: turn domain objects into attribute maps by reflection |
| `warden-ktor3` | A [Ktor](https://ktor.io/) 3 server plugin that makes enforcement mandatory per route |

Requires JDK 17 or later.

## Documentation

| Guide | Covers |
| --- | --- |
| [Key concepts](https://lgwillmore.github.io/warden/concepts.html) | Access requests, policies, decision, information and enforcement points |
| [Getting started](https://lgwillmore.github.io/warden/getting-started.html) | Policies and enforcement for a small blog |
| [Policies](https://lgwillmore.github.io/warden/policies.html) | Expression, boolean and collection member policies |
| [Decision and enforcement](https://lgwillmore.github.io/warden/decision-and-enforcement.html) | Policy sources, information points, decision and enforcement points |
| [Attributes](https://lgwillmore.github.io/warden/attributes.html) | `warden-atts` helpers |
| [Testing](https://lgwillmore.github.io/warden/testing.html) | Testing policies, services and routes |
| [Ktor](https://lgwillmore.github.io/warden/ktor.html) | The Ktor plugin |

The [API reference](https://lgwillmore.github.io/warden/api/) covers every public type.

## Contributing

Requires JDK 17.

```shell
./gradlew test
./gradlew ktfmtFormat   # before opening a PR
```

- [`AGENTS.md`](AGENTS.md) holds the design rules the library is built to.
- [`docs/roadmap.md`](docs/roadmap.md) lists outstanding work.
- The guides are a [Writerside](https://www.jetbrains.com/writerside/) project in `docs/guides`; open it with the
  Writerside plugin for IntelliJ IDEA to preview them. The code they quote lives in `docs/guides/snippets` and is
  compiled and tested by `./gradlew test`. `./gradlew dokkaGenerate` builds the API reference.
- Releases are described in [`PUBLISHING_SETUP.md`](PUBLISHING_SETUP.md).

Licensed under [MIT](LICENSE).
