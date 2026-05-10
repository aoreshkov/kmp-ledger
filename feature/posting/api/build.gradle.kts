plugins {
    id("kmpledger.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "app.oreshkov.kmpledger.feature.posting.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.navigation3.runtime)
        }
    }
}