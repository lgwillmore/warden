package buildsrc.convention

plugins {
    id("buildsrc.convention.ktlint")
}

if (project != rootProject) {
    group = rootProject.group
    version = rootProject.version
}

description = "Common settings for all Warden subprojects"
