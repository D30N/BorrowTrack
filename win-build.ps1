# Windows PowerShell build and run script for BorrowTrack

$ErrorActionPreference = "Stop"

# Always change current working directory to script location
Set-Location $PSScriptRoot

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "BorrowTrack Windows Build & Run Tool" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# 1. Unset conflicting environment variables
Remove-Item env:ANDROID_PREFS_ROOT -ErrorAction SilentlyContinue

# 2. Detect ANDROID_HOME
if (-not $env:ANDROID_HOME) {
    if ($env:ANDROID_SDK_ROOT) {
        $env:ANDROID_HOME = $env:ANDROID_SDK_ROOT
    } elseif (Test-Path "$env:LOCALAPPDATA\Android\Sdk") {
        $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
    } elseif (Test-Path "C:\src\android\SDK") {
        $env:ANDROID_HOME = "C:\src\android\SDK"
    }
}

if (-not $env:ANDROID_HOME) {
    Write-Error "Could not locate Android SDK. Please set the ANDROID_HOME environment variable."
    exit 1
}

Write-Host "[INFO] Using Android SDK at: $env:ANDROID_HOME" -ForegroundColor Green

# 3. Ensure app\local.properties exists
if (-not (Test-Path "app\local.properties")) {
    Write-Host "[INFO] Creating app\local.properties..." -ForegroundColor Yellow
    $sdkPathKts = $env:ANDROID_HOME -replace '\\', '/'
    Set-Content -Path "app\local.properties" -Value "sdk.dir=$sdkPathKts"
}

# 4. Detect JAVA_HOME
if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Program Files\Java\jdk-21") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
    } elseif (Test-Path "C:\Program Files\Android\Android Studio\jbr") {
        $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
    }
}

$keytoolBin = if ($env:JAVA_HOME) { "$env:JAVA_HOME\bin\keytool.exe" } else { "keytool" }

# 5. Ensure required debug keystore exists
if (-not (Test-Path "..\android-toolchain")) {
    New-Item -ItemType Directory -Force -Path "..\android-toolchain" | Out-Null
}
if (-not (Test-Path "..\android-toolchain\debug.keystore")) {
    Write-Host "[INFO] Generating debug keystore at ..\android-toolchain\debug.keystore..." -ForegroundColor Yellow
    & $keytoolBin -genkey -v -keystore "..\android-toolchain\debug.keystore" -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 10000
}

# 6. Ensure dummy local-repo directory exists
if (-not (Test-Path "C:\home\hatch\workspace\android-toolchain\local-repo")) {
    New-Item -ItemType Directory -Force -Path "C:\home\hatch\workspace\android-toolchain\local-repo" | Out-Null
}

# 7. Find or download Gradle
$gradleBin = Get-Command gradle -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source
if (-not $gradleBin) {
    $gradlePath = "$env:USERPROFILE\.gradle\gradle-8.7\bin\gradle.bat"
    if (-not (Test-Path $gradlePath)) {
        Write-Host "[INFO] Installing Gradle 8.7 to $env:USERPROFILE\.gradle\gradle-8.7..." -ForegroundColor Yellow
        $tempZip = "$env:TEMP\gradle-8.7-bin.zip"
        Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-8.7-bin.zip" -OutFile $tempZip
        Expand-Archive -Path $tempZip -DestinationPath "$env:USERPROFILE\.gradle" -Force
        Remove-Item $tempZip
    }
    $gradleBin = $gradlePath
}

# 8. Build
Write-Host "[INFO] Building debug APK with Gradle..." -ForegroundColor Green
Set-Location "app"
& $gradleBin assembleDebug $args
Set-Location $PSScriptRoot

# 9. Install and run on connected ADB device
$adbCmd = Get-Command adb -ErrorAction SilentlyContinue
if ($adbCmd) {
    $devices = adb devices | Select-String -Pattern "\tdevice$"
    if ($devices) {
        foreach ($devLine in $devices) {
            $deviceId = ($devLine -split "\t")[0]
            Write-Host "[INFO] Installing APK to ADB device $deviceId..." -ForegroundColor Green
            adb -s $deviceId install -r app\app\build\outputs\apk\debug\app-debug.apk
            Write-Host "[INFO] Launching BorrowTrack on $deviceId..." -ForegroundColor Green
            adb -s $deviceId shell am start -n com.deon.borrowtrack/.MainActivity
        }
    } else {
        Write-Host "[WARN] No connected ADB device found." -ForegroundColor Yellow
    }
} else {
    Write-Host "[WARN] ADB command not found in PATH." -ForegroundColor Yellow
}

Write-Host "[SUCCESS] Build and run complete!" -ForegroundColor Green
