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
            id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
        }
    }
}
