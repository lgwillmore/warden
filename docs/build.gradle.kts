plugins {
    id("com.eden.orchidPlugin") version "0.18.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io")
}

val orchidVersion: String by project

dependencies {
    compile("io.github.javaeden.orchid:OrchidCore:$orchidVersion")
    orchidCompile("io.github.javaeden.orchid:OrchidCore:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidPosts:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidPages:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidWiki:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidNetlifyCMS:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidPluginDocs:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidSearch:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidWritersBlocks:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidSyntaxHighlighter:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidTaxonomies:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidAsciidoc:$orchidVersion")
    orchidRuntime("io.github.javaeden.orchid:OrchidEditorial:$orchidVersion")
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
    version = "${project.version}"
    baseUrl = when {
        isProd && envOrProperty("PULL_REQUEST") == "true" -> envOrProperty("DEPLOY_URL", required = true)
        isProd -> envOrProperty("URL", required = true)
        else -> "http://localhost:8080"
    }
}