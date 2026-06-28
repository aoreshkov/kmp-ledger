rootProject.name = "Ledger"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":desktopApp")
include(":iosExport")
include(":core:bootstrap")
include(":core:common")
include(":core:compose")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:domain")
include(":core:model")
include(":core:navigation")
include(":core:ui")
include(":core:test")
include(":feature:posting:api")
include(":feature:posting:impl")
include(":feature:settings:api")
include(":feature:settings:impl")