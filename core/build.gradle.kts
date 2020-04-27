val projectVersion: String by project
val mockkVersion: String by project

plugins {
    kotlin("multiplatform") version "1.3.61"
    id("com.jfrog.bintray") version "1.8.4"
    id("maven-publish")
    id("org.jetbrains.dokka") version "0.10.0"
}

repositories {
    mavenCentral()
    jcenter()
}



version = projectVersion


kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("io.mockk:mockk-common:$mockkVersion")
                implementation("com.willowtreeapps.assertk:assertk-common:0.14")
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        // JVM-specific tests and their dependencies:
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("io.mockk:mockk:1.9.3")
                implementation("com.willowtreeapps.assertk:assertk-jvm:0.20")
            }
        }

    }
}

publishing {
    publications {
        register("coreJVM", MavenPublication::class) {
            groupId = "codes.laurence.warden"
            artifactId = "warden-core-jvm"
            version = projectVersion
            artifact("build/libs/core-jvm-${project.version}.jar")
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    pkg.apply {
        repo = "codes.laurence.warden"
        name = "warden-core-jvm"
        websiteUrl = "https://warden-kotlin.netlify.com/"
        vcsUrl = "https://github.com/lgwillmore/warden"
        issueTrackerUrl = "https://github.com/lgwillmore/warden/issues"
        setLabels("Kotlin", "ABAC", "Authorization")
        setLicenses("MIT")
        setPublications("coreJVM")
        version.apply {
            name = project.version.toString()
            desc = "SNAPSHOT release"
//            val timeZone = org.gradle.internal.impldep.org.joda.time.DateTimeZone.UTC
//            released  = org.gradle.internal.impldep.org.joda.time.DateTime(timeZone).toString()
        }
    }

}

tasks {
    val build by existing

    bintrayUpload {
        dependsOn(build)
    }

    dokka{
        outputFormat = "html"
        outputDirectory = "../build/dokka-core"
    }
}

