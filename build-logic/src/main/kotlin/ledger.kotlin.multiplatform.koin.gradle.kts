import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.the

plugins {
    id("ledger.kotlin.multiplatform")
    id("io.insert-koin.compiler.plugin")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("koin-core").get())
            implementation(libs.findLibrary("koin-annotations").get())
        }
    }
}

// Koin compiler 1.1.0 dropped per-module validation: the full graph is verified only at the
// `@KoinApplication` entry points (androidApp / desktopApp / iosExport). Every library module
// therefore emits an informational "compile-safety validation skipped — no Koin entry point in
// this compilation" line, once per compilation (android / jvm / iosArm64 / iosSimulatorArm64 /
// metadata). That is disclosure, not a diagnostic, so it is downgraded to INFO here. Real
// KOIN-D***/KOIN-W*** diagnostics keep their own severity regardless of this setting, and the
// assembled graph is still checked at the entry points plus by the runtime `verify()` tests.
koinCompiler {
    logSeverity = "info"
    // Vendor CTA pointing at Kotzilla MCP; not a diagnostic.
    aiAssist = false
}