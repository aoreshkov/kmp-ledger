import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    id("ledger.kotlin.multiplatform.koin")
}

// Pure logic module: hold line and branch high.
kover {
    reports {
        verify {
            rule("Data logic coverage") {
                minBound(90, CoverageUnit.LINE)
                minBound(85, CoverageUnit.BRANCH)
            }
        }
    }
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.data"
        androidResources {
            enable = true
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":core:model"))
            implementation(project(":core:domain"))
            implementation(project(":core:common"))
            implementation(project(":core:database"))
        }
        commonTest.dependencies {
            implementation(libs.bundles.common.test)
            implementation(project(":core:test"))
        }
    }
}