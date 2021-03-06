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
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    }
}
