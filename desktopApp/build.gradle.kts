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

// Koin compiler: full-graph validation doesn't honour the `providerOnly` flag the plugin itself
// sets on a DSL `single<T> { … }` whose lambda builds T (KoinDSLTransformer sets it and
// CallSiteValidator filters on it, but CompileSafetyValidator does not). It therefore walks T's
// *constructor* and reports those parameters missing. Here that is test-only: DesktopUiTest's
// `single<RoomDatabase.Builder<LedgerDatabase>> { … }` override yields false KOIN-D001 for
// `klass` and `factory`. Those tests pass, so the graph is fine.
//
// This — not the multi-module false positive fixed in 1.1.0 — was always the real cause; the
// earlier diagnosis blamed A3 for not seeing `PlatformDatabaseModule`'s platform `actual`, but
// main source (`compileKotlin`, where that graph lives) compiles clean and the error names the
// test's own `inMemoryDatabaseModule`.
//
// `compileSafety` is per-Gradle-project, so silencing the test compilation silences main too.
// Acceptable: androidApp is an entry point with compile safety on and validates the identical
// `BootstrapModule` closure, leaving only the JVM platform `actual`s uncovered at compile time,
// and those are checked at runtime by core:bootstrap's KoinModuleVerificationTest. Remove once
// the plugin honours `providerOnly` in the full-graph pass.
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