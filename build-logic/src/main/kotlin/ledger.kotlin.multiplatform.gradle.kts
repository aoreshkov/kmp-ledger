import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlinx.kover")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget>("android") {
        compileSdk = libs.findVersion("android-sdk-compile").get().requiredVersion.toInt()
        minSdk = libs.findVersion("android-sdk-min").get().requiredVersion.toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
        }
    }
}