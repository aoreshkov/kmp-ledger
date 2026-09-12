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
        // Compose `ui-test-junit4` still declares espresso-core 3.5.0, whose
        // InputManagerEventInjectionStrategy eagerly reflects the hidden
        // InputManager.getInstance() at graph-construction time — gone on API 37,
        // so `Espresso.onIdle` kills every Robolectric Compose test. 3.7.0 reflects
        // only below API 23 and uses Context.getSystemService above it. Latent until
        // Robolectric 4.17 let these tests actually run on SDK 37.
        // api scope so the constraint propagates to every consumer's unit-test classpath.
        "androidMainApi"(libs.androidx.test.espresso.core)
    }
}
