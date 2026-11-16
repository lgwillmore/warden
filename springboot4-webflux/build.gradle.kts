plugins {
  id("buildsrc.convention.kotlin-jvm")
  id("buildsrc.convention.publish")
}

description =
    "Warden Spring WebFlux filter for Spring Boot 4: every handler is enforced by an EnforcementPoint or explicitly left open"

dependencies {
  // The base module and WebFlux types appear in this module's public API.
  api(project(":warden-springboot4"))
  api(libs.springWebflux)
  // CoWebFilter and the Reactor context bridge are coroutine-to-Reactor glue.
  api(libs.kotlinxCoroutinesReactor)
  implementation(libs.kotlinxCoroutines)
  implementation(libs.springBootAutoconfigure)

  testImplementation(libs.bundles.springWebfluxTesting)
  // Spring needs it to recognise suspending handler methods.
  testImplementation(kotlin("reflect"))
}
