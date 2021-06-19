---
title: 2) Policy Source
---

The `PolicySource` interface alleviates the responsibility of retrieving available policies from the `DecisionPoint`.
They could be in memory, or stored in a DB or available from another service.

A `PolicySource` defines a single method for retrieving a set of `deny` and `allow` policies based on an `AccessRequest` (or
left null to fetch all policies).

The simplest Policy source is `PolicySourceInMemory`.
```kotlin
val policySource = PolicySourceInMemory(
    allow = listOf(
        // Some set of policies that allow access.
    ),
    deny = listOf(
        // Some set of policies that deny access.
    )
)
```



