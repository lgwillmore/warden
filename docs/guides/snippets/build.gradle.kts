plugins { id("buildsrc.convention.kotlin-jvm") }

description = "Code quoted by the guides, compiled and tested with the build; not published"

// The http4k sample needs the JDK that http4k 6 is compiled for.
kotlin { jvmToolchain(21) }

dependencies {
  implementation(project(":warden-core"))
  implementation(project(":warden-atts"))
  implementation(project(":warden-ktor3"))
  implementation(project(":warden-http4k6"))
  implementation(project(":warden-springboot4-webflux"))
  // The sample Ktor application maps NotAuthorizedException to a status and serves a websocket.
  implementation(libs.ktorServerStatusPages)
  implementation(libs.ktorServerWebsockets)
  // The sample Spring Boot application is a WebFlux application with suspending handlers.
  implementation(libs.springBootStarterWebflux)
  implementation(kotlin("reflect"))

  testImplementation(libs.bundles.ktorTesting)
  testImplementation(libs.springBootTest)
  testImplementation(libs.springTest)
  // Spring's JUnit extension needs Jupiter 6; kotlin-test alone would bring 5.
  testImplementation(libs.junitJupiter)
}
