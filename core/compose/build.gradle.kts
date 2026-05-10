plugins {
    id("kmpledger.kotlin.multiplatform")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose.compiler)
}

kotlin {
    android {
        namespace = "app.oreshkov.kmpledger.core.compose"
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            api(libs.compose.components.resources)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.compose.ui.test)
            implementation(project(":core:test"))
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "app.oreshkov.kmpledger.core.compose.resources"
    generateResClass = auto
}