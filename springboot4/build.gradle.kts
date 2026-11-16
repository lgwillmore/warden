plugins {
  id("buildsrc.convention.kotlin-jvm")
  id("buildsrc.convention.publish")
}

description =
    "Warden for Spring: route configuration, the request marker and an EnforcementPoint shared by the Spring Boot 4 web adapters"

dependencies {
  // Core and spring-web types appear in this module's public API.
  api(project(":warden-core"))
  api(libs.springWeb)
  implementation(libs.kotlinxCoroutines)
  implementation(libs.slf4jApi)
  // Only used when the application runs on Reactor, so the class is looked up before it is touched.
  compileOnly(libs.kotlinxCoroutinesReactor)

  testImplementation(libs.kotlinxCoroutinesReactor)
}
