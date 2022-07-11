package buildsrc.convention

plugins {
    `maven-publish`
    id("com.jfrog.artifactory")
}

publishing {
    repositories {
        // publish to local dir, for testing
        maven(rootProject.layout.buildDirectory.dir("maven-internal")) {
            name = "maven-internal"
        }
    }
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
