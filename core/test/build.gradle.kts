plugins {
    id("kmpledger.kotlin.multiplatform")
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    android {
        namespace = "app.oreshkov.kmpledger.core.test"
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.test)
            api(libs.junit)
            api(project(":core:model"))
            api(project(":core:data"))
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