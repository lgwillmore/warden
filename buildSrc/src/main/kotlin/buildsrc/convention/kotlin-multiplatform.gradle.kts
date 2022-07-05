package buildsrc.convention

plugins {
    id("buildsrc.convention.subproject")
    kotlin("multiplatform")
}

kotlin {
    targets.all {
        compilations.all {
            kotlinOptions {
                languageVersion = "1.6"
                apiVersion = "1.6"
            }
        }
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
}
