plugins {
    id("kmpledger.kotlin.multiplatform.koin")
}

kotlin {
    android {
        namespace = "app.oreshkov.kmpledger.core.data"
        androidResources {
            enable = true
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(project(":core:model"))
            implementation(project(":core:database"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}