# Attributes

`warden-atts` turns your classes into the attribute maps that access requests are made of. The core modules do not
depend on it; it is a convenience for the code that builds requests.

```kotlin
dependencies {
    implementation("codes.laurence.warden:warden-atts:%warden_version%")
}
```

## `HasAttsI` and `Atts`

`Atts` is a type alias for `Map<String, Any?>`. `HasAttsI` is the one-method interface for anything that can produce
its attributes. Request-building code depends on these two and nothing else.

## Extend `HasAtts`

`HasAtts` implements `HasAttsI` by reflection over the class's public properties. Give it an `AttType` and the type is
added to the map:

```kotlin
```
{src="src/main/kotlin/Atts.kt" include-symbol="AccountType,Account"}

```kotlin
```
{src="src/test/kotlin/AttsTest.kt" include-symbol="AttsTest"}

## Implement `HasAttsI` with `attsOf`

A class that already has a superclass cannot extend `HasAtts`. `attsOf` is the same reflection as a function:

```kotlin
```
{src="src/main/kotlin/Atts.kt" include-symbol="Form,Signup"}

## Nested values

Properties, collection elements and map values that implement `HasAttsI` are converted to maps too, so nested
attributes can be reached with a path such as `resource("owner", "id")`:

```kotlin
```
{src="src/main/kotlin/Atts.kt" include-symbol="Team"}

## `AttType`

Several classes often describe the same kind of thing: an entity, a form that creates one, a reference to one by ID.
`AttType` gives them one `type` attribute. `typeKeyword` is the key it is stored under, `attributeType` by default; the
samples use `type`.

Beyond tagging a `HasAtts`, an `AttType` builds attributes when there is no instance at hand. `withAtts` merges the type
with other attributes, and `asHasAtts` wraps the result in a `HasAttsI`:

```kotlin
```
{src="src/main/kotlin/Atts.kt" include-symbol="accountReference"}

## Merging and wrapping

`withAtts` on any `HasAttsI` adds attributes without changing the object. `HasAttsMapWrapper` makes a `HasAttsI` from a
map that already exists:

```kotlin
```
{src="src/main/kotlin/Atts.kt" include-symbol="accountInSession,accountFromClaims"}
