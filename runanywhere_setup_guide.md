# RunAnywhere SDK Local Integration Guide

> Complete setup instructions for integrating the RunAnywhere SDK into an Android app using local repositories for fully offline LLM inference.

---

## Prerequisites

### Required Repositories (Clone These)

```bash
# Main SDK - REQUIRED
git clone https://github.com/RunanywhereAI/runanywhere-sdks.git

# Pre-built binaries - REQUIRED for offline setup
git clone https://github.com/RunanywhereAI/runanywhere-binaries.git

# Optional - only if rebuilding native code
git clone https://github.com/RunanywhereAI/runanywhere-core.git
```

### Required Versions

| Component | Version | Notes |
|-----------|---------|-------|
| **Kotlin** | 2.1.21 | SDK requirement - cannot use 1.9.x |
| **AGP** | 8.11.2 | Android Gradle Plugin |
| **Gradle** | 8.13 | Wrapper version |
| **KSP** | 2.0.21-1.0.28 | Symbol processing (find exact compatible version) |
| **Compose** | Plugin-based | Use `kotlin.plugin.compose`, not `composeOptions` |
| **Java** | 17 | sourceCompatibility/targetCompatibility |
| **compileSdk** | 35+ | Required by SDK dependencies |

---

## Step 1: Project Structure

Place repositories in your project root:

```
your-android-project/
├── app/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── runanywhere-sdks/           # Clone here
├── runanywhere-binaries/       # Clone here
└── local.properties
```

---

## Step 2: Update Gradle Wrapper

**File: `gradle/wrapper/gradle-wrapper.properties`**

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
```

---

## Step 3: Root build.gradle.kts

**File: `build.gradle.kts` (project root)**

```kotlin
// Top-level build file
plugins {
    id("com.android.application") version "8.11.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.21" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21" apply false
}
```

> **Note**: Find the exact KSP version compatible with Kotlin 2.1.21 from [KSP Releases](https://github.com/google/ksp/releases)

---

## Step 4: settings.gradle.kts

**File: `settings.gradle.kts`**

```kotlin
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

rootProject.name = "YourAppName"
include(":app")

// Include local RunAnywhere SDK
includeBuild("runanywhere-sdks/sdk/runanywhere-kotlin") {
    dependencySubstitution {
        substitute(module("com.runanywhere.sdk:runanywhere-kotlin")).using(project(":"))
        substitute(module("com.runanywhere.sdk:runanywhere-core-llamacpp")).using(project(":modules:runanywhere-core-llamacpp"))
        substitute(module("com.runanywhere.sdk:runanywhere-core-onnx")).using(project(":modules:runanywhere-core-onnx"))
    }
}
```

---

## Step 5: App build.gradle.kts

**File: `app/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")  // Required for Kotlin 2.x
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.yourapp.name"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yourapp.name"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // NOTE: Do NOT use composeOptions {} with Kotlin 2.x
    // The compose plugin handles it automatically

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            pickFirst("lib/arm64-v8a/libonnxruntime.so")
            pickFirst("lib/armeabi-v7a/libonnxruntime.so")
            pickFirst("lib/x86/libonnxruntime.so")
            pickFirst("lib/x86_64/libonnxruntime.so")
        }
    }
}

dependencies {
    // RunAnywhere SDK (resolved from local includeBuild)
    implementation("com.runanywhere.sdk:runanywhere-kotlin:0.1.5-SNAPSHOT")
    implementation("com.runanywhere.sdk:runanywhere-core-llamacpp:0.1.5-SNAPSHOT")
    
    // Your other dependencies...
}
```

---

## Step 6: SDK Configuration Files

### 6a. Create local.properties for SDK

**File: `runanywhere-sdks/sdk/runanywhere-kotlin/local.properties`**

```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

### 6b. Configure SDK gradle.properties

**File: `runanywhere-sdks/sdk/runanywhere-kotlin/gradle.properties`**

Set these values:
```properties
runanywhere.testLocal=true
runanywhere.rebuildCommons=false
```

---

## Step 7: SDK Build Script Fix (Windows)

The SDK's build script uses Unix commands. For Windows, edit:

**File: `runanywhere-sdks/sdk/runanywhere-kotlin/build.gradle.kts`**

Find the `buildLocalJniLibs` task (around line 376) and change:

```kotlin
// FROM:
commandLine("echo", "JNI libs up to date")

// TO:
if (System.getProperty("os.name").lowercase().contains("win")) {
    commandLine("cmd", "/c", "echo", "JNI libs up to date")
} else {
    commandLine("echo", "JNI libs up to date")
}
```

---

## Step 8: Standardize SDK Group IDs

Edit these files to set `group = "com.runanywhere.sdk"`:

1. **`runanywhere-sdks/sdk/runanywhere-kotlin/build.gradle.kts`** (~line 46-52)
2. **`runanywhere-sdks/sdk/runanywhere-kotlin/modules/runanywhere-core-llamacpp/build.gradle.kts`** (~line 215)
3. **`runanywhere-sdks/sdk/runanywhere-kotlin/modules/runanywhere-core-onnx/build.gradle.kts`** (~line 225)

Change from conditional logic to:
```kotlin
group = "com.runanywhere.sdk"
```

---

## Step 9: Manual JNI Library Setup (Offline Mode)

### Extract binaries from runanywhere-binaries

```powershell
# Extract LlamaCPP binaries
Expand-Archive -Path "runanywhere-binaries/releases/core-v0.2.6/RABackendLlamaCPP-android-v0.2.6.zip" -DestinationPath "temp_libs/llamacpp"

# Extract ONNX binaries
Expand-Archive -Path "runanywhere-binaries/releases/core-v0.2.6/RABackendONNX-android-v0.2.6.zip" -DestinationPath "temp_libs/onnx"
```

### Copy to SDK jniLibs directories

```powershell
# Main SDK jniLibs
Copy-Item "temp_libs/llamacpp/RABackendLlamaCPP-android-v0.2.6/llamacpp/arm64-v8a/*" "runanywhere-sdks/sdk/runanywhere-kotlin/src/androidMain/jniLibs/arm64-v8a/"
Copy-Item "temp_libs/onnx/RABackendONNX-android-v0.2.6/onnx/arm64-v8a/*" "runanywhere-sdks/sdk/runanywhere-kotlin/src/androidMain/jniLibs/arm64-v8a/"

# LlamaCPP module jniLibs
mkdir -Force "runanywhere-sdks/sdk/runanywhere-kotlin/modules/runanywhere-core-llamacpp/src/androidMain/jniLibs/arm64-v8a"
Copy-Item "runanywhere-sdks/sdk/runanywhere-kotlin/src/androidMain/jniLibs/arm64-v8a/librac_backend_llamacpp_jni.so" "runanywhere-sdks/sdk/runanywhere-kotlin/modules/runanywhere-core-llamacpp/src/androidMain/jniLibs/arm64-v8a/"

# ONNX module jniLibs
mkdir -Force "runanywhere-sdks/sdk/runanywhere-kotlin/modules/runanywhere-core-onnx/src/androidMain/jniLibs/arm64-v8a"
Copy-Item "runanywhere-sdks/sdk/runanywhere-kotlin/src/androidMain/jniLibs/arm64-v8a/librac_backend_onnx_jni.so" "runanywhere-sdks/sdk/runanywhere-kotlin/modules/runanywhere-core-onnx/src/androidMain/jniLibs/arm64-v8a/"
```

### Expected libraries in arm64-v8a:

| Library | Source |
|---------|--------|
| `libc++_shared.so` | LlamaCPP |
| `libomp.so` | LlamaCPP |
| `librac_backend_llamacpp_jni.so` | LlamaCPP |
| `librunanywhere_llamacpp.so` | LlamaCPP |
| `libonnxruntime.so` | ONNX |
| `librac_backend_onnx_jni.so` | ONNX |
| `librunanywhere_onnx.so` | ONNX |
| `libsherpa-onnx-c-api.so` | ONNX |
| `libsherpa-onnx-cxx-api.so` | ONNX |
| `libsherpa-onnx-jni.so` | ONNX |

---

## Step 10: Build

```bash
./gradlew installDebug
```

---

## Known Issues & Solutions

### Issue: "Unresolved reference: isJitPack"
**Solution**: Remove `(JitPack=$isJitPack)` from logger.lifecycle() call in SDK build.gradle.kts

### Issue: "androidUnitTest" not found
**Solution**: Change `androidUnitTest {` to `val androidUnitTest by getting {`

### Issue: Kotlin metadata version mismatch
**Solution**: Ensure app Kotlin version matches SDK (2.1.21)

### Issue: KSP version not found
**Solution**: Check [KSP releases](https://github.com/google/ksp/releases) for exact compatible version

### Issue: "Compose Compiler Gradle plugin is required"
**Solution**: Add `id("org.jetbrains.kotlin.plugin.compose")` to plugins block

---

## Files Summary

| File | Key Changes |
|------|-------------|
| `gradle-wrapper.properties` | Gradle 8.13 |
| Root `build.gradle.kts` | AGP 8.11.2, Kotlin 2.1.21, KSP, Compose plugin |
| `settings.gradle.kts` | `includeBuild()` with dependency substitution |
| App `build.gradle.kts` | compileSdk 35, Compose plugin, Java 17 |
| SDK `local.properties` | Android SDK path |
| SDK `gradle.properties` | `testLocal=true` |
| SDK `build.gradle.kts` | Windows echo fix, group ID |

---

## Alternative: Wait for Published SDK

Instead of local integration, you can wait for RunAnywhere to publish stable releases to Maven Central, then simply add:

```kotlin
implementation("com.runanywhere.sdk:runanywhere-kotlin:VERSION")
implementation("com.runanywhere.sdk:runanywhere-core-llamacpp:VERSION")
```

This avoids all the version compatibility issues.

---

*Last updated: January 30, 2026*
