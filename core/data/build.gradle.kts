plugins {
    id("ledger.kotlin.multiplatform.koin")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.data"
        androidResources {
            enable = true
        }
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(project(":core:model"))
            api(project(":core:domain"))
            implementation(project(":core:common"))
            implementation(project(":core:database"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}