import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.9.10"
}

// set the versions of Gradle plugins that the subprojects will use here
val kotlinPluginVersion: String = "1.9.10"
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

val gradleJvmTarget = "1.8"
val gradleJvmTargetInt = 8

tasks.withType<KotlinCompile>().configureEach {

    kotlinOptions {
        jvmTarget = gradleJvmTarget
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(gradleJvmTargetInt))
    }

    kotlinDslPluginOptions {
        jvmTarget.set(gradleJvmTarget)
    }
}
