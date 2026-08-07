import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlinx.kover")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    compilerOptions {
        extraWarnings.set(true)
    }

    extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget>("android") {
        compileSdk = libs.findVersion("android-sdk-compile").get().requiredVersion.toInt()
        minSdk = libs.findVersion("android-sdk-min").get().requiredVersion.toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
        }
    }
}

// Shared Kover report excludes (keep in sync with the root aggregate excludes).
kover {
    reports {
        filters {
            excludes {
                classes(
                    "*ComposableSingletons*",
                    "*_Factory",
                    "*\$\$serializer",
                    // Compose `Res` accessors; each module pins its own package via
                    // `packageOfResClass` under `app.oreshkov.ledger.*.resources`.
                    "app.oreshkov.ledger.*.resources.*",
                    "*.compose.resources.*",
                    "*.di.*",
                )
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
            }
        }
    }
}