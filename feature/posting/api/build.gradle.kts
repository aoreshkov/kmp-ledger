plugins {
    id("ledger.kotlin.multiplatform")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.feature.posting.api"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.navigation3.runtime)
        }
    }
}