import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library.multiplatform) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.jetbrains.compose) apply false
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
    kover(project(":core:domain"))
    kover(project(":core:model"))
    kover(project(":core:navigation"))
    kover(project(":core:ui"))
    kover(project(":feature:posting:api"))
    kover(project(":feature:posting:impl"))
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*ComposableSingletons*",
                    "*_Factory",
                    "*\$\$serializer",
                    "*.generated.resources.*",
                    "*.compose.resources.*",
                    "*_HiltModules*",
                    // DI wiring lives in *.di packages; validated by Koin verify(),
                    // not by execution — exclude so coverage reflects real logic.
                    "*.di.*"
                )
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
            }
        }

        // Coverage floors, set just below the current baseline (line 89%, branch 59%,
        // instruction 85%) to block regressions. Ratchet up as P2 adds branch/edge tests
        // (target: branch >= 70%). Run via `./gradlew koverVerify`.
        verify {
            rule("Minimum line coverage") {
                minBound(85, CoverageUnit.LINE)
            }
            rule("Minimum branch coverage") {
                minBound(55, CoverageUnit.BRANCH)
            }
            rule("Minimum instruction coverage") {
                minBound(80, CoverageUnit.INSTRUCTION)
            }
        }
    }
}

apiValidation {
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
        strictValidation = true
    }
}