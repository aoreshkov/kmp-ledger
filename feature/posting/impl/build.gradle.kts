plugins {
    id("ledger.kotlin.multiplatform.koin.compose")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.feature.posting.impl"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.compose.navigation)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.adaptive.nav3)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":core:common"))
            implementation(project(":core:compose"))
            implementation(project(":core:data"))
            implementation(project(":core:domain"))
            implementation(project(":core:navigation"))
            implementation(project(":feature:posting:api"))
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}