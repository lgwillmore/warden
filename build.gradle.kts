plugins {
    id("com.palantir.git-version") version "0.12.3"
    id("com.google.devtools.ksp") apply false
    buildsrc.convention.ktlint
}

val gitVersion: groovy.lang.Closure<String> by extra

group = "codes.laurence.warden"
version = gitVersion().replace(".dirty", "")

ktlint {
    kotlinScriptAdditionalPaths {
        include(fileTree("buildSrc"))
    }
}

tasks.wrapper {
    gradleVersion = "8.5"
    distributionType = Wrapper.DistributionType.ALL
}
