plugins { id("buildsrc.convention.kotlin-jvm") }

description = "Code quoted by the guides, compiled and tested with the build; not published"

dependencies {
  implementation(project(":warden-core"))
  implementation(project(":warden-atts"))
  implementation(project(":warden-ktor"))
  // The sample Ktor application maps NotAuthorizedException to a status and serves a websocket.
  implementation(libs.ktorServerStatusPages)
  implementation(libs.ktorServerWebsockets)

  testImplementation(libs.bundles.ktorTesting)
}
