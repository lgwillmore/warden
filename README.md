# Warden

![Build Status](https://github.com/lgwillmore/warden/actions/workflows/test.yml/badge.svg?branch=main)
[![Maven Central](https://img.shields.io/maven-central/v/codes.laurence.warden/warden-core-jvm)](https://central.sonatype.com/artifact/codes.laurence.warden/warden-core-jvm)
[![Netlify Status](https://api.netlify.com/api/v1/badges/0d20e576-551e-42be-9e8c-66355d420603/deploy-status)](https://app.netlify.com/sites/warden-kotlin/deploys)


> **Attribute Based Access Control for Kotlin**
>
> - Simple and expressive Policy based Authorization
> - Decoupled from web frameworks

**[FULL DOCUMENTATION](https://warden-kotlin.netlify.app/)**

**Contents**

- [What is Attribute Based Access Control?](#what-is-attribute-based-access-control)
- [Advantages of ABAC](#advantages-of-abac)
- [A Quick Intro](#a-quick-intro)
    * [Policies](#policies)
    * [Enforcement](#enforcement)
- [Installation](#installation)

## What is Attribute Based Access Control?

ABAC allows us to define access/deny policies based around any conceivable attribute of a request. Attributes of:

- **The subject**: The entity performing the request/action, typically a user.
- **The action**: The action being performed e.g. A Forced update.
- **The resource**: The entity the action is related to or being performed on.
- **The environment**: Any environmental attributes eg. Time, IP address, location.

With policies defined using rules with access to this information we can then enforce our Policies in any part of a
distributed system.

You can find out more about the concepts involved here: [ABAC Overview](./docs/src/orchid/resources/pages/abac.md).

## Advantages of ABAC

ABAC has the following advantages over role based access control implemented with your flavour of web framework:

### Decouple Authorization from Routing

Defining the rules for authorization separately from your routing provides all the benefits of low coupling.

In your routing, all you have to ensure is that Authorization is checked, not how or what the Authorization rules are.

Your authorization rules are business rules and are formulated against business domain objects, not URLs. You can have
multiple URLs that need the same rules to be enforced, and this is better done in a lower layer.

### Expressive Authorization logic

Role based authorization is a subset of the rules that can be defined with ABAC. You will likely find your needs
extending beyond Role Based Authorization quickly and ABAC has you covered.

### Architectural flexibility

With ABAC, and the separation between decision and enforcement, your authorization rules can be leveraged across
systems, languages, frontend, backend. It also exposes Policies as business data for CRUD.

## A Quick Intro

Let us say we have a blog site, and we have the following entities: `User` and `Article`.

We would like the following authorization rules:

- Any `User` can read any `Article`
- The author of an `Article` can modify and delete their `Article`
- A `User` must be an Author to be able to create an article.

### Policies

We can use the **Warden** policy DSL to build these rules in a way that is not tightly coupled to our routes, and could
in theory be used anywhere that we would like to check user permissions.

Here is a list of our `Article` Policies.

```kotlin
val policies = listOf(
    // Any User can read any Article
    allOf {
        resource("type") equalTo "Article"
        action("type") equalTo "READ"
    },
    // The author of an Article can read, modify and delete their Article
    allOf {
        resource("type") equalTo "Article"
        action("type") isIn listOf("MODIFY", "DELETE")
        subject("id") equalTo resource("authorID")
    },
    // A User must be an Author to be able to create an article.
    allOf {
        resource("type") equalTo "Article"
        action("type") equalTo "CREATE"
        subject("roles") contains "Author"
    }
)
 ```

Here we have a single `AllOf` policy for each one of our logical desired policies. These each define all conditions that
must be met to satisfy what we want logically.

Now that we have our policies, we can enforce them.

### Enforcement

An `EnforcementPoint` allows us to protect any given execution path with an Exception. If a request is not authorized,
an Exception is thrown.

`EnforcementPoint`s and `DecisionPoint`s both work on maps of attributes for subject, action, resource, environment.
Maps allow for subsets of attributes as well as the merging of attributes.

Let us say we have the following business domain objects, and we are using the Warden `HasAtts` helper class to provide
easy conversion into Maps of attributes. You can read more on the `warden-atts`
library [here](https://warden-kotlin.netlify.com/attributes).

```kotlin

data class User(
    val id: String
) : HasAtts()

class Article(
    val id: String?,
    val authorID: String
) : HasAtts()
```

We have our `User` and `Article`, now let us look at a Service layer function for reading an Article.

```kotlin
val enforcementPoint = EnforcementPointDefault(policies)

suspend fun readArticle(user: User, articleID: String): Article {
    // Fetch the article
    val article = getArticleByID(articleID)
    // Check authorization with our EnforcementPoint
    enforcementPoint.enforceAuthorization(
        AccessRequest(
            subject = user.atts(),
            action = mapOf("type" to "READ"),
            resource = article.atts()
        )
    )
    // return the article if execution was allowed to proceed.
    return article
}
```

Here we have an instance of an `EnforcementPoint` constructed from our policies.

We also have our service layer function with an already resolved `User` instance (possibly from a preceding
authentication layer), and the user is requesting an `Article` by ID. The following occurs:

- We retrieve the `Article`.
- We build our `AccessRequest` from the attributes of the `User` and `Article`, and we manually construct the Action
  attributes based on the purpose of the function.
- We use the `EnforcementPoint` to check the request, and then return the result.

At the point of enforcement, if no access had been granted, a `NotAuthorizedException` would have been thrown and the
article would not be returned. If access is granted, execution continues without a hitch.

## Installation

### Gradle (kotlin dsl)

```kotlin
repositories {
    maven(url = "https://laurencecodes.jfrog.io/artifactory/codes.laurence.warden/")
}

dependencies {
    //ABAC
    implementation("codes.laurence.warden:warden-core:0.3.0")
}
```
