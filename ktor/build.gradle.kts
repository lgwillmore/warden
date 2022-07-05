import buildsrc.config.createWardenPom
import buildsrc.config.defaults
import buildsrc.config.publish

plugins {
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.publishing
    id("com.jfrog.artifactory")
}

val ktorVersion: String by project
val mockkVersion: String by project
val assertKVersion: String by project
val slf4jVersion: String by project

dependencies {
    implementation(projects.wardenCore)

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("org.slf4j", "slf4j-api", slf4jVersion)

    testImplementation("io.ktor", "ktor-websockets", ktorVersion)
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testImplementation(kotlin("test"))
}

val artifactName = "warden-ktor"
val artifactGroup = "codes.laurence.warden"

publishing {
    publications {
        create<MavenPublication>("ktor") {
            groupId = artifactGroup
            artifactId = artifactName
            version = version
            artifact("$buildDir/libs/warden-ktor-${project.version}-sources.jar") {
                classifier = "sources"
            }

            artifact("$buildDir/libs/warden-ktor-${project.version}.jar")

            createWardenPom(artifactName) // TODO should the pom name be the root project name?
        }
    }
}


artifactory {
    setContextUrl("https://laurencecodes.jfrog.io/artifactory")
    publish {
        defaults {
            publications("ktor")
        }
    }
//    publish(
//        delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig> {
//            repository(
//                delegateClosureOf<org.jfrog.gradle.plugin.artifactory.dsl.DoubleDelegateWrapper> {
//                    setProperty("repoKey", "codes.laurence.warden")
//                    setProperty("username", System.getenv("JFROG_USER"))
//                    setProperty("password", System.getenv("JFROG_PASSWORD"))
//                    setProperty("maven", true)
//                }
//            )
//            defaults(
//                delegateClosureOf<org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask> {
//                    publications("ktor")
//                }
//            )
//        }
//    )
}

tasks.build.configure {
    dependsOn(tasks.kotlinSourcesJar)
}

tasks.artifactoryPublish.configure {
    dependsOn(tasks.build)
}
