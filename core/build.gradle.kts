import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val projectVersion: String by project
val mockkVersion: String by project

plugins {
    kotlin("multiplatform")
    id("com.jfrog.artifactory")
    id("maven-publish")
    id("org.jetbrains.dokka") version "0.10.0"
}

repositories {
    mavenCentral()
    jcenter()
}


group = "codes.laurence.warden"
version = projectVersion

val assertKVersion: String by project


kotlin {
    jvm {}
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
                implementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("coreJVM") {
            groupId = "codes.laurence.warden"
            artifactId = "warden-core"
            version = projectVersion

            artifact("$buildDir/libs/warden-core-jvm-metadata-${project.version}-sources.jar") {
                classifier = "sources"
            }
            artifact("$buildDir/libs/warden-core-jvm-${project.version}.jar")
        }
    }
}

artifactory {
    setContextUrl("https://laurencecodes.jfrog.io/artifactory")
    publish(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig> {
        repository(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.DoubleDelegateWrapper> {
            setProperty("repoKey", "codes.laurence.warden")
            setProperty("username", System.getenv("JFROG_USER"))
            setProperty("password", System.getenv("JFROG_PASSWORD"))
            setProperty("maven", true)
        })
        defaults(delegateClosureOf<org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask> {
            publications("coreJVM")
        })
    })

}

tasks {
    val build by existing

    artifactoryPublish {
        dependsOn(build)
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "../build/dokka-core"
    }
}