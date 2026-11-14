plugins {
  id("buildsrc.convention.kotlin-jvm")
  id("buildsrc.convention.publish")
}

description = "Warden helpers for turning domain objects into attribute maps by reflection"

dependencies { implementation(kotlin("reflect")) }
