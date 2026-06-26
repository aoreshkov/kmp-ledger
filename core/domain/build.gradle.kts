import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    id("ledger.kotlin.multiplatform.koin")
}

// Pure logic module: hold line and branch high.
kover {
    reports {
        verify {
            rule("Domain logic coverage") {
                minBound(90, CoverageUnit.LINE)
                minBound(85, CoverageUnit.BRANCH)
            }
        }
    }
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.domain"
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":core:common"))
            api(project(":core:model"))
        }
        commonTest.dependencies {
            implementation(libs.bundles.common.test)
            implementation(project(":core:test"))
        }
    }
}