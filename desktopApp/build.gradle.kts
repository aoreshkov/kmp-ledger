import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
    implementation(libs.compose.ui)
    implementation(compose.desktop.currentOs)
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(libs.kotlinx.coroutines.swing)
    testImplementation(project(":core:data"))
    testImplementation(project(":core:database"))
    testImplementation(libs.kotlin.test)
    testImplementation(libs.koin.test)
    testImplementation(libs.compose.ui.test)
}

compose.desktop {
    application {
        mainClass = "app.oreshkov.ledger.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ledger"
            packageVersion = "1.0.0"

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