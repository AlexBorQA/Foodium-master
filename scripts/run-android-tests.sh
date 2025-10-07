#!/bin/bash
set -euo pipefail

SERIAL="${ANDROID_SERIAL:-${1:-emulator-5556}}"

ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
GRADLEW="$(dirname "$0")/../gradlew"

echo "Using device: $SERIAL"

# Disable animations for stable Espresso
"$ADB" -s "$SERIAL" shell settings put global window_animation_scale 0 || true
"$ADB" -s "$SERIAL" shell settings put global transition_animation_scale 0 || true
"$ADB" -s "$SERIAL" shell settings put global animator_duration_scale 0 || true

# Build and install APKs
JAVA_HOME=$(/usr/libexec/java_home -v 11) "$GRADLEW" :app:installDebug :app:installDebugAndroidTest --no-daemon --stacktrace --warning-mode all

PKG="dev.shreyaspatil.foodium"
RUNNER="$PKG.test/dev.shreyaspatil.foodium.CustomTestRunner"
TEST_CLASS="dev.shreyaspatil.foodium.ui.main.MainActivityTest"

function run_one() {
  local method="$1"
  echo "==> Running $TEST_CLASS#$method"
  "$ADB" -s "$SERIAL" shell am instrument -w -r -e class "$TEST_CLASS#$method" "$RUNNER"
}

case "${2:-all}" in
  posts) run_one postsRecyclerView_isDisplayed_onLaunch ;;
  click) run_one clickFirstItem_opens_PostDetailsActivity ;;
  about) run_one aboutMenu_opens_AboutActivity ;;
  all)
    "$ADB" -s "$SERIAL" shell am instrument -w -r -e class "$TEST_CLASS" "$RUNNER"
    ;;
  *)
    echo "Usage: $0 [serial] [posts|click|about|all]" >&2
    exit 2
    ;;
esac


