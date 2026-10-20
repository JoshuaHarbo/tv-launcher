#!/usr/bin/env bash
# Builds the debug APK and installs it on the TV. Run from this folder on your Mac.
# Does NOT set the launcher as default home and does NOT remove any apps.
set -euo pipefail
# TV address comes from local.properties (tv.ip=HOST:5555), which is gitignored.
TV=$(grep -E '^tv\.ip=' "$(dirname "$0")/local.properties" 2>/dev/null | cut -d= -f2)
[ -n "$TV" ] || { echo "Set tv.ip=<TV address>:5555 in local.properties (see local.properties.example)"; exit 1; }
cd "$(dirname "$0")"

# 1. Java 17+ (reuses Android Studio's bundled JDK if present)
AS_JDK="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
if [ -z "${JAVA_HOME:-}" ] && [ -d "$AS_JDK" ]; then export JAVA_HOME="$AS_JDK"; fi
# /usr/bin/java is a stub on macOS, so ask java_home for a real JDK instead of `command -v java`
if [ -z "${JAVA_HOME:-}" ] && ! /usr/libexec/java_home -v 17+ >/dev/null 2>&1; then
  brew list openjdk@17 >/dev/null 2>&1 || brew install openjdk@17
  export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

# 2. Android SDK (reuses Android Studio's SDK if present)
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
if [ ! -d "$ANDROID_HOME/platforms/android-34" ]; then
  command -v sdkmanager >/dev/null || brew install --cask android-commandlinetools
  mkdir -p "$ANDROID_HOME"
  yes | sdkmanager --sdk_root="$ANDROID_HOME" --licenses >/dev/null || true
  sdkmanager --sdk_root="$ANDROID_HOME" "platforms;android-34" "build-tools;34.0.0" "platform-tools"
fi
touch local.properties
grep -v '^sdk.dir=' local.properties > local.properties.tmp || true
echo "sdk.dir=$ANDROID_HOME" >> local.properties.tmp
mv local.properties.tmp local.properties

# 3. Gradle wrapper (one time)
if [ ! -f gradlew ]; then
  command -v gradle >/dev/null || brew install gradle
  gradle wrapper --gradle-version 8.9
fi

# 4. Build, install, open
./fetch-fonts.sh
./gradlew assembleDebug
APK=app/build/outputs/apk/debug/app-debug.apk
adb connect "$TV" >/dev/null
adb -s "$TV" install -r "$APK"
adb -s "$TV" shell am start -n com.josh.tvhome/.MainActivity
echo "Done. Check the TV."
