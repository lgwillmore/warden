plugins { id("buildsrc.convention.kotlin-jvm") }

description = "Code quoted by the guides, compiled and tested with the build; not published"

// The http4k sample needs the JDK that http4k 6 is compiled for.
kotlin { jvmToolchain(21) }

dependencies {
  implementation(project(":warden-core"))
  implementation(project(":warden-atts"))
  implementation(project(":warden-ktor3"))
  implementation(project(":warden-http4k6"))
  // The sample Ktor application maps NotAuthorizedException to a status and serves a websocket.
  implementation(libs.ktorServerStatusPages)
  implementation(libs.ktorServerWebsockets)

  testImplementation(libs.bundles.ktorTesting)
}
