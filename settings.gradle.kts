pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "sdkcheckoutandroid-github"

// Include your module(s) here.
// If your code is in a folder named 'app', use ":app".
// Based on your path, you might need to include your library module:
include(":sdkcheckoutandroid")