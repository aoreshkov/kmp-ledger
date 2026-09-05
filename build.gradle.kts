import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library.multiplatform) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.compose.hot.reload) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

dependencies {
    kover(project(":core:bootstrap"))
    kover(project(":core:common"))
    kover(project(":core:compose"))
    kover(project(":core:data"))
    kover(project(":core:database"))
    kover(project(":core:datastore"))
    kover(project(":core:domain"))
    kover(project(":core:model"))
    kover(project(":core:navigation"))
    kover(project(":core:ui"))
    kover(project(":feature:posting:api"))
    kover(project(":feature:posting:impl"))
    kover(project(":feature:settings:api"))
    kover(project(":feature:settings:impl"))
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*ComposableSingletons*",
                    "*_Factory",
                    "*\$\$serializer",
                    // Compose `Res` accessors. Each module pins its own package via
                    // `packageOfResClass`, so this tracks `app.oreshkov.ledger.*.resources`
                    // rather than the `*.generated.resources` default.
                    "app.oreshkov.ledger.*.resources.*",
                    "*.compose.resources.*",
                    // DI wiring lives in *.di packages; validated by Koin verify(),
                    // not by execution — exclude so coverage reflects real logic.
                    "*.di.*"
                )
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
            }
        }

        // Aggregate floors block regressions; branch stays modest (Compose synthetic
        // branches). Per-module logic floors live in those modules' build files.
        verify {
            rule("Aggregate line coverage") {
                minBound(88, CoverageUnit.LINE)
            }
            rule("Aggregate branch coverage") {
                minBound(60, CoverageUnit.BRANCH)
            }
            rule("Aggregate instruction coverage") {
                minBound(84, CoverageUnit.INSTRUCTION)
            }
        }
    }
}

apiValidation {
    // Koin compiler emits public `org.koin.plugin.hints.*` synthetics purely for
    // cross-module graph validation; they are compiler-owned and re-shape on every
    // Koin/Kotlin compiler bump, so they are not stable public API worth tracking.
    ignoredPackages.add("org.koin.plugin.hints")

    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
        strictValidation = true
    }
}