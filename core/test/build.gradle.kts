plugins {
    id("ledger.kotlin.multiplatform")
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.test"
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.test)
            api(libs.junit)
            api(project(":core:domain"))
        }
        androidMain.dependencies {
            api(libs.robolectric)
            implementation(libs.compose.runtime)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}