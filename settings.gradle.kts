rootProject.name = "warden"
enableFeaturePreview("GRADLE_METADATA")
include("core", "ktor", "docs")
project(":core").name = "warden-core"
project(":ktor").name = "warden-ktor"
