plugins {
    `kotlin-dsl`
}

group = "app.oreshkov.kmpledger.buildlogic"

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.koin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
}