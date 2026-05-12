import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.the

plugins {
    id("ledger.kotlin.multiplatform.koin")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<VersionCatalogsExtension>().named("libs")

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