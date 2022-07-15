---
title: 'Attributes'
---

Warden provides some jvm tools for working with the attributes of your data classes and class
instances. These tools are not required by the core components and are included as a convenience.

## `HasAttributesI` and `Attributes`

The 2 core pieces of the attribute toolset are the `HasAttributesI` interface and a type alias for a
Map of attributes `Attributes`. This allows us to easily convert the data classes that represent our
subjects, actions, resource and environment into attributes at any point we are building
an `AccessRequest`.

### Single Inheritance

The toolset comes with a quick reflection based implementation `HasAttributes`, and its usage is as
simple as extending
the class.

```kotlin
data class User(
    val id: String,
    val email: String,
    val emailIsVerified: Boolean,
    val handle: String,
    val firstName: String? = null,
    val lastName: String? = null
) : HasAttributes()
```

### Multiple Inheritance

Often, we need to implement multiple interfaces and then need to choose our single concrete
implementation. In that case there are helpers for easily implementing `HasAttributesI` such as
the `attributeOf` function.

```kotlin
data class UserNew(
    val email: String,
    val handle: String,
    val password: String
) : HasAttributesI, Validatable {

    override fun attributes() = attributesOf(this)

}
```

## `AttributeType`

It is highly likely that the first and primary attribute that a domain object needs is some sort
of `type` attribute. It is also highly likely that this type will be shared by several data classes
representing different
aspects of the same domain object. `AttributeType` helps us handle this.

`AttributeType` takes `type` and an optional `typeKeyword` constructor arguments:

- `type`: A string defining the type of the Attributes
- `typeKeyword`: Optional aliasing of the key that the type will be mapped to in order to avoid
  conflicts with other
  attributes.

`AttributeType` also implements `HasAttributesI` and we can see a usage example below. In
particular, it is convenient
for constructing attributes when we do not have an instance of some domain data class for building
an `AccessRequest`.

```kotlin
val USER_TYPE = AttributeType(
    type = "USER",
    typeKeyword = "authType"
)

println(USER_TYPE.attributes())

/**
 * Output:
 *
 *  {authType=USER}
 */

println(
    USER_TYPE.withAttributes(
        "id" to "12345"
    )
)

/**
 * Output:
 *
 *  {authType=USER, id=12345}
 */

```

### `HasAttributes` and `AttributeType`

The `AttributeType` can then be used across your `HasAttributesI` implementing data classes as seen
in the example
below.

```kotlin
val USER_TYPE = AttributeType(
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
) : HasAttributes(USER_TYPE)


// Multiple Inheritance
data class UserNew(
    val email: String,
    val handle: String,
    val password: String
) : HasAttributesI, Validatable() {

    override fun attributes() = USER_TYPE.withAttributes(attributesOf(this))

}
```

## Helpers and Extensions

### `attributesOf`

The `attributesOf` function is used to recursively and reflectively convert the instance of a class
into `Attributes`. It will also handle the conversion of aggregates of nested implementers of
the `HasAttributesI` interface.

### `withAttributes`

`withAttributes` is an extension function on `HasAttributesI` that lets us conveniently merge other
attributes into the attributes of the original object.
