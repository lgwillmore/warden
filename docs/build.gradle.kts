plugins {
    id("com.eden.orchidPlugin") version "0.18.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
}

val projectVersion: String by project
val orchidVersion: String by project

dependencies {
    orchidImplementation("io.github.javaeden.orchid:OrchidCore:$orchidVersion")
    orchidImplementation("io.github.javaeden.orchid:OrchidPages:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidSearch:$orchidVersion")

    // Themes
    orchidImplementation("io.github.javaeden.orchid:OrchidBsDoc:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidEditorial:$orchidVersion")

    orchidImplementation("io.github.javaeden.orchid:OrchidSyntaxHighlighter:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidGithub:$orchidVersion")

    // Source Docs
    orchidRuntime("io.github.javaeden.orchid:OrchidDocs:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidKotlindoc:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidPluginDocs:$orchidVersion")
}

fun envOrProperty(name: String, required: Boolean = false): String? {
    val result = project.findProperty(name) as? String ?: System.getenv(name)
    check(result != null || required.not()) { "Missing required environment property:\n  export $name=\"...\"" }
    return result
}

orchid {
    val isProd = envOrProperty("env") == "prod"
    environment = if (isProd) "production" else "debug"
    // Theme is required
    theme = "Editorial"

    // The following properties are optional
    version = projectVersion
    args = listOf("--experimentalSourceDoc")
    baseUrl = when {
        isProd && envOrProperty("PULL_REQUEST") == "true" -> envOrProperty("DEPLOY_URL", required = true)
        isProd -> envOrProperty("URL", required = true)
        else -> "http://localhost:8080"
    }
}
