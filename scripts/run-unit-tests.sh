#!/bin/zsh
set -euo pipefail

export JAVA_HOME=$(/usr/libexec/java_home -v 11)

./gradlew clean :app:testDebugUnitTest

# Try to open HTML report on macOS; ignore errors in CI/non-GUI
open app/build/reports/tests/testDebugUnitTest/index.html || true




