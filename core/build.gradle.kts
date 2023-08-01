plugins {
    buildsrc.convention.`kotlin-multiplatform`
    buildsrc.convention.`sonatype-publish`
}

val assertKVersion: String by project
val mockkVersion: String by project
val kotlinVersion: String by project

kotlin {

    jvm()

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


