# Policies

A policy is anything that implements `Policy`: a function from an `AccessRequest` to an `AccessResponse` whose
`access` is `Granted` or `Denied`. The DSL in `codes.laurence.warden.policy` builds the common ones.

## Expressions

An expression compares one attribute of the request with a value or with another attribute. `Exp.subject(path)`,
`Exp.action(path)`, `Exp.resource(path)` and `Exp.environment(path)` start one, and an infix operator finishes it.

### Comparison operators

`equalTo` works on any value. The ordering operators need values that are `Comparable` with each other:

```kotlin
```
{src="src/main/kotlin/Policies.kt" include-symbol="comparisonPolicies"}

### Collection operators

`isIn` tests a single attribute against a collection. `contains`, `containsAll` and `containsAny` test a collection
attribute:

```kotlin
```
{src="src/main/kotlin/Policies.kt" include-symbol="collectionPolicies"}

### Attributes on the right

The right-hand side is a literal unless it is a reference. `subjectVal`, `actionVal`, `resourceVal` and
`environmentVal` refer to attributes of the request, so two parts of the request can be compared with each other.
`Exp.value` puts a literal on the left:

```kotlin
```
{src="src/main/kotlin/Policies.kt" include-symbol="operandPolicies"}

Inside an `allOf` or `anyOf` block, `resource("authorId")` on the right-hand side would add a second expression to the
block rather than refer to the attribute. Always use the `...Val` functions for references.

### Nested attributes

A path has one or more segments. Each segment after the first steps into a nested map:

```kotlin
```
{src="src/main/kotlin/Policies.kt" include-symbol="nestedPathPolicy"}

### Missing attributes and wrong types

A path that does not resolve, because an attribute is absent or an intermediate value is not a map, evaluates to
`Denied`. Policies can therefore be written for attributes that are only sometimes present.

An attribute that is present but has the wrong shape is a programming error. `contains` on a value that is not a
collection, or `greaterThan` between values that cannot be compared, throws `BadExpressionException`:

```kotlin
```
{src="src/test/kotlin/PolicyOperatorsTest.kt" include-symbol="PolicyOperatorsTest"}

## Boolean policies

`allOf` is granted when every nested policy is granted, `anyOf` when at least one is. Both are denied when empty.
`not` inverts a policy:

```kotlin
```
{src="src/main/kotlin/Policies.kt" include-symbol="allOfPolicy,anyOfPolicy,notPolicy"}

Inside a block, `subject(...)`, `action(...)`, `resource(...)` and `environment(...)` add an expression to the block.
Blocks nest, and `notAllOf` and `notAnyOf` add a negated block. `add` adds any `Policy`, including your own:

```kotlin
```
{src="src/main/kotlin/Policies.kt" include-symbol="nestedBooleanPolicy"}

## Policies over the members of a collection

When an attribute is a collection of maps, `forAnyMember` and `forAllMembers` evaluate expressions against each
member. Inside the block, `attribute(path)` refers to the member, and the `...Val` references still refer to the
request:

```kotlin
```
{src="src/main/kotlin/Policies.kt" include-symbol="collaboratorMayEdit,allCollaboratorsVerified"}

`forAnyMember` is denied for an empty collection. `forAllMembers` is granted for one. Both are denied when the attribute
is missing or is not a collection of maps.

## Writing a policy

Implement `Policy` when a rule does not fit the DSL. The response carries properties that explain the outcome; an
enforcement point puts those of a denial into the `NotAuthorizedException`:

```kotlin
```
{src="src/main/kotlin/Policies.kt" include-symbol="BusinessHoursPolicy"}

Policies are evaluated on every request, so keep them free of I/O. Attributes that must be looked up belong in an
information point, described in [](decision-and-enforcement.md).
