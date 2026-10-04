#!/usr/bin/env bash
set -euo pipefail
source_sha="${1:?full source commit required}"
[[ "$source_sha" =~ ^[0-9a-f]{40}$ ]]
test "$(git rev-parse HEAD)" = "$source_sha"
sdk_root="${ANDROID_HOME:?Android SDK required}"
tools="$sdk_root/build-tools/36.0.0"
apk=app/build/outputs/apk/debug/app-debug.apk
out=app/build/distribution
label="${source_sha:0:12}"
mkdir -p "$out"
"$tools/aapt2" dump badging "$apk" > "$out/badging.txt"
grep -F "package: name='com.jk.operatorperformance' versionCode='47' versionName='0.38.0'" "$out/badging.txt"
grep -F "application-label:'LEGEND'" "$out/badging.txt"
"$tools/aapt2" dump permissions "$apk" > "$out/permissions.txt"
if grep -F 'android.permission.INTERNET' "$out/permissions.txt"; then exit 1; fi
"$tools/apksigner" verify --verbose --print-certs "$apk" > "$out/signature.txt"
"$tools/zipalign" -c 4 "$apk" > "$out/zipalign.txt"
unzip -p "$apk" 'classes*.dex' | strings > "$out/dex-strings.txt"
grep -F "$label" "$out/dex-strings.txt"
cp "$apk" "$out/legend-v7.2-$label-debug.apk"
printf 'sourceCommit=%s\nembeddedLabel=%s\nkind=debug\nrelease=NO\n' "$source_sha" "$label" > "$out/BUILD.txt"
(cd "$out" && sha256sum "legend-v7.2-$label-debug.apk" > SHA256SUMS && zip -q "legend-v7.2-$label-debug.zip" "legend-v7.2-$label-debug.apk" SHA256SUMS BUILD.txt signature.txt)
sha256sum "$out/legend-v7.2-$label-debug.apk" "$out/legend-v7.2-$label-debug.zip"
