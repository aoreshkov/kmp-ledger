plugins {
    id("ledger.kotlin.multiplatform.koin.compose")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.ui"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.material3.adaptive.nav3)
            implementation(libs.compose.material3.adaptive.navigation.suite)

            implementation(libs.koin.compose.navigation)

            api(project(":core:navigation"))
            api(project(":core:common"))
            api(project(":core:domain"))
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

// Pin the generated `Res` package instead of taking the `{group}.{module}.generated.resources`
// default, which derives from `rootProject.name` and so would silently repackage every module's
// accessors if the root project were renamed. Stays internal (`publicResClass` defaults to false).
compose.resources {
    packageOfResClass = "app.oreshkov.ledger.core.ui.resources"
}
