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
                    // Room KSP output (`*_Impl` DAO/database implementations and the
                    // generated `LedgerDatabaseConstructor` actual).
                    "*_Impl*",
                    "*DatabaseConstructor",
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

// Robolectric 4.17 added SDK 37 shadows, and API 37's `ApplicationSharedMemory`
// drives Robolectric's `FileDescriptorInterceptor` into `jdk.internal.access`.
// On JDK 17+ that is module-encapsulated, so the interceptor throws
// `IllegalAccessException` during `setUpApplicationState` and every Robolectric
// test dies before it runs. These are the flags robolectric.org/getting-started
// prescribes for JDK 17+; scoped to the Android host-test tasks, the only ones
// that load Robolectric.
tasks.withType<Test>().matching { it.name.contains("AndroidHostTest") }.configureEach {
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.net=ALL-UNNAMED",
        "--add-opens=java.base/java.security=ALL-UNNAMED",
        "--add-opens=java.base/java.text=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    )
}
