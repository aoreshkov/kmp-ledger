import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.the

plugins {
    id("ledger.kotlin.multiplatform.koin")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<VersionCatalogsExtension>().named("libs")

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
    //
    // KNOWN BROKEN as of Kotlin 2.4.0: this produces no report. Both destinations receive a single
    // 0-byte file named after the Kotlin module name instead of the documented <prefix>-classes.txt
    // / -composables.txt / -module.json. Not a collision between targets and not Gradle's stale-output
    // cleanup — reproduced with one module, one target, on a clean directory outside build/. To read a
    // class's stability meanwhile, the compiler's synthetic field says it directly (0 = stable,
    // 8 = unstable):
    //   javap -p -c <module>/build/classes/kotlin/jvm/main/<Class>.class | grep -A2 'static {}'
    if (providers.gradleProperty("ledger.composeCompilerReports").orNull.toBoolean()) {
        reportsDestination.set(layout.buildDirectory.dir("compose_compiler/reports"))
        metricsDestination.set(layout.buildDirectory.dir("compose_compiler/metrics"))
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