#!/bin/bash
# Manual BorrowTrack build — Gradle daemon is unreliable on this VM (Oct 2026),
# so this pipeline builds the release APK directly:
# aapt2 (resources) -> kotlinc (sources) -> d8 (dex) -> zipalign -> apksigner.
# Usage: bash manual-build.sh <versionName> <versionCode> <out.apk>
set -e
VN=${1:?versionName}; VC=${2:?versionCode}; OUTAPK=${3:?out apk}
export JAVA_HOME=~/workspace/android-toolchain/jdk17
export PATH=$JAVA_HOME/bin:$PATH
TC=~/workspace/android-toolchain
BT=$TC/android-sdk/build-tools/34.0.0
AJ=$TC/android-sdk/platforms/android-34/android.jar
GH=$TC/gradle-home/caches
CORE=$(find $GH/transforms-4 -name 'core-1.9.0-runtime.jar' | head -1)
STDLIB=$(find $GH/transforms-4 -name 'kotlin-stdlib-1.9.22.jar' | head -1)
KOTLINC_JAR=$(find $GH/transforms-4 -name 'kotlin-compiler-embeddable-1.9.24.jar' | head -1)
TROVE=$(find $GH/transforms-4 -name 'trove4j-1.0.20200330.jar' | grep original | head -1)
ANN=$(find $GH/transforms-4 -name 'annotations-13.0.jar' | head -1)
SRC=~/workspace/borrowtrack/app/app/src/main
OUT=/tmp/btmanual
rm -rf $OUT; mkdir -p $OUT/gen $OUT/compiled $OUT/classes $OUT/dex
sed "s|<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">|<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"com.deon.borrowtrack\" android:versionCode=\"$VC\" android:versionName=\"$VN\">|" \
  $SRC/AndroidManifest.xml > $OUT/AndroidManifest.xml
$BT/aapt2 compile --dir $SRC/res -o $OUT/compiled
$BT/aapt2 link -o $OUT/base.apk -I $AJ --manifest $OUT/AndroidManifest.xml \
  --java $OUT/gen --min-sdk-version 26 --target-sdk-version 34 $OUT/compiled/*.flat
java -cp $KOTLINC_JAR:$STDLIB:$TROVE:$ANN org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -cp $AJ:$CORE:$STDLIB -jvm-target 17 -no-stdlib -no-reflect \
  -d $OUT/classes $(find $SRC/java -name '*.kt') $(find $OUT/gen -name 'R.java')
RJAVA=$(find $OUT/gen -name 'R.java' | head -1)
javac -cp $AJ:$OUT/classes -d $OUT/classes $RJAVA
(cd $OUT/classes && jar cf $OUT/classes.jar .)
$BT/d8 --min-api 26 --lib $AJ --output $OUT/dex $OUT/classes.jar $CORE $STDLIB
cp $OUT/base.apk $OUT/app-unsigned.apk
(cd $OUT/dex && zip -q $OUT/app-unsigned.apk classes.dex)
$BT/zipalign -f 4 $OUT/app-unsigned.apk $OUT/aligned.apk
$BT/apksigner sign --ks $TC/debug.keystore --ks-pass pass:android \
  --key-pass pass:android --ks-key-alias androiddebugkey --out $OUTAPK $OUT/aligned.apk
echo "BUILT $OUTAPK"
$BT/aapt dump badging $OUTAPK | grep -E 'package:'
$BT/apksigner verify --print-certs $OUTAPK | grep 'SHA-256 digest' | head -1
