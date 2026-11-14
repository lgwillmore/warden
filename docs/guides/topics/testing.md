# Testing

Everything in Warden is a plain object, so policies, services and routes can be tested without a server, a database
or any mocking.

## Test policies directly

`Policy.checkAuthorized` is an ordinary function. The decision point evaluates the allow list as an `AnyOf`, so wrap
your policies in one and call it with hand-built requests:

```kotlin
```
{src="src/test/kotlin/BlogPoliciesTest.kt" include-symbol="BlogPoliciesTest"}

These tests pin down the rules independently of any code that uses them. A rule that is hard to test this way is
usually a rule that needs an information point.

## Test a service

Build the service with `EnforcementPointDefault(policies)` and assert that denied operations throw before they change
anything:

```kotlin
```
{src="src/test/kotlin/BlogServiceTest.kt" include-symbol="BlogServiceTest"}

## Test the decision point

A decision point with information points and deny policies is tested the same way. Pass functions for what the
information points fetch, so the test controls the roles and the clock:

```kotlin
```
{src="src/test/kotlin/DecisionsTest.kt" include-symbol="DecisionsTest"}

## Test a Ktor application

Ktor's `testApplication` runs the module in process. Besides the granted and denied cases, assert that a route which
forgets to enforce is refused by the plugin, and that the routes you meant to leave open are open:

```kotlin
```
{src="src/test/kotlin/KtorAppTest.kt" include-symbol="KtorAppTest"}

The refusal has a fixed body, `Not Authorized: EnforcementPoint not enforced`, which distinguishes it from a denial
mapped by `StatusPages`.
