plugins {
    id("ledger.kotlin.multiplatform.koin")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.domain"
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            api(project(":core:model"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":core:test"))
        }
    }
}