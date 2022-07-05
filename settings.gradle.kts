rootProject.name = "warden"

include(
  ":core",
  ":ktor",
  ":docs",
)

project(":core").name = "warden-core"
project(":ktor").name = "warden-ktor"
