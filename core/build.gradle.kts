plugins {
    buildsrc.convention.`kotlin-multiplatform`
    buildsrc.convention.`sonatype-publish`
    id("com.google.devtools.ksp")
}

val assertKVersion: String by project
val mockkVersion: String by project
val mockativeVersion: String by project
val kotlinVersion: String by project

kotlin {
    targetHierarchy.default()

    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
                implementation("io.mockative:mockative:$mockativeVersion")
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)
        }

        val jvmTest by getting {
            // JVM-specific tests and their dependencies:
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val nativeMain by getting {
            dependsOn(commonMain)
        }

        val nativeTest by getting {
            dependsOn(commonTest)
        }
    }
}

dependencies {
    configurations
        .filter { it.name.startsWith("ksp") && it.name.contains("Test") }
        .forEach {
            add(it.name, "io.mockative:mockative-processor:$mockativeVersion")
        }
}

ktlint {
    filter {
        include { element ->
            element.file.extension in setOf("kt", "kts")
        }
        exclude { element ->
            element.file.path.contains("generated") || element.file.name == "build.gradle.kts"
        }
    }
}

