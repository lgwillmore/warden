package buildsrc.config

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

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

/**
 * Fetches credentials from `gradle.properties`, environment variables, or command line args.
 *
 * See https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:handling_credentials
 */
// https://github.com/gradle/gradle/issues/20925
fun ProviderFactory.credentialsAction(
    repositoryName: String
): Provider<Action<PasswordCredentials>> = zip(
    environmentVariable("${repositoryName}_USERNAME"),
    environmentVariable("${repositoryName}_PASSWORD"),
) { user, pass ->
    Action<PasswordCredentials> {
        username = user
        password = pass
    }
}

/**
 * Check if a Kotlin Mutliplatform project also has Java enabled.
 *
 * Logic from [KotlinJvmTarget.withJava]
 */
fun Project.isKotlinMultiplatformJavaEnabled(): Boolean {
    val multiplatformExtension: KotlinMultiplatformExtension? =
        extensions.findByType(KotlinMultiplatformExtension::class)

    return multiplatformExtension?.targets
        ?.any { it is KotlinJvmTarget && it.withJavaEnabled }
        ?: false
}
