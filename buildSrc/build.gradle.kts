import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.6.21"
    // Gradle uses an embedded Kotlin with version 1.4
    // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
    // but it's safe to use 1.6.21, as long as the language level is set to 1.4
    // (the kotlin-dsl plugin does this).
}

// set the versions of Gradle plugins that the subprojects will use here
val kotlinPluginVersion: String = "1.6.21"
val artifactoryPluginVersion: String = "4.28.4"
val orchidPluginVersion: String = "0.21.1"
val ktlintPluginVersion: String = "10.3.0"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinPluginVersion"))
    implementation("org.jetbrains.kotlin:kotlin-serialization")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinPluginVersion")

    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:$artifactoryPluginVersion")

    implementation("gradle.plugin.com.eden:orchidPlugin:$orchidPluginVersion")

    implementation("org.jlleitschuh.gradle:ktlint-gradle:$ktlintPluginVersion")
}

val gradleJvmTarget = "11"

tasks.withType<KotlinCompile>().configureEach {

    kotlinOptions {
        jvmTarget = gradleJvmTarget
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(gradleJvmTarget))
    }

    kotlinDslPluginOptions {
        jvmTarget.set(gradleJvmTarget)
    }
}
