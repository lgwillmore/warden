rootProject.name = "warden"
include("core", "ktor", "docs")
project(":core").name = "warden-core-jvm"
project(":ktor").name = "warden-ktor"
