pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    // Downloads the JDK the toolchain asks for when it is not installed.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    ":core",
    ":atts",
    ":ktor",
    ":docs-snippets",
)

project(":core").name = "warden-core"
project(":atts").name = "warden-atts"
project(":ktor").name = "warden-ktor"

// Lives inside the Writerside module, whose topics quote its sources.
project(":docs-snippets").projectDir = file("docs/guides/snippets")

rootProject.name = "warden"
