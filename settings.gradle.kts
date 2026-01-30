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
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    }
}

rootProject.name = "Hetu"
include(":app")

// Include local RunAnywhere SDK with dependency substitution
includeBuild("../runanywhere-sdks/sdk/runanywhere-kotlin") {
    dependencySubstitution {
        substitute(module("com.runanywhere.sdk:runanywhere-kotlin")).using(project(":"))
        substitute(module("com.runanywhere.sdk:runanywhere-core-llamacpp")).using(project(":modules:runanywhere-core-llamacpp"))
        substitute(module("com.runanywhere.sdk:runanywhere-core-onnx")).using(project(":modules:runanywhere-core-onnx"))
    }
}
