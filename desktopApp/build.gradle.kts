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