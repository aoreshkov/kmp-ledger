plugins {
    id("ledger.kotlin.multiplatform.koin.compose")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.navigation"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.navigation3.runtime)
            // ImageVector type carried by TopLevelDestination.
            implementation(libs.compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// Pin the generated `Res` package instead of taking the `{group}.{module}.generated.resources`
// default, which derives from `rootProject.name` and so would silently repackage every module's
// accessors if the root project were renamed. Stays internal (`publicResClass` defaults to false).
compose.resources {
    packageOfResClass = "app.oreshkov.ledger.core.navigation.resources"
}
