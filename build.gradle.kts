plugins {
//    kotlin("jvm")
//    id("org.jlleitschuh.gradle.ktlint") version "10.1.0" // TODO re-enable ktlint
    id("com.palantir.git-version") version "0.15.0"
}
//
val gitVersion: groovy.lang.Closure<String> by extra
//
group = "codes.laurence.warden"
version = gitVersion().replace(".dirty", "")
//
//subprojects {
// TODO move ktlint settings to buildSrc plugin
//    apply(plugin = "org.jlleitschuh.gradle.ktlint")
//
//    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
//        disabledRules.set(
//            setOf("no-wildcard-imports", "filename")
//        )
//    }
//}
//
//buildscript {
//    repositories {
//        mavenCentral()
////        jcenter()
//    }
//    dependencies {
//        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4.21.0")
//    }
//}

tasks.wrapper {
    gradleVersion = "7.4.2"
    distributionType = Wrapper.DistributionType.ALL
}
