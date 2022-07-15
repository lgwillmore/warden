import buildsrc.config.createWardenPom

plugins {
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`artifactory-publish`
}

val mockkVersion: String by project
val assertKVersion: String by project

dependencies {
    implementation(kotlin("reflect"))

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("atts") {
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
            publications("atts")
        }
    }
}

tasks.build.configure {
    dependsOn(tasks.kotlinSourcesJar)
}

tasks.artifactoryPublish.configure {
    dependsOn(tasks.build)
}
