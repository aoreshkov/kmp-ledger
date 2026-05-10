plugins {
    id("kmpledger.kotlin.multiplatform.koin")
}

kotlin {
    android {
        namespace = "app.oreshkov.kmpledger.core.domain"
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(project(":core:model"))
            implementation(project(":core:data"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":core:test"))
        }
    }
}