---
title: 'Attributes'
---

Warden provides some jvm tools for working with the attributes of your data classes and class instances. These tools are
not required by the core components and are included as a convenience.

## Gradle installation

![version](https://img.shields.io/github/v/tag/lgwillmore/warden?include_prereleases&label=release)

```kotlin
dependencies {
    implementation("codes.laurence.warden:warden-atts:0.2.0")
}
```

## HasAttsI and Atts

The 2 core pieces of the attribute toolset are the `HasAttsI` interface and a type alias for a Map of
atts `Atts`. This allows us to easily convert the data classes that represent our subjects, actions,
resource and environment into atts at any point we are building an `AccessRequest`.

### Single Inheritance

The toolset comes with a quick reflection based implementation `HasAtts`, and its usage is as simple as extending
the class.

```kotlin
data class User(
    val id: String,
    val email: String,
    val emailIsVerified: Boolean,
    val handle: String,
    val firstName: String? = null,
    val lastName: String? = null
) : HasAtts()
```

### Multiple Inheritance

Often, we need to implement multiple interfaces and then need to choose our single concrete implementation. In that case
there are helpers for easily implementing `HasAttsI` such as the `attsOf` function.

```kotlin
data class UserNew(
    val email: String,
    val handle: String,
    val password: String
) : HasAttsI, Validatable {

    override fun atts() = attsOf(this)

}
```

## AttType

It is highly likely that the first and primary attribute that a domain object needs is some sort of `type` attribute. It
is also highly likely that this type will be shared by several data classes representing different aspects of the same
domain object. `AttType` helps us handle this.

`AttType` takes `type` and an optional `typeKeyword` constructor arguments:

- `type`: A string defining the type of the Atts
- `typeKeyword`: Optional aliasing of the key that the type will be mapped to in order to avoid conflicts with other
  atts.

`AttType` also implements `HasAttsI` and we can see a usage example below. In particular, it is convenient
for constructing atts when we do not have an instance of some domain data class for building an `AccessRequest`.

```kotlin
val USER_TYPE = AttType(
    type = "USER",
    typeKeyword = "authType"
)

println(USER_TYPE.atts())

/**
 * Output:
 *
 *  {authType=USER}
 */

println(
    USER_TYPE.withAtts(
        "id" to "12345"
    )
)

/**
 * Output:
 *
 *  {authType=USER, id=12345}
 */

```

### HasAtts and AttType

The `AttType` can then be used across your `HasAttsI` implementing data classes as seen in the example
below.

```kotlin

val USER_TYPE = AttType(
    type = "USER",
    typeKeyword = "authType"
)

// Single Inheritance
data class User(
    val id: String,
    val email: String,
    val emailIsVerified: Boolean,
    val handle: String,
    val firstName: String? = null,
    val lastName: String? = null
) : HasAtts(USER_TYPE)


// Multiple Inheritance
data class UserNew(
    val email: String,
    val handle: String,
    val password: String
) : HasAttsI, Validatable() {

    override fun atts() = USER_TYPE.withAtts(attsOf(this))

}
```

## Helpers and Extensions

### `attsOf`

The `attsOf` function is used to recursively and reflectively convert the instance of a class into `Atts`.
It will also handle the conversion of aggregates of nested implementers of the `HasAttsI` interface.

### `withAtts`

`withAtts` is an extension function on `HasAttsI` that lets us conveniently merge other atts into the
atts of the original object.
