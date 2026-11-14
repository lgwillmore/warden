plugins {
  id("buildsrc.convention.kotlin-jvm")
  id("buildsrc.convention.publish")
}

description =
    "Warden Ktor plugin: every route is enforced by an EnforcementPoint or explicitly left open"

dependencies {
  // Core and Ktor types appear in this module's public API.
  api(project(":warden-core"))
  api(libs.ktorServerCore)
  implementation(libs.kotlinxCoroutines)
  implementation(libs.slf4jApi)

  testImplementation(libs.bundles.ktorTesting)
}
