---
title: 'Quick Intro'
---

Let us say we have a blog site and we have the following entities: `User` and `Article`.

We would like the following authorization rules:

- Any `User` can read any `Article`
- The author of an `Article` can modify and delete their `Article`
- A `User` must be be an Author to be able to create an article.

## Policies

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
    // A User must be be an Author to be able to create an article.
    allOf {
        resource("type") equalTo "Article"
        action("type") equalTo "CREATE"
        subject("roles") contains "Author"
    }
)
 ```

Here we have a single `AllOf` policy for each one of our logical desired policies. These each define all of the
conditions that must be met to satisfy what we want logically.

Now that we have our policies, we can enforce them.

## Enforcement

An `EnforcementPoint` allows us to protect any given execution path with an Exception. If a request is not authorized,
an Exception is thrown.

`EnforcementPoint`s and `DecisionPoint`s both work on maps of attributes for subject, action, resource, environment.
Maps allow for subsets of attributes as well as the merging of attributes.

Let us say we have the following business domain objects, and we are using the Warden `HasAttributes` helper class to
provide easy conversion into Maps of attributes.

```kotlin

data class User(
    val id: String
) : HasAttributes()

class Article(
    val id: String?,
    val authorID: String
) : HasAttributes()
```

We have our `User` and `Article` and we have defined a simple interface for getting the attribute map from each of our
entities. Now let us look at a Service layer function for reading an Article.

```kotlin
val enforcementPoint = EnforcementPointDefault(policies)

suspend fun readArticle(user: User, articleID: String): Article {
    // Fetch the article
    val article = getArticleByID(articleID)
    // Check authorization with our EnforcementPoint
    enforcementPoint.enforceAuthorization(
        AccessRequest(
            subject = user.attributes(),
            action = mapOf("type" to "READ"),
            resource = article.attributes()
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
