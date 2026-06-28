plugins {
    id("ledger.kotlin.multiplatform.koin.compose")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.bootstrap"
    }

    swiftExport {
        moduleName = "Ledger"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:navigation"))
            implementation(project(":core:ui"))
            implementation(project(":feature:posting:api"))
            implementation(project(":feature:posting:impl"))
            implementation(project(":feature:settings:api"))
            implementation(project(":feature:settings:impl"))
        }
        commonTest.dependencies {
            implementation(libs.kermit)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
        getByName("androidHostTest") {
            kotlin.srcDir("src/jvmTest/kotlin")
            dependencies {
                implementation(libs.koin.test)
            }
        }
    }
}