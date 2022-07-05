import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `kotlin-dsl`
  kotlin("jvm") version "1.6.21"
}

dependencies {
    implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:1.6.21"))
    implementation("org.jetbrains.kotlin:kotlin-serialization")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")

    // TODO Verify if build-info-extractor-gradle is still required
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.21.0")

    implementation("gradle.plugin.com.eden:orchidPlugin:0.21.1")

    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.3.0")
}

val gradleJvmTarget = "11"

tasks.withType<KotlinCompile>().configureEach {

  kotlinOptions {
    jvmTarget = gradleJvmTarget
  }

  kotlinOptions.freeCompilerArgs += listOf(
    "-opt-in=kotlin.RequiresOptIn",
    "-opt-in=kotlin.ExperimentalStdlibApi",
    "-opt-in=kotlin.time.ExperimentalTime",
  )
}

kotlin {
  jvmToolchain {
    (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(gradleJvmTarget))
  }

  kotlinDslPluginOptions {
    jvmTarget.set(gradleJvmTarget)
  }
}
