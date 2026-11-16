plugins {
  alias(libs.plugins.ktfmt)
  id("org.jetbrains.dokka")
}

group = "codes.laurence.warden"

// A commit carrying a release tag (e.g. `0.5.0`) builds that version; any other commit builds a `-SNAPSHOT`
// (`0.5.0-3-gabc1234-SNAPSHOT`, or `0.0.0-SNAPSHOT` without tags), which can never become a Maven Central release.
version =
    providers
        .exec {
          commandLine("git", "describe", "--tags", "--match", "[0-9]*.[0-9]*.[0-9]*")
          isIgnoreExitValue = true
        }
        .standardOutput
        .asText
        .get()
        .trim()
        .let { described ->
          when {
            described.matches(Regex("""\d+\.\d+\.\d+""")) -> described
            described.isEmpty() -> "0.0.0-SNAPSHOT"
            else -> "$described-SNAPSHOT"
          }
        }

subprojects { apply(plugin = "com.ncorti.ktfmt.gradle") }

// `dokkaGenerate` writes the API reference for every published module to build/dokka/html.
dokka { moduleName = "Warden" }

dependencies {
  dokka(project(":warden-core"))
  dokka(project(":warden-atts"))
  dokka(project(":warden-ktor3"))
  dokka(project(":warden-http4k6"))
  dokka(project(":warden-springboot4"))
  dokka(project(":warden-springboot4-webflux"))
}
