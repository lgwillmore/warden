plugins {
//    id("org.jlleitschuh.gradle.ktlint") version "10.1.0" // TODO re-enable ktlint
    id("com.palantir.git-version") version "0.15.0"
}

val gitVersion: groovy.lang.Closure<String> by extra

group = "codes.laurence.warden"
version = gitVersion().replace(".dirty", "")

tasks.wrapper {
    gradleVersion = "7.4.2"
    distributionType = Wrapper.DistributionType.ALL
}
