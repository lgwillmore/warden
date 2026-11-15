# Getting started

This guide protects a small blog with three rules:

- Any user can read any article.
- The author of an article can modify and delete it.
- Only users with the `Author` role can create articles.

## Model the domain

Users and articles extend `HasAtts` from `warden-atts`, which turns their public properties into attributes. The
`AttType` adds a `type` attribute, so policies can tell a `User` from an `Article`. Actions are attribute maps too:

```kotlin
```
{src="src/main/kotlin/Blog.kt" include-symbol="UserType,ArticleType,User,Article,ArticleAction"}

`Article(id = "a1", title = "Warden", authorId = "ada").atts()` is
`{type=Article, id=a1, title=Warden, authorId=ada}`.

## Write the policies

One `allOf` per rule. Each expression names a part of the request and an attribute path, then an operator and a value.
`resourceVal("authorId")` refers to an attribute of the resource rather than a literal, so the last expression of the
second policy compares the subject's ID with the article's author:

```kotlin
```
{src="src/main/kotlin/Blog.kt" include-symbol="articlePolicies"}

A policy that refers to an attribute the request does not have is denied, not an error. [](policies.md) lists every
operator.

## Enforce them

`EnforcementPointDefault(allow, deny)` builds an in-memory decision point from the policies. The service builds an
`AccessRequest` for each operation and calls `enforceAuthorization` before doing anything:

```kotlin
```
{src="src/main/kotlin/Blog.kt" include-symbol="ArticleService"}

When access is denied, `enforceAuthorization` throws `NotAuthorizedException` and the method ends there. The exception
carries the evaluated request and the properties of the denial, for logging or for an HTTP status mapping.

`listArticles` shows the other operation an enforcement point offers. `filterAuthorization` evaluates the same
subject and action against each resource and returns those that were granted.

## Run it

```kotlin
val articles = ArticleStore(listOf(Article("a1", "Warden", authorId = "ada")))
val service = ArticleService(articles, EnforcementPointDefault(articlePolicies))

val bob = User(id = "bob", roles = emptyList())
service.readArticle(bob, "a1")      // returns the article
service.deleteArticle(bob, "a1")    // throws NotAuthorizedException
```

The service never mentions roles, authors or URLs. Those live in the policies, which the tests in [](testing.md)
exercise directly.

## Next steps

- [](decision-and-enforcement.md): deny policies, and enriching a request that arrives with only an ID.
- [](ktor.md), [](http4k.md) or [](spring-webflux.md): make every HTTP route go through an enforcement point.
