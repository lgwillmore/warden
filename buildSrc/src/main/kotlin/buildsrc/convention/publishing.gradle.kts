package buildsrc.convention

import buildsrc.config.publish
import buildsrc.config.repository

plugins {
    `maven-publish`
    id("com.jfrog.artifactory")
}

artifactory {
    setContextUrl("https://laurencecodes.jfrog.io/artifactory")
    publish {
        repository {
            setProperty("repoKey", "codes.laurence.warden")
            setProperty("username", System.getenv("JFROG_USER"))
            setProperty("password", System.getenv("JFROG_PASSWORD"))
            setProperty("maven", true)
        }
    }
}
