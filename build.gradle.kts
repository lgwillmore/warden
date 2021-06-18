val projectVersion: String by project
plugins {
    kotlin("jvm") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
}

group = "codes.laurence.warden"
version = projectVersion

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        disabledRules.set(
            setOf("no-wildcard-imports", "filename")
        )
    }
}

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4.21.0")
    }
}
