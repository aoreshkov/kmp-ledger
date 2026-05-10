plugins {
    id("kmpledger.kotlin.multiplatform.koin.compose")
}

kotlin {
    android {
        namespace = "app.oreshkov.kmpledger.core.navigation"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.navigation3.runtime)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}