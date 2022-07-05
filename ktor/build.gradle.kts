import buildsrc.config.createWardenPom
import buildsrc.config.defaults
import buildsrc.config.publish

plugins {
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`artifactory-publish`
}

val ktorVersion: String by project
val mockkVersion: String by project
val assertKVersion: String by project
val slf4jVersion: String by project

dependencies {
    implementation(projects.wardenCore)

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("io.ktor:ktor-websockets:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("ktor") {
            artifact(tasks.jar)
            artifact(tasks.sourcesJar)
            artifact(tasks.javadocJar)

            createWardenPom()
        }
    }
}

artifactory {
    publish {
        defaults {
            publications("ktor")
        }
    }
}

tasks.build.configure {
    dependsOn(tasks.kotlinSourcesJar)
}

tasks.artifactoryPublish.configure {
    dependsOn(tasks.build)
}
