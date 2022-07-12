import buildsrc.config.envOrProperty

plugins {
    id("com.eden.orchidPlugin")
}

val orchidVersion: String by project

dependencies {
    orchidImplementation("io.github.javaeden.orchid:OrchidCore:$orchidVersion")
    orchidImplementation("io.github.javaeden.orchid:OrchidPages:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidSearch:$orchidVersion")

    // Themes
    orchidImplementation("io.github.javaeden.orchid:OrchidBsDoc:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidEditorial:$orchidVersion")

    orchidImplementation("io.github.javaeden.orchid:OrchidSyntaxHighlighter:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidGithub:$orchidVersion")

    // Source Docs
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidDocs:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidKotlindoc:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidPluginDocs:$orchidVersion")
}

orchid {
    val isProd = envOrProperty("env") == "prod"
    environment = if (isProd) "production" else "debug"
    // Theme is required
    theme = "Editorial"

    // The following properties are optional
    port = 8082
    version = version
    args = listOf("--experimentalSourceDoc")
    baseUrl = when {
        isProd && envOrProperty("PULL_REQUEST") == "true" ->
            envOrProperty("DEPLOY_URL", required = true)
        isProd                                            -> envOrProperty("URL", required = true)
        else                                              -> "http://localhost:8082"
    }
}
