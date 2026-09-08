import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("ledger.kotlin.multiplatform.koin")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<VersionCatalogsExtension>().named("libs")

val composeCompilerReports =
    providers.gradleProperty("ledger.composeCompilerReports").orNull.toBoolean()

composeCompiler {
    // core:model carries no Compose compiler plugin (see the layering rule in CLAUDE.md), so its
    // classes reach consumers without stability metadata and get inferred unstable. The conf file
    // declares them stable here instead, which keeps the module Compose-free and lets strong
    // skipping compare them by value. `stabilityConfigurationFiles` is the plural form; the
    // singular `stabilityConfigurationFile` is a DeprecationLevel.ERROR alias as of Kotlin 2.4.0.
    stabilityConfigurationFiles.add(layout.settingsDirectory.file("compose_stability.conf"))

    // Emit Compose compiler stability/skippability reports on demand:
    //   ./gradlew assemble -Pledger.composeCompilerReports=true
    // Kept off by default so normal builds aren't slowed by report generation.
    if (composeCompilerReports) {
        reportsDestination.set(layout.buildDirectory.dir("compose_compiler/reports"))
        metricsDestination.set(layout.buildDirectory.dir("compose_compiler/metrics"))
    }
}

// Windows-only workaround, and only while the reports are being generated. KGP 2.4.0 builds the
// default module name as "$group:$archivesName" (Project.moduleName in klibUtils.kt), so this
// project's is e.g. `Ledger.feature.posting:impl`. The Compose compiler derives its report
// filenames from that name, replacing only '.', '<' and '>' (ModuleMetricsImpl.saveReportsTo), so
// it asks NTFS for `Ledger_feature_posting:impl-classes.txt` — which NTFS reads as file
// `Ledger_feature_posting` plus alternate data stream `impl-classes.txt`. The reports are written,
// they are just invisible to anything that doesn't know to look for the stream. The Kotlin compiler
// itself got this sanitization (KT-82216, for .kotlin_module filenames); the Compose plugin's report
// writer did not, as of 2.4.10.
//
// Handing the compiler a colon-free but still unique module name puts the four report files back on
// disk under their documented names. Scoped to the property so ordinary builds keep the module name
// KGP chose; applied unconditionally it would rename every module and invalidate incremental caches
// for no benefit. Note the compile tasks are UP-TO-DATE after a config-only edit, so measuring this
// needs --rerun-tasks.
if (composeCompilerReports) {
    val colonFreeModuleName = "$group:$name".replace('.', '_').replace(':', '_')
    tasks.withType(KotlinJvmCompile::class.java).configureEach {
        compilerOptions.moduleName.set(colonFreeModuleName)
    }
}

kotlin {
    android {
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-runtime").get())
            implementation(libs.findLibrary("compose-components-resources").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
            implementation(libs.findLibrary("compose-ui-test").get())
            implementation(project(":core:test"))
        }
    }
}