plugins {
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`sonatype-publish`
}

val ktorVersion: String by project
val mockkVersion: String by project
val assertKVersion: String by project
val slf4jVersion: String by project

dependencies {
    implementation(projects.wardenCore)

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("io.ktor:ktor-websockets:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testImplementation(kotlin("test"))
}
