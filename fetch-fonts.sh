#!/usr/bin/env bash
# Downloads the two open-source fonts the launcher bundles (Doto, Space Mono) into app/src/main/res/font.
set -euo pipefail
cd "$(dirname "$0")"
F=app/src/main/res/font
B=https://raw.githubusercontent.com/google/fonts/main/ofl
mkdir -p "$F"
[ -f "$F/doto.ttf" ] || curl -fL "$B/doto/Doto%5BROND%2Cwght%5D.ttf" -o "$F/doto.ttf"
[ -f "$F/space_mono_regular.ttf" ] || curl -fL "$B/spacemono/SpaceMono-Regular.ttf" -o "$F/space_mono_regular.ttf"
[ -f "$F/space_mono_bold.ttf" ] || curl -fL "$B/spacemono/SpaceMono-Bold.ttf" -o "$F/space_mono_bold.ttf"
ls -la "$F"
