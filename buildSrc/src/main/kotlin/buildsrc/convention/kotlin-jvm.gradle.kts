package buildsrc.convention

plugins {
    id("buildsrc.convention.subproject")
    kotlin("jvm")
}

java {
    withSourcesJar()
    withJavadocJar()
}
