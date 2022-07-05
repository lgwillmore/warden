package buildsrc.config

import org.gradle.api.publish.maven.MavenPublication

fun MavenPublication.createWardenPom(
    description: String = "https://github.com/lgwillmore/warden",
): Unit = pom {
    // Note: 'group' and 'name' are set from the Gradle Project group and name

    description.set(description)
    url.set("https://warden-kotlin.netlify.com/")

    licenses {
        license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/mit-license")
            distribution.set("repo")
        }
    }

    developers {
        developer {
            id.set("lgwillmore")
            name.set("Laurence Willmore")
        }
    }

    scm {
        connection.set("scm:git:git://github.com/lgwillmore/warden.git")
        developerConnection.set("scm:git:ssh://github.com/lgwillmore/warden.git")
        url.set("https://github.com/lgwillmore/warden")
    }

    issueManagement {
        url.set("https://github.com/lgwillmore/warden/issues")
    }
}
