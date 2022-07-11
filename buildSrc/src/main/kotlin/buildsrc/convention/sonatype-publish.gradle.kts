package buildsrc.convention

import buildsrc.config.createWardenPom
import buildsrc.config.credentialsAction
import buildsrc.config.isKotlinMultiplatformJavaEnabled
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin

plugins {
    `maven-publish`
    signing
}

description = "Configuration for publishing to Sonatype Maven Central"

// in $GRADLE_USER_HOME/gradle.properties, set
// sonatypeRepositoryUsername=...
// sonatypeRepositoryPassword=...
// Or set environment variables
// ORG_GRADLE_PROJECT_sonatypeRepositoryUsername=...
// ORG_GRADLE_PROJECT_sonatypeRepositoryPassword=...
val sonatypeRepositoryCredentials: Provider<Action<PasswordCredentials>> =
    providers.credentialsAction("sonatypeRepository")

val sonatypeRepositoryReleaseUrl: Provider<String> = provider {
    if (version.toString().endsWith("SNAPSHOT")) {
        "https://s01.oss.sonatype.org/content/repositories/snapshots/"
    } else {
        "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
    }
}

val signingKeyId: Provider<String> =
    providers.gradleProperty("signing.keyId")
val signingKey: Provider<String> =
    providers.gradleProperty("signing.key")
val signingPassword: Provider<String> =
    providers.gradleProperty("signing.password")
val signingSecretKeyRingFile: Provider<String> =
    providers.gradleProperty("signing.secretKeyRingFile")


tasks.withType<AbstractPublishToMaven>().configureEach {
    // Gradle warns about some signing tasks using publishing task outputs without explicit
    // dependencies. I'm not going to go through them all and fix them, so here's a quick fix.
    dependsOn(tasks.withType<Sign>())
    mustRunAfter(tasks.withType<Sign>())

    doLast {
        logger.lifecycle("[${this.name}] ${project.group}:${project.name}:${project.version}")
    }
}

afterEvaluate {
    // Register signatures afterEvaluate, otherwise the signing plugin creates the signing tasks
    // too early, before all the publications are added.

    if (sonatypeRepositoryCredentials.isPresent()) {
        // Use .all { }, not .configureEach { }, otherwise the signing plugin doesn't create the
        // signing tasks soon enough.
        publishing.publications.withType<MavenPublication>().all {
            signing.sign(this)
            logger.lifecycle("configuring signature for publication ${this.name}")
        }
    }
}

signing {
    if (sonatypeRepositoryCredentials.isPresent()) {
        if (signingKeyId.isPresent() && signingKey.isPresent() && signingPassword.isPresent()) {
            useInMemoryPgpKeys(signingKeyId.get(), signingKey.get(), signingPassword.get())
        } else {
            useGpgCmd()
        }
    }
}


publishing {
    if (sonatypeRepositoryCredentials.isPresent()) {
        repositories {
            maven(sonatypeRepositoryReleaseUrl) {
                name = "sonatype"
                credentials(sonatypeRepositoryCredentials.get())
            }
            // publish to local dir, for testing
            maven(rootProject.layout.buildDirectory.dir("maven-internal")) {
                name = "LocalProjectDir"
            }
        }
        publications.withType<MavenPublication>().configureEach {
            createWardenPom()
        }
    }
}

plugins.withType<KotlinMultiplatformPlugin>().configureEach {
    publishing.publications.withType<MavenPublication>().configureEach {
        // if required, do specific Kotlin Multiplatform configuration
        // Kotlin Multiplatform automatically registers publications
    }
}

plugins.withType<JavaPlugin>().configureEach {
    afterEvaluate {
        // Here we only want to apply config to non-KMP projects, so check to only do that if the
        // KMP plugin isn't also enabled (it automatically applies the Java plugin).
        if (!isKotlinMultiplatformJavaEnabled()) {
            publishing.publications.create<MavenPublication>("mavenJava") {
                from(components["java"])
//                artifact(tasks["sourcesJar"])
            }
        }
    }
}

fun Project.javadocStubTask(): Jar {

    // use creating, not registering, because the signing plugin sucks
    val javadocJarStub by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Stub javadoc.jar artifact (required by Maven Central)"
        archiveClassifier.set("javadoc")
    }

    tasks.withType<AbstractPublishToMaven>().all {
        dependsOn(javadocJarStub)
    }

    publishing {
        publications.withType<MavenPublication>().configureEach {
            artifact(javadocJarStub)
        }
    }

    if (sonatypeRepositoryCredentials.isPresent()) {
        val signingTasks = signing.sign(javadocJarStub)
        tasks.withType<AbstractPublishToMaven>().all {
            signingTasks.forEach { dependsOn(it) }
        }
    }

    return javadocJarStub
}
