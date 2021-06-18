# Warden

![Build Status](https://github.com/lgwillmore/warden/actions/workflows/cicd_actions.yml/badge.svg?branch=main) [![Netlify Status](https://api.netlify.com/api/v1/badges/0d20e576-551e-42be-9e8c-66355d420603/deploy-status)](https://app.netlify.com/sites/warden-kotlin/deploys)

>A kotlin implementation of light weight Attribute Based Access Control (ABAC).

**[FULL DOCUMENTATION](https://warden-kotlin.netlify.com/)**

## A Quick Intro

Let us say we have a blog site and we have the following entities: `User` and `Article`.

We would like the following authorization rules:

 - Any `User` can read any `Article`
 - The author of an `Article` can modify and delete their `Article`
 - A `User` must be be an Author to be able to create an article.
 
### Policies
 
We can use the **Warden** policy DSL to build these rules in a way that is not tightly coupled to our routes, and could in theory be used anywhere that we would like to check user permissions.
 
Here is a list of our `Article` Policies.
```kotlin
val policies = listOf(
    // Any `User` can read any `Article`
    allOf{
        resource("type") equalTo "Article"
        action("type") equalTo "READ"
    },
    // The author of an `Article` can read, modify and delete their `Article`
    allOf {
        resource("type") equalTo "Article"
        action("type") isIn listOf("MODIFY", "DELETE")
        subject("id") equalTo resource("authorID")
    },
    // A `User` must be be an Author to be able to create an article.
    allOf {
        resource("type") equalTo "Article"
        action("type") equalTo "CREATE"
        subject("roles") contains "Author"
    }
)
 ```
Here we have a single `AllOf` policy for each one of our logical desired policies. These each define all of the conditions that must be met to satisfy what we want logically.

Having each one as its own separate policy might be preferred by some, but we could also build this in a way which kept all of the authorization logic for `Article` more clearly grouped. We can leverage nested `AllOf` and `AnyOf` Policies to achieve this.
```kotlin
val articlePolicy = allOf {
    resource("type") equalTo "Article"
    anyOf{
        // Any `User` can read any `Article`
        action("type") equalTo "READ"
        // The author of an `Article` can read, modify and delete their `Article`
        allOf {
            action("type") isIn listOf("MODIFY", "DELETE")
            subject("id") equalTo resource("authorID")
        }
        // A `User` must be be an Author to be able to create an article.
        allOf {
            action("type") equalTo "CREATE"
            subject("roles") contains "Author"
        }
    }
}
```
Here, we have a single `Article` policy, and we use a nested `AnyOf` policy to define our 3 access cases for an `Article`. There is quite a lot to digest there, but more detail can be found in the Policy documentation.

Now that we have our policies, we can enforce them.

### Enforcement

An `EnforcementPoint` allows us to protect any given execution path with an Exception. If a request is not authorized, an Exception is thrown. Let us say we have the following business domain objects and helper functions
```kotlin
interface HasAttributes {
    fun attributes(): Map<String, Any?>
}

class HasAttributesComponent : HasAttributes {
    override fun attributes(): Map<String, Any?> {
        TODO("Implement some way of transforming to a Map")
    }
}

data class User(
    val id: String
) : HasAttributes by HasAttributesComponent()

class Article(
    val id: String?,
    val authorID: String
) : HasAttributes by HasAttributesComponent()
```

We have our `User` and `Article` and we have defined a simple interface for getting the attribute map from each of our entities. Now let us look at a Service layer function for reading an Article.

```kotlin
val enforcementPoint = EnforcementPointDefault(policies)

suspend fun readArticle(user: User, articleID: String): Article {
    val article = getArticleByID(articleID)
    enforcementPoint.enforceAuthorization(
        AccessRequest(
            subject = user.attributes(),
            action = mapOf("type" to "READ"),
            resource = article.attributes()
        )
    )
    return article
}
```

Here we have an instance of an `EnforcementPoint` constructed from our policies.

We also have our service layer function with an already resolved `User` instance (possibly from a preceding authentication layer), and the user is requesting an `Article` by ID. The following occurs:

- We retrieve the `Article`.
- We build our `AccessRequest` from the attributes of the `User` and `Article`, and we manually construct the Action attributes based on the purpose of the function.
- We use the `EnforcementPoint` to check the request, and then return the result.

At the point of enforcement, if no access had been granted, a `NotAuthorizedException` would have been thrown and the article would not be returned. If access is granted, execution continues without a hitch.

> #### NOTE
> We have not looked at a `DecisionPoint` in this quick intro, as it is not strictly needed in the shortest path from policies to enforcement. It is the layer that lets us host our policies and decisioning independently of enforcement.
 
