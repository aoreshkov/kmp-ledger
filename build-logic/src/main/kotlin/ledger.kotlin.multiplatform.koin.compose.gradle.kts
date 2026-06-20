import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.the

plugins {
    id("ledger.kotlin.multiplatform.koin")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<VersionCatalogsExtension>().named("libs")

// Emit Compose compiler stability/skippability reports on demand:
//   ./gradlew assemble -Pledger.composeCompilerReports=true
// Output lands in <module>/build/compose_compiler/*-classes.txt and *-composables.txt.
// Kept off by default so normal builds aren't slowed by report generation.
composeCompiler {
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