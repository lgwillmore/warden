package buildsrc.config

import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugins.signing.SigningExtension

object PublishingAttributes {

    val artifactName = "warden-ktor"

    val pomUrl = "https://warden-kotlin.netlify.com/"
    val pomScmUrl = "https://github.com/lgwillmore/warden"
    val pomIssueUrl = "https://github.com/lgwillmore/warden/issues"
    val pomDesc = "https://github.com/lgwillmore/warden"

    val githubRepo = "lgwillmore/warden"
    val githubReadme = "README.md"

    val pomLicenseName = "MIT"
    val pomLicenseUrl = "https://opensource.org/licenses/mit-license.php"
    val pomLicenseDist = "repo"

    val pomDeveloperId = "lgwillmore"
    val pomDeveloperName = "Laurence Willmore"
}

fun MavenPublication.createWardenPom(
    artifactName : String,
): Unit = pom {
    name.set(artifactName)
    description.set("https://github.com/lgwillmore/warden")
    url.set(PublishingAttributes.pomUrl)

    licenses {
        license {
            name.set(PublishingAttributes.pomLicenseName)
            url.set(PublishingAttributes.pomLicenseUrl)
        }
    }

    developers {
        developer {
            id.set(PublishingAttributes.pomDeveloperId)
            name.set(PublishingAttributes.pomDeveloperName)
        }
    }

    scm {
        connection.set("scm:git:git://github.com/lgwillmore/warden.git")
        developerConnection.set("scm:git:ssh://github.com/lgwillmore/warden.git")
        url.set("https://github.com/lgwillmore/warden")
    }
//    groupId = artifactGroup
//    artifactId = artifactName
//    version = version
//    artifact("$buildDir/libs/warden-ktor-${project.version}-sources.jar") {
//        classifier = "sources"
//    }
//
//    artifact("$buildDir/libs/warden-ktor-${project.version}.jar")
//
//    pom.withXml {
//        asNode().apply {
//            appendNode("description", pomDesc)
//            appendNode("name", rootProject.name)
//            appendNode("url", pomUrl)
//            appendNode("licenses").appendNode("license").apply {
//                appendNode("name", pomLicenseName)
//                appendNode("url", pomLicenseUrl)
//                appendNode("distribution", pomLicenseDist)
//            }
//            appendNode("developers").appendNode("developer").apply {
//                appendNode("id", pomDeveloperId)
//                appendNode("name", pomDeveloperName)
//            }
//            appendNode("scm").apply {
//                appendNode("url", pomScmUrl)
//            }
//        }
//    }
}
