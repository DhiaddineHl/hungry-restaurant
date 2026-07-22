pluginManagement {
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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Sunmi SDKs (printer, scanner, etc.) are hosted here.
        maven { url = uri("https://maven.sunmi.com/repository/maven-public/") }
    }
}

rootProject.name = "HungryRestaurantPOS"
include(":app")
