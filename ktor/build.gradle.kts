plugins {
    kotlin("jvm") version "1.3.61"
    id("com.jfrog.bintray") version "1.8.4"
    id("maven-publish")
}

val ktorVersion: String by project
val mockkVersion: String by project
val projectVersion: String by project

dependencies {

    implementation(project(":core"))

    api("io.ktor:ktor-server-core:$ktorVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        register("coreJVM", MavenPublication::class) {
            groupId = "codes.laurence.warden"
            artifactId = "warden-ktor"
            version = projectVersion
            artifact("build/libs/core-jvm-${project.version}.jar")
        }
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}