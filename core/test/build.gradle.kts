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

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.test)
            api(project(":core:domain"))
            api(project(":core:common"))
            implementation(libs.compose.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            api(libs.robolectric)
            api(libs.junit)
        }
        jvmMain.dependencies {
            api(libs.junit)
            implementation(compose.desktop.currentOs)
        }
        iosMain.dependencies {
        }
    }
}