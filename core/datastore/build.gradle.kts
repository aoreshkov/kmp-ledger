import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    id("ledger.kotlin.multiplatform.koin")
}

// Preferences read/write logic carries the coverage; platform path providers and DI
// wiring (the *.di.* packages) are excluded from the aggregate report.
kover {
    reports {
        verify {
            rule("DataStore logic coverage") {
                minBound(90, CoverageUnit.LINE)
            }
        }
    }
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.datastore"
        withHostTest {}
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":core:common"))
            implementation(project(":core:domain"))
            implementation(project(":core:model"))
        }
        commonTest.dependencies {
            implementation(libs.bundles.common.test)
            implementation(project(":core:test"))
        }
        jvmTest.dependencies {
            implementation(libs.bundles.common.test)
        }
    }
}
