plugins {
    id("ledger.kotlin.multiplatform")
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.test"
        androidResources {
            enable = true
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlin.test)
            api(project(":core:domain"))
            api(project(":core:common"))
            implementation(libs.compose.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            api(libs.robolectric)
            api(libs.junit)
        }
        jvmMain.dependencies {
            api(libs.junit)
            implementation(compose.desktop.currentOs)
        }
        iosMain.dependencies {
        }
    }
}

dependencies {
    constraints {
        // robolectric 4.16.1 pins bcprov 1.81 (GHSA-574f-3g2m-x479, fixed in 1.84).
        // api scope so the constraint propagates to every consumer's unit-test classpath.
        "androidMainApi"(libs.bouncycastle.bcprov)
    }
}