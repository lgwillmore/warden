---
title: 1) Policies
---
A policy encapsulates the logic for evaluating if a particular condition or set of conditions has been satisfied by the
access Request.

## Expression Policies

Expression Policies are the simplest statements we can make about a particular access request. They form a single
expression.

### Comparison operators

```kotlin
//Equality
var v = Exp.value("foo") equalTo "Bar"

// Greater than
v = Exp.value(1) greaterThan 2

// Greater than equal
v = Exp.value(1) greaterThanEqual 2

// Less than
v = Exp.value(1) lessThan 2

// Less than equal
v = Exp.value(1) lessThanEqual 2
```

### Collection Operators

```kotlin
// isIn
var v = Exp.value("foo") isIn listOf("bar")

// Contains
v = Exp.value(listOf("foo")) contains "bar"

// Contains All
v = Exp.value(listOf("foo")) containsAll listOf("bar")

// Contains Any
v = Exp.value(listOf("foo")) containsAny listOf("bar")
```

### Attribute and Value Operands

We can access the values of any attribute in the access request.

```kotlin
// Reference subject attributes
var v = Exp.subject("foo") equalTo subjectVal("Bar")

// Reference action attributes
v = Exp.action("foo") equalTo actionVal("Bar")

// Reference resource attributes
v = Exp.resource("foo") equalTo resourceVal("Bar")

// Reference environment attributes
v = Exp.environment("foo") equalTo environmentVal("Bar")

// Reference plain values
v = Exp.value("foo") equalTo "Bar"
```

### Nested Attributes

We can access the values of nested attribute maps with a path.

```kotlin
// Example subject map
val subjectAttributes = mapOf(
    "foo" to mapOf(
        "bar" to "fizz"
    )
)

// Reference attributes
var v = Exp.subject("foo", "bar") equalTo resourceVal("foobar")
```

## Boolean Policies

We can build more complex policies with boolean operators of nested policies.

### AllOf

The equivalent of an AND expression over child expressions. All child policies must allow access for the policy as a
whole to allow access. An empty `allOf` will deny access.

```kotlin
allOf {
    subject("foo") equalTo resourceVal("bar")
    action("fizz") equalTo environmentVal("bang")
}
```

### AnyOf

The equivalent of an OR expression over child expressions. Any child policy must allow access for the policy as a whole
to allow access. An empty `anyOf` will deny access.

```kotlin
anyOf {
    subject("foo") equalTo resourceVal("bar")
    action("fizz") equalTo environmentVal("bang")
}
```

### Not

We can negate any policy with a not policy.

```kotlin
not(allOf {
    subject("foo") equalTo resourceVal("bar")
    action("fizz") equalTo environmentVal("bang")
})
```

## Collection Member Policies

We can build more complex policies around the members of a collection attribute.

### For Any Member

The policy will allow access if any member of the attribute collection is allowed access for all nested policies.

```kotlin
Exp.action("foo") forAnyMember {
    attribute("bar", "fizz") equalTo subjectVal("bang")
    attribute("widget",) equalTo "flubber"
}
```

### For All Members

The policy will allow access if all members of the attribute collection allow access for all nested policies.

```kotlin
Exp.action("foo") forAllMembers {
    attribute("bar", "fizz") equalTo subjectVal("bang")
    attribute("widget",) equalTo "flubber"
}
```