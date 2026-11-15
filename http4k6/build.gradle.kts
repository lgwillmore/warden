plugins {
  id("buildsrc.convention.kotlin-jvm")
  id("buildsrc.convention.publish")
}

description =
    "Warden http4k 6 filter: every handler is enforced by an EnforcementPoint or explicitly left open"

// http4k 6 is compiled for Java 21, so this module needs it too; the other modules stay on 17.
kotlin { jvmToolchain(21) }

dependencies {
  // Core and http4k types appear in this module's public API.
  api(project(":warden-core"))
  api(libs.http4kCore)
  implementation(libs.kotlinxCoroutines)
  implementation(libs.slf4jApi)
}
