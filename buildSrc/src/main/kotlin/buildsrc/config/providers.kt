package buildsrc.config

import org.gradle.api.Project

// TODO maybe convert envOrProperty to use Provider API?

fun Project.envOrProperty(name: String, required: Boolean = false): String? {
    val result = project.findProperty(name) as? String ?: System.getenv(name)
    return if (required) {
        requireNotNull(result) { "Missing required environment property:\n  export $name=\"...\"" }
    } else {
        result
    }
}
