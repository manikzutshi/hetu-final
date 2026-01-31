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
        // SDK dependencies
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    }
}

rootProject.name = "HetuApp"
include(":app")

// Restore real RunAnywhere SDK
includeBuild("../runanywhere-sdks/sdk/runanywhere-kotlin") {
    dependencySubstitution {
        substitute(module("com.runanywhere.sdk:runanywhere-kotlin")).using(project(":"))
        substitute(module("com.runanywhere.sdk:runanywhere-core-llamacpp")).using(project(":modules:runanywhere-core-llamacpp"))
        substitute(module("com.runanywhere.sdk:runanywhere-core-onnx")).using(project(":modules:runanywhere-core-onnx"))
    }
}
