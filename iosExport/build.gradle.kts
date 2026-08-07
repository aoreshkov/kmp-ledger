import org.jetbrains.kotlin.gradle.swiftexport.ExperimentalSwiftExportDsl

plugins {
    id("ledger.kotlin.multiplatform.koin.compose")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger"
    }

    iosArm64 {
        binaries.framework {
            baseName = "LedgerBinary"
            isStatic = true
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = "LedgerBinary"
            isStatic = true
        }
    }

    @OptIn(ExperimentalSwiftExportDsl::class)
    swiftExport {
        moduleName = "Ledger"
        flattenPackage = "app.oreshkov.ledger"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:bootstrap"))
            implementation(project(":core:ui"))
            implementation(project(":feature:posting:impl"))
            implementation(project(":feature:settings:impl"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

// Pin the generated `Res` package instead of taking the `{group}.{module}.generated.resources`
// default, which derives from `rootProject.name` and so would silently repackage every module's
// accessors if the root project were renamed. Stays internal (`publicResClass` defaults to false).
compose.resources {
    packageOfResClass = "app.oreshkov.ledger.iosexport.resources"
}
