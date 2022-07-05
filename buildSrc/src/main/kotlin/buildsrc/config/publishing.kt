package buildsrc.config

import org.gradle.api.publish.maven.MavenPublication

fun MavenPublication.createWardenPom(
    pomDescription: String = "https://github.com/lgwillmore/warden",
): Unit = pom {
    // Note: Gradle will automatically set the POM 'group' and 'name' from the subproject group and name

    description.set(pomDescription)
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
