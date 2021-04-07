val projectVersion: String by project
plugins {
    kotlin("jvm") version "1.4.20"
}

group = "codes.laurence.warden"
version = projectVersion

allprojects {
    repositories {
        mavenCentral()
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
