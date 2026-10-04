@echo off
setlocal enabledelayedexpansion
echo =========================================
echo BorrowTrack Windows Build ^& Run Tool
echo =========================================

rem 1. Unset conflicting environment variables
set "ANDROID_PREFS_ROOT="

rem 2. Detect ANDROID_HOME
if not defined ANDROID_HOME (
    if defined ANDROID_SDK_ROOT (
        set "ANDROID_HOME=%ANDROID_SDK_ROOT%"
    ) else if exist "%LOCALAPPDATA%\Android\Sdk" (
        set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
    ) else if exist "C:\src\android\SDK" (
        set "ANDROID_HOME=C:\src\android\SDK"
    )
)

if not defined ANDROID_HOME (
    echo [ERROR] Could not automatically locate Android SDK.
    echo Please set the ANDROID_HOME environment variable.
    exit /b 1
)

echo [INFO] Using Android SDK at: %ANDROID_HOME%

rem 3. Ensure app\local.properties exists
if not exist "app\local.properties" (
    echo [INFO] Creating app\local.properties...
    set "SDK_PATH_KTS=%ANDROID_HOME:\=/%"
    echo sdk.dir=!SDK_PATH_KTS!> app\local.properties
)

rem 4. Detect JAVA_HOME
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-21" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    ) else if exist "C:\Program Files\Android\Android Studio\jbr" (
        set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    )
)

if defined JAVA_HOME (
    echo [INFO] Using JAVA_HOME at: %JAVA_HOME%
    set "KEYTOOL_BIN=%JAVA_HOME%\bin\keytool.exe"
) else (
    set "KEYTOOL_BIN=keytool"
)

rem 5. Ensure required debug keystore exists
if not exist "..\android-toolchain" (
    mkdir "..\android-toolchain" 2>nul
)
if not exist "..\android-toolchain\debug.keystore" (
    echo [INFO] Generating debug keystore at ..\android-toolchain\debug.keystore...
    "%KEYTOOL_BIN%" -genkey -v -keystore "..\android-toolchain\debug.keystore" -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 10000
)

rem 6. Ensure dummy local-repo directory exists to satisfy settings.gradle.kts
if not exist "C:\home\hatch\workspace\android-toolchain\local-repo" (
    mkdir "C:\home\hatch\workspace\android-toolchain\local-repo" 2>nul
)

rem 7. Find or download Gradle
set "GRADLE_BIN="
where gradle >nul 2>nul
if %errorlevel% equ 0 (
    set "GRADLE_BIN=gradle"
) else if exist "%USERPROFILE%\.gradle\gradle-8.7\bin\gradle.bat" (
    set "GRADLE_BIN=%USERPROFILE%\.gradle\gradle-8.7\bin\gradle.bat"
) else (
    echo [INFO] Gradle 8.7 not found. Installing to %USERPROFILE%\.gradle\gradle-8.7...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.7-bin.zip' -OutFile '$env:TEMP\gradle-8.7-bin.zip'; Expand-Archive -Path '$env:TEMP\gradle-8.7-bin.zip' -DestinationPath '$env:USERPROFILE\.gradle' -Force; Remove-Item '$env:TEMP\gradle-8.7-bin.zip'"
    set "GRADLE_BIN=%USERPROFILE%\.gradle\gradle-8.7\bin\gradle.bat"
)

rem 8. Build the app
echo [INFO] Building debug APK with Gradle...
cd app
call "%GRADLE_BIN%" assembleDebug %*
if %errorlevel% neq 0 (
    echo [ERROR] Gradle build failed.
    cd ..
    exit /b %errorlevel%
)
cd ..

rem 9. Install and run on connected ADB device if available
where adb >nul 2>nul
if %errorlevel% equ 0 (
    echo [INFO] Checking for connected ADB devices...
    for /f "tokens=1,2" %%A in ('adb devices ^| findstr /v "List of devices attached"') do (
        if "%%B"=="device" (
            echo [INFO] Installing APK to ADB device %%A...
            adb -s %%A install -r app\app\build\outputs\apk\debug\app-debug.apk
            echo [INFO] Launching BorrowTrack on %%A...
            adb -s %%A shell am start -n com.deon.borrowtrack/.MainActivity
            goto :done
        )
    )
    echo [WARN] No connected ADB device found. APK built at: app\app\build\outputs\apk\debug\app-debug.apk
) else (
    echo [WARN] ADB command not found in PATH. APK built at: app\app\build\outputs\apk\debug\app-debug.apk
)

:done
echo [SUCCESS] Build and run complete!
