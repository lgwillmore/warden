plugins {
    kotlin("jvm") version "1.3.61"
    id("com.jfrog.bintray") version "1.8.0"
    id("maven-publish")
}

buildscript {
    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.0")
    }
}

val ktorVersion: String by project
val mockkVersion: String by project
val projectVersion: String by project
version = projectVersion

dependencies {

    implementation(project(":core"))

    api("io.ktor:ktor-server-core:$ktorVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        register("ktorJar", MavenPublication::class) {
            groupId = "codes.laurence.warden"
            artifactId = "warden-ktor"
            version = projectVersion
            artifact("$buildDir/libs/ktor-${project.version}.jar")
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")
    pkg.apply {
        repo = "codes.laurence.warden"
        name = "warden-ktor"
        websiteUrl = "https://warden-kotlin.netlify.com/"
        vcsUrl = "https://github.com/lgwillmore/warden"
        issueTrackerUrl = "https://github.com/lgwillmore/warden/issues"
        setLabels("Kotlin", "ABAC", "Authorization")
        setLicenses("MIT")
        setPublications("ktorJar")
        version.apply {
            name = project.version.toString()
            desc = "SNAPSHOT release"
//            val timeZone = org.gradle.internal.impldep.org.joda.time.DateTimeZone.UTC
//            released  = org.gradle.internal.impldep.org.joda.time.DateTime(timeZone).toString()
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

    val build by existing

    bintrayUpload {
        dependsOn(build)
    }
}