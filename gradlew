#!/usr/bin/env sh
# Lightweight Gradle bootstrapper for this project. It downloads Gradle 9.2.1
# on first use because the binary Gradle wrapper JAR is intentionally not vendored.
set -eu
VERSION="9.2.1"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DIST_ROOT="$GRADLE_USER_HOME/wrapper/dists/orange-client/gradle-$VERSION-bin"
GRADLE_HOME="$DIST_ROOT/gradle-$VERSION"

if [ -x "$GRADLE_HOME/bin/gradle" ]; then
  exec "$GRADLE_HOME/bin/gradle" "$@"
fi

if command -v gradle >/dev/null 2>&1; then
  if gradle --version 2>/dev/null | grep -q "Gradle $VERSION"; then
    exec gradle "$@"
  fi
fi

mkdir -p "$DIST_ROOT"
ZIP="$DIST_ROOT/gradle-$VERSION-bin.zip"
if [ ! -f "$ZIP" ]; then
  echo "Downloading Gradle $VERSION..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip"
  else
    echo "Install curl or wget, then run this command again." >&2
    exit 1
  fi
fi
if ! command -v unzip >/dev/null 2>&1; then
  echo "Install unzip, then run this command again." >&2
  exit 1
fi
unzip -oq "$ZIP" -d "$DIST_ROOT"
exec "$GRADLE_HOME/bin/gradle" "$@"
