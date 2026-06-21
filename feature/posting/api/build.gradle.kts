import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    id("ledger.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

// NavKey serialization contract: hold line and branch high.
kover {
    reports {
        verify {
            rule("NavKey contract coverage") {
                minBound(90, CoverageUnit.LINE)
                minBound(85, CoverageUnit.BRANCH)
            }
        }
    }
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.feature.posting.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.navigation3.runtime)
            implementation(libs.kotlinx.serialization.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}