// A settings.gradle.kts plugin for defining shared repositories used by both buildSrc and the root project

@Suppress("UnstableApiUsage") // Central declaration of repositories is an incubating feature
dependencyResolutionManagement {

    repositories {
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        google()
    }

    pluginManagement {
        repositories {
            mavenCentral()
            jcenter()
            gradlePluginPortal()
            google()
        }

        plugins {
            id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
        }
    }
}
