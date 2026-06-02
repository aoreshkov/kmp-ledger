plugins {
    id("ledger.kotlin.multiplatform.koin")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.common"
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
        }
        androidMain.dependencies {
            implementation(libs.slf4j.android)
        }
        jvmMain.dependencies {
            implementation(libs.slf4j.api)
            implementation(libs.logback.classic)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}