plugins {
    id("ledger.kotlin.multiplatform.koin")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.bootstrap"
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:navigation"))
            implementation(project(":core:ui"))
            implementation(project(":feature:posting:api"))
            implementation(project(":feature:posting:impl"))
        }
        commonTest.dependencies {
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
        val androidHostTest by getting {
            kotlin.srcDir("src/jvmTest/kotlin")
            dependencies {
                implementation(libs.koin.test)
            }
        }
    }
}