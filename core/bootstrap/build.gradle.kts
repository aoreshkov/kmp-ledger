plugins {
    id("ledger.kotlin.multiplatform.koin.compose")
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.bootstrap"
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

// Pin the generated `Res` package instead of taking the `{group}.{module}.generated.resources`
// default, which derives from `rootProject.name` and so would silently repackage every module's
// accessors if the root project were renamed. Stays internal (`publicResClass` defaults to false).
compose.resources {
    packageOfResClass = "app.oreshkov.ledger.core.bootstrap.resources"
}
