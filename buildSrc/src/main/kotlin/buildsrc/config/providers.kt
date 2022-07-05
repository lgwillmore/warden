package buildsrc.config

import org.gradle.api.Project

/**
 * First try to retrieve a property from the [Project], or if it's unavailable, try fetching it
 * from an environment variable.
 *
 * If it is [required], then throw an exception if it's unavailable. Else, return `null`.
 */
fun Project.envOrProperty(name: String, required: Boolean = false): String? {
    val result = project.findProperty(name) as? String ?: System.getenv(name)
    return if (required) {
        requireNotNull(result) { "Missing required environment property:\n  export $name=\"...\"" }
    } else {
        result
    }
}
