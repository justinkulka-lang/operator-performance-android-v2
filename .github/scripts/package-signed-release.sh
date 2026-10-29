#!/usr/bin/env bash
# Decode the owner keystore only under RUNNER_TEMP, sign the release APK, check the
# public certificate, install it on a fresh emulator, then delete the keystore.
set -euo pipefail
source_sha="${1:?full source commit required}"
[[ "$source_sha" =~ ^[0-9a-f]{40}$ ]]
test "$(git rev-parse HEAD)" = "$source_sha"

for secret_name in LEGEND_RELEASE_KEYSTORE_B64 LEGEND_RELEASE_STORE_PASSWORD LEGEND_RELEASE_KEY_ALIAS LEGEND_RELEASE_KEY_PASSWORD; do
  if [[ -z "${!secret_name:-}" ]]; then
    echo "Missing Actions secret: $secret_name" >&2
    exit 1
  fi
done

sdk_root="${ANDROID_HOME:?Android SDK required}"
tools="$sdk_root/build-tools/36.0.0"
adb="$sdk_root/platform-tools/adb"
serial=emulator-5592
ks="${RUNNER_TEMP:-/tmp}/legend-release.jks"
out=app/build/reports/signing
dist=app/build/distribution-release
label="${source_sha:0:12}"
emulator_pid=""
mkdir -p "$out"
rm -rf "$dist"
mkdir -p "$dist"

cleanup() {
  status=$?
  rm -f "$ks"
  if [[ -n "$emulator_pid" ]]; then
    "$adb" -s "$serial" emu kill >/dev/null 2>&1 || true
  fi
  exit "$status"
}
trap cleanup EXIT

umask 077
printf '%s' "$LEGEND_RELEASE_KEYSTORE_B64" | base64 -d > "$ks"
export OPERATOR_RELEASE_KEYSTORE_PATH="$ks"
export OPERATOR_RELEASE_STORE_PASSWORD="$LEGEND_RELEASE_STORE_PASSWORD"
export OPERATOR_RELEASE_KEY_ALIAS="$LEGEND_RELEASE_KEY_ALIAS"
export OPERATOR_RELEASE_KEY_PASSWORD="$LEGEND_RELEASE_KEY_PASSWORD"
unset LEGEND_RELEASE_KEYSTORE_B64 LEGEND_RELEASE_STORE_PASSWORD LEGEND_RELEASE_KEY_PASSWORD
GITHUB_SHA="$source_sha" gradle :app:assembleRelease --stacktrace
unset OPERATOR_RELEASE_STORE_PASSWORD OPERATOR_RELEASE_KEY_PASSWORD

apk=app/build/outputs/apk/release/app-release.apk
test -f "$apk"
"$tools/apksigner" verify --print-certs "$apk" > "$out/release-certs.txt"
actual="$(awk -F': ' '/SHA-256 digest:/{print $2; exit}' "$out/release-certs.txt" | tr -d '[:space:]' | tr '[:upper:]' '[:lower:]')"
expected="$(tr -d '[:space:]' < docs/signing/RELEASE_CERT_SHA256.txt | tr '[:upper:]' '[:lower:]')"
if [[ "$actual" != "$expected" ]]; then
  echo "Release signer SHA-256 does not match docs/signing/RELEASE_CERT_SHA256.txt" >&2
  echo "actual=$actual" >&2
  echo "expected=$expected" >&2
  exit 1
fi
"$tools/aapt2" dump badging "$apk" > "$out/release-badging.txt"
package_line="package: name='com.jk.operatorperformance' versionCode='53' versionName='0.44.0'"
grep -F "$package_line" "$out/release-badging.txt"
grep -F "$package_line" app/build/distribution/badging.txt
grep -F "application-label:'LEGEND'" "$out/release-badging.txt"
echo "$package_line"

if [[ "${GITHUB_ACTIONS:-}" == true && "${RUNNER_ENVIRONMENT:-}" == github-hosted ]]; then
  for unused_tooling in /usr/share/dotnet /opt/hostedtoolcache/CodeQL; do
    available_bytes="$(df --output=avail -B1 "$out" | tail -1 | tr -d ' ')"
    if (( available_bytes >= 10737418240 )); then break; fi
    if [[ -d "$unused_tooling" ]]; then sudo rm -rf -- "$unused_tooling"; fi
  done
fi
available_bytes="$(df --output=avail -B1 "$out" | tail -1 | tr -d ' ')"
if (( available_bytes < 8589934592 )); then
  df -h "$out" >&2
  echo 'Release emulator requires at least 8 GiB free.' >&2
  exit 1
fi

sdkmanager --install 'system-images;android-35;google_apis;x86_64'
avd_name="legend_release_${GITHUB_RUN_ID:-local}"
printf 'no\n' | avdmanager create avd -n "$avd_name" -k 'system-images;android-35;google_apis;x86_64' -d pixel_2
"$sdk_root/emulator/emulator" -avd "$avd_name" -port 5592 -no-window -no-audio -no-snapshot -no-boot-anim -gpu swiftshader -memory 2048 -cores 2 -partition-size 2048 > "$out/release-emulator.log" 2>&1 &
emulator_pid=$!

for attempt in {1..120}; do
  if ! kill -0 "$emulator_pid" 2>/dev/null; then
    tail -60 "$out/release-emulator.log" >&2
    echo 'Release emulator exited before boot completed' >&2
    exit 1
  fi
  if [[ "$("$adb" -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == 1 ]]; then
    "$adb" -s "$serial" shell input keyevent 82
    break
  fi
  if [[ "$attempt" -eq 120 ]]; then
    echo 'Release emulator boot timeout' >&2
    exit 1
  fi
  sleep 2
done

"$adb" -s "$serial" install -r "$apk" > "$out/release-install.txt"
user_unlocked() {
  if "$adb" -s "$serial" shell dumpsys activity | tr -d '\r' | grep -q 'User #0: state=RUNNING_UNLOCKED'; then
    return 0
  fi
  "$adb" -s "$serial" logcat -d -b main -b system | tr -d '\r' | grep -q 'android.intent.action.USER_UNLOCKED'
}
launch_ok=0
for attempt in {1..30}; do
  if user_unlocked && "$adb" -s "$serial" shell am start -W -n com.jk.operatorperformance/.MainActivity > "$out/release-launch.txt" 2>&1 \
      && grep -F 'Status: ok' "$out/release-launch.txt"; then
    launch_ok=1
    break
  fi
  "$adb" -s "$serial" shell input keyevent 82 >/dev/null 2>&1 || true
  sleep 2
done
if [[ "$launch_ok" -ne 1 ]]; then
  echo 'Release APK launch did not report Status: ok' >&2
  cat "$out/release-launch.txt" >&2 || true
  exit 1
fi
grep -F 'Status: ok' "$out/release-launch.txt"

cp "$apk" "$dist/legend-v7.4-$label-release.apk"
(
  cd "$dist"
  sha256sum "legend-v7.4-$label-release.apk" > SHA256SUMS
)
if find "$dist" -iname '*.jks' | grep -q .; then
  echo 'The release upload must not contain a keystore.' >&2
  exit 1
fi
find "$dist" -type f -print
