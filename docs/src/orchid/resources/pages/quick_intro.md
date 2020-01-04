---
title: 'Quick Intro'
---

Let us say we have a blog site and we have the following entities: `User` and `Article`.

We would like the following authorization rules:

 - Any `User` can read any `Article`
 - The author of an `Article` can modify and delete their `Article`
 - A `User` must be be an Author to be able to create an article.
 
 ## Policies
 
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

Now that we have our policies, we can 
 