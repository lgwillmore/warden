package buildsrc.convention

import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    disabledRules.set(
        setOf("no-wildcard-imports", "filename")
    )
}
