plugins {
    id("ledger.kotlin.multiplatform")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.common"
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}