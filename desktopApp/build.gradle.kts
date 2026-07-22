import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.koin.compiler)
}

dependencies {
    implementation(project(":core:bootstrap"))
    implementation(project(":core:ui"))
    implementation(project(":feature:posting:impl"))
    implementation(project(":feature:settings:impl"))
    implementation(libs.compose.ui)
    implementation(compose.desktop.currentOs)
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(libs.kotlinx.coroutines.swing)
    testImplementation(project(":core:data"))
    testImplementation(project(":core:database"))
    testImplementation(libs.room3.runtime)
    testImplementation(libs.androidx.datastore)
    testImplementation(libs.androidx.datastore.preferences)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.koin.test)
    testImplementation(libs.compose.ui.test)
    testImplementation(libs.koin.compose)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

// Koin compiler plugin 1.0.2's aggregator (A3) validation is too strict for this
// project's multi-Gradle-module + expect/actual `@Module` graph: at the entry point it
// can't see definitions provided by sibling modules / platform `actual` `@Module`s (e.g.
// `RoomDatabase.Builder` from `PlatformDatabaseModule`) and reports false KOIN-D001
// "Missing dependency" (InsertKoinIO/koin-compiler-plugin#51 — maintainer confirms the
// A3 pass is over-strict, relaxed multi-module safety is slated for 1.1.0). Per-module
// A2 validation stays on for every library module; the full assembled graph is still
// verified at runtime by the `verify()` tests in core:bootstrap / feature *:impl. Revert
// to the default (compileSafety = true) once the plugin ships the 1.1.0 fix.
koinCompiler {
    compileSafety = false
}

// Pin the Java side too: without this, compileJava targets whatever JDK runs the
// Gradle daemon (IDEs may override gradle/gradle-daemon-jvm.properties with their
// own runtime), tripping KGP's JVM-target validation against jvmTarget 21.
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

compose.desktop {
    application {
        mainClass = "app.oreshkov.ledger.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ledger"
            packageVersion = providers.gradleProperty("ledger.version.name").get()

            linux {
                iconFile.set(project.file("appIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("appIcons/WindowsIcon.ico"))
            }
            macOS {
                iconFile.set(project.file("appIcons/MacosIcon.icns"))
                bundleID = "app.oreshkov.ledger.desktopApp"
            }
        }
    }
}