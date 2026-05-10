plugins {
    id("kmpledger.kotlin.multiplatform.koin")
}

kotlin {
    android {
        namespace = "app.oreshkov.kmpledger.core.bootstrap"
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:navigation"))
            implementation(project(":core:ui"))
            implementation(project(":feature:posting:api"))
            implementation(project(":feature:posting:impl"))
        }
    }
}