import buildsrc.config.createWardenPom
import buildsrc.config.defaults
import buildsrc.config.publish

plugins {
    buildsrc.convention.`kotlin-multiplatform`
    buildsrc.convention.`artifactory-publish`
}

val assertKVersion: String by project
val mockkVersion: String by project
val kotlinVersion: String by project

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.mockk:mockk:$mockkVersion")
                implementation("com.willowtreeapps.assertk:assertk:$assertKVersion")
            }
        }

        val jvmMain by getting {
            // Default source set for JVM-specific sources and dependencies:
            dependencies {
                implementation(kotlin("reflect"))
            }
        }

        val jvmTest by getting {
            // JVM-specific tests and their dependencies:
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("coreJVM") {
            groupId = "codes.laurence.warden"
            artifactId = "warden-core"

//            artifact("$buildDir/libs/warden-core-jvm-${project.version}-sources.jar") {
//                classifier = "sources"
//            }
//            artifact("$buildDir/libs/warden-core-jvm-${project.version}.jar")
//
            artifact(tasks.jvmJar)
            artifact(tasks.jvmSourcesJar)

            createWardenPom(artifactId) // TODO should the pom name be the root project name?
        }
    }
}

artifactory {
    publish {
        defaults {
            publications("coreJVM")
        }
    }
}

tasks.artifactoryPublish.configure {
    dependsOn(tasks.build)
//    dependsOn(tasks.publishJvmPublicationToMavenLocal)
}
