## Gradle

![version](https://img.shields.io/github/v/tag/lgwillmore/warden?include_prereleases&label=release)

**Add the repo and core dependency**
```kotlin
repositories {
    maven(url = "https://laurencecodes.jfrog.io/artifactory/codes.laurence.warden/")
}

dependencies {
    //ABAC
    implementation("codes.laurence.warden:warden-core:0.0.1")
}
```
**And to include the Ktor plugin**
```kotlin

dependencies {
    //ABAC
    implementation("codes.laurence.warden:warden-ktor:0.0.1")
}
```
