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