plugins {
    id("ledger.kotlin.multiplatform.koin")
    alias(libs.plugins.ksp)
    alias(libs.plugins.room3)
}

kotlin {
    android {
        namespace = "app.oreshkov.ledger.core.database"
        androidResources {
            enable = true
        }
        withHostTest {}
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(libs.room3.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.kotlinx.coroutines.core)
        }
        jvmMain.dependencies {
        }
        commonTest.dependencies {
            implementation(libs.bundles.common.test)
        }
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.sqlite.bundled.jvm)
            }
        }
    }
}

room3 {
    schemaDirectory(layout.projectDirectory.dir("schemas").asFile.path)
}

dependencies {
    add("kspAndroid", libs.room3.compiler)
    add("kspJvm", libs.room3.compiler)
    add("kspIosArm64", libs.room3.compiler)
    add("kspIosSimulatorArm64", libs.room3.compiler)
}