

plugins {
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`sonatype-publish`
}

val mockkVersion: String by project
val assertKVersion: String by project

dependencies {
    implementation(kotlin("reflect"))

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testImplementation(kotlin("test"))
}
