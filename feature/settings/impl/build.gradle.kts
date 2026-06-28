import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    id("ledger.kotlin.multiplatform.koin.compose")
}

// Hold a line floor on par with the other logic modules; the branch floor is tuned
// lower because Compose codegen in the @Composable screen emits synthetic branches.
kover {
    reports {
        verify {
            rule("Feature settings impl coverage") {
                minBound(90, CoverageUnit.LINE)
                minBound(60, CoverageUnit.BRANCH)
            }
        }
    }
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.feature.settings.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.compose.navigation)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.material3)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":core:common"))
            implementation(project(":core:compose"))
            implementation(project(":core:datastore"))
            implementation(project(":core:domain"))
            implementation(project(":core:model"))
            implementation(project(":core:navigation"))
            implementation(project(":feature:settings:api"))
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
