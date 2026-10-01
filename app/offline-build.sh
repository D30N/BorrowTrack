#!/bin/bash
# Offline Android build using the vendored local-repo (no network needed).
set -u
export JAVA_HOME=~/workspace/android-toolchain/jdk17
export GRADLE_USER_HOME=~/workspace/android-toolchain/gradle-home
export ANDROID_HOME=~/workspace/android-toolchain/android-sdk
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export GRADLE_OPTS="-Djava.net.preferIPv4Stack=true"

cd ~/workspace/borrowtrack/app
~/workspace/android-toolchain/gradle-8.7/bin/gradle --offline "$@"
