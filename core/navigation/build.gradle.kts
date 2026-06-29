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