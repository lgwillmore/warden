plugins {
    kotlin("multiplatform") version "1.3.61"
}

repositories {
    mavenCentral()
}

val mockkVersion: String by project

kotlin {
    jvm()
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
                implementation("com.willowtreeapps.assertk:assertk-jvm:0.20")
            }
        }

    }
}

