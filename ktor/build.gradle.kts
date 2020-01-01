plugins {
    kotlin("jvm") version "1.3.61"
}

val ktorVersion: String by project
val mockkVersion: String by project

dependencies {

    implementation(project(":core"))

    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation(kotlin("test"))
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