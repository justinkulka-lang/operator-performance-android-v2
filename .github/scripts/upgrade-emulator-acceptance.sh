#!/usr/bin/env bash
set -euo pipefail
source_sha="${1:?candidate SHA required}"
test "$(git rev-parse HEAD)" = "$source_sha"
baseline=9cfbd896e7d7103e9833d0563a463a5eac91983c
sdk_root="${ANDROID_HOME:?SDK required}"
adb="$sdk_root/platform-tools/adb"
serial=emulator-5580
out="$PWD/app/build/reports/upgrade-persistence"
candidate="$PWD/app/build/outputs/apk/debug/app-debug.apk"
tests="$PWD/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
mkdir -p "$out"
baseline_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/legend-upgrade-source.XXXXXX")"
git worktree add --detach "$baseline_dir" "$baseline"
GITHUB_SHA="$baseline" gradle -p "$baseline_dir" :app:assembleDebug --stacktrace
before="$baseline_dir/app/build/outputs/apk/debug/app-debug.apk"
"$sdk_root/build-tools/36.0.0/apksigner" verify --print-certs "$before" > "$out/before-certificate.txt"
"$sdk_root/build-tools/36.0.0/apksigner" verify --print-certs "$candidate" > "$out/after-certificate.txt"
cmp "$out/before-certificate.txt" "$out/after-certificate.txt"
sha256sum "$before" "$candidate" > "$out/apk-hashes.txt"

# This device belongs only to this job. Never address a physical device or clear user data.
avd_name="legend_upgrade_${GITHUB_RUN_ID:-local}"
printf 'no\n' | avdmanager create avd -n "$avd_name" -k 'system-images;android-35;default;x86_64' -d pixel_2
# The stock Pixel profile requests a 12 GiB data partition, exceeding runner disk
# after the managed-device suite. Bound only this newly created test AVD to 2 GiB.
"$sdk_root/emulator/emulator" -avd "$avd_name" -port 5580 -no-window -no-audio -no-snapshot -no-boot-anim -gpu swiftshader -memory 2048 -cores 2 -partition-size 2048 > "$out/emulator.log" 2>&1 &
emulator_pid=$!
trap '"$adb" -s "$serial" emu kill >/dev/null 2>&1 || true' EXIT

wait_boot() {
  for attempt in {1..120}; do
    if ! kill -0 "$emulator_pid" 2>/dev/null; then
      tail -60 "$out/emulator.log" >&2
      echo 'Owned emulator exited before boot completed' >&2
      exit 1
    fi
    if [[ "$("$adb" -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]]; then
      "$adb" -s "$serial" shell input keyevent 82
      return
    fi
    sleep 2
  done
  echo 'Owned emulator boot timeout' >&2
  exit 1
}
snapshot() {
  "$adb" -s "$serial" exec-out run-as com.jk.operatorperformance cat shared_prefs/ares_v71_reference_state.xml > "$out/$1.xml"
}
instrument() {
  "$adb" -s "$serial" shell am instrument -w -r -e persistenceMode "$1" -e class "com.jk.operatorperformance.V72TimerPersistenceUiTest#$2" com.jk.operatorperformance.test/androidx.test.runner.AndroidJUnitRunner > "$out/$3.txt"
  grep -F 'OK (1 test)' "$out/$3.txt"
  if grep -E 'FAILURES|INSTRUMENTATION_FAILED' "$out/$3.txt"; then exit 1; fi
}

wait_boot
"$adb" -s "$serial" install "$before"
"$adb" -s "$serial" install "$tests"
instrument seed seedsManualRestTimer seed
snapshot before-upgrade
"$adb" -s "$serial" install -r "$candidate"
snapshot after-install
cmp "$out/before-upgrade.xml" "$out/after-install.xml"
"$adb" -s "$serial" shell am force-stop com.jk.operatorperformance
"$adb" -s "$serial" shell am start -W -n com.jk.operatorperformance/.MainActivity > "$out/relaunch.txt"
snapshot after-launch
cmp "$out/before-upgrade.xml" "$out/after-launch.xml"
instrument verify timerAndPointerSurviveProcessDeath force-stop-verification
snapshot before-reboot
"$adb" -s "$serial" reboot
wait_boot
snapshot after-reboot
cmp "$out/before-reboot.xml" "$out/after-reboot.xml"
instrument verify timerAndPointerSurviveProcessDeath reboot-verification
printf 'sourceCommit=%s\nbaselineCommit=%s\nprofileUpgrade=PASS\nforceStop=PASS\nreboot=PASS\nphysicalDevice=NOT_RUN\nphoneCertificateCompatibility=NOT_PROVEN\n' "$source_sha" "$baseline" > "$out/RESULT.txt"
