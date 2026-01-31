# Simplified PowerShell script to restore RunAnywhere SDK v0.2.6
$ErrorActionPreference = "Stop"

$NDK = "C:/Users/Manik/AppData/Local/Android/Sdk/ndk/27.2.12479018"
$SDK_BIN = "C:/Users/Manik/AppData/Local/Android/Sdk/cmake/3.22.1/bin"
$CMAKE = "$SDK_BIN/cmake.exe"
$NINJA = "$SDK_BIN/ninja.exe"
$SDK_ROOT = "z:/test/runanywhere-sdks"
$COMMONS_DIR = "$SDK_ROOT/sdk/runanywhere-commons"
$SHERPA_DIR = "$COMMONS_DIR/third_party/sherpa-onnx-android"
$KOTLIN_SDK_DIR = "$SDK_ROOT/sdk/runanywhere-kotlin"
$ABI = "arm64-v8a"

Write-Host "--- RunAnywhere SDK Restoration (Proper Way) ---" -ForegroundColor Cyan

# 1. Ensure Sherpa-ONNX is there
if (!(Test-Path "$SHERPA_DIR/jniLibs/$ABI/libsherpa-onnx-jni.so")) {
    Write-Host "Downloading Sherpa-ONNX..."
    New-Item -ItemType Directory -Force -Path "$COMMONS_DIR/third_party"
    $url = "https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.12.20/sherpa-onnx-v1.12.20-android.tar.bz2"
    $tempFile = "$COMMONS_DIR/third_party/sherpa.tar.bz2"
    curl.exe -L -o $tempFile $url
    $oldPwd = Get-Location
    Set-Location "$COMMONS_DIR/third_party"
    tar.exe -xjf sherpa.tar.bz2
    $extractedDir = Get-ChildItem -Directory -Filter "sherpa-onnx-*-android" | Select-Object -First 1
    if ($extractedDir) { Rename-Item -Path $extractedDir.FullName -NewName "sherpa-onnx-android" }
    Set-Location $oldPwd
}

# 2. Build
$buildDir = "$COMMONS_DIR/build/android/unified/$ABI"
if (Test-Path $buildDir) { Remove-Item -Recurse -Force $buildDir }
New-Item -ItemType Directory -Force -Path $buildDir
Set-Location $buildDir

Write-Host "Configuring and Building..." -ForegroundColor Yellow

# Use CMD to avoid PS parsing issues
$cmakeCmd = "`"$CMAKE`" -G Ninja -DCMAKE_MAKE_PROGRAM=`"$NINJA`" " +
            "-DCMAKE_TOOLCHAIN_FILE=`"$NDK/build/cmake/android.toolchain.cmake`" " +
            "-DANDROID_ABI=$ABI -DANDROID_PLATFORM=android-24 -DANDROID_STL=c++_shared " +
            "-DCMAKE_BUILD_TYPE=Release -DRAC_BUILD_BACKENDS=ON -DRAC_BUILD_JNI=ON " +
            "-DRAC_BACKEND_ONNX=ON -DRAC_BACKEND_LLAMACPP=ON -DRAC_BUILD_TESTS=OFF -DRAC_BUILD_SHARED=ON " +
            "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON " +
            "-DCMAKE_SHARED_LINKER_FLAGS=`"-Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384`" " +
            "`"../../../../`""

cmd /c $cmakeCmd
cmd /c "`"$CMAKE`" --build . -j 8"

# 3. Copy
Write-Host "Installing..." -ForegroundColor Yellow
$targetBase = "$KOTLIN_SDK_DIR/src/androidMain/jniLibs/$ABI"
New-Item -ItemType Directory -Force -Path $targetBase
$llamaBase = "$KOTLIN_SDK_DIR/modules/runanywhere-core-llamacpp/src/androidMain/jniLibs/$ABI"
New-Item -ItemType Directory -Force -Path $llamaBase
$onnxBase = "$KOTLIN_SDK_DIR/modules/runanywhere-core-onnx/src/androidMain/jniLibs/$ABI"
New-Item -ItemType Directory -Force -Path $onnxBase

Copy-Item "src/jni/librunanywhere_jni.so" "$targetBase/" -Force
Copy-Item "librac_commons.so" "$targetBase/" -Force
Copy-Item "$NDK/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/lib/aarch64-linux-android/libc++_shared.so" "$targetBase/" -Force
Copy-Item "src/backends/llamacpp/librac_backend_llamacpp.so" "$llamaBase/" -Force
Copy-Item "src/backends/llamacpp/librac_backend_llamacpp_jni.so" "$llamaBase/" -Force
Copy-Item "$NDK/toolchains/llvm/prebuilt/windows-x86_64/lib/clang/18/lib/linux/aarch64/libomp.so" "$llamaBase/" -Force
Copy-Item "src/backends/onnx/librac_backend_onnx.so" "$onnxBase/" -Force
Copy-Item "src/backends/onnx/librac_backend_onnx_jni.so" "$onnxBase/" -Force
Copy-Item "$SHERPA_DIR/jniLibs/$ABI/*.so" "$onnxBase/" -Force

Write-Host "SUCCESS!" -ForegroundColor Green
