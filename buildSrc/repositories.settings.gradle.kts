// A settings.gradle.kts plugin for defining shared repositories used by both buildSrc and the root project

@Suppress("UnstableApiUsage") // Central declaration of repositories is an incubating feature
dependencyResolutionManagement {

    repositories {
        jcenter()
        mavenCentral()
        gradlePluginPortal()
        google()
    }

    pluginManagement {
        repositories {
            jcenter()
            gradlePluginPortal()
            mavenCentral()
            google()
        }

        plugins {
            id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
        }
    }
}
