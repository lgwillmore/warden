package buildsrc.convention

plugins {
    base
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    disabledRules.set(
        setOf("no-wildcard-imports", "filename")
    )
}

tasks.check.configure {
    dependsOn(tasks.ktlintCheck)
}
