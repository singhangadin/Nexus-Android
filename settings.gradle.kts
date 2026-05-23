pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Nexus"

include(":app")
include(":benchmark")

// Core modules
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:ui")
include(":core:testing")

// Feature modules
include(":feature:auth")
include(":feature:workspace")
include(":feature:board")
include(":feature:task-detail")
include(":feature:search")
include(":feature:notifications")
include(":feature:analytics")
include(":feature:profile")
