package buildsrc.convention

plugins {
    id("buildsrc.convention.subproject")
    kotlin("multiplatform")
}

kotlin {
    targets.all {
        compilations.all {
            kotlinOptions {
                languageVersion = "1.9"
                apiVersion = "1.9"
            }
        }
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
}
