plugins {
    id("ledger.kotlin.multiplatform.koin.compose")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger"
    }

    iosArm64 {
        binaries.framework {
            baseName = "Ledger"
            isStatic = true
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Ledger"
            isStatic = true
        }
    }

    swiftExport {
        moduleName = "Ledger"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:bootstrap"))
            implementation(project(":core:ui"))
            implementation(project(":feature:posting:impl"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}