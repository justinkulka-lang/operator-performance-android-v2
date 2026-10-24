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

# The system image's base userdata needs about 7.4 GB even with -partition-size.
# Free only unused preinstalled tooling on a disposable GitHub-hosted runner.
# Never apply this cleanup to a local machine, self-hosted runner, SDK, or user data.
if [[ "${GITHUB_ACTIONS:-}" == true && "${RUNNER_ENVIRONMENT:-}" == github-hosted ]]; then
  for unused_tooling in /usr/share/dotnet /opt/hostedtoolcache/CodeQL; do
    available_bytes="$(df --output=avail -B1 "$out" | tail -1 | tr -d ' ')"
    if (( available_bytes >= 10737418240 )); then break; fi
    if [[ -d "$unused_tooling" ]]; then sudo rm -rf -- "$unused_tooling"; fi
  done
fi
df -h "$out" > "$out/disk-before-emulator.txt"
available_bytes="$(df --output=avail -B1 "$out" | tail -1 | tr -d ' ')"
if (( available_bytes < 8589934592 )); then
  cat "$out/disk-before-emulator.txt" >&2
  echo 'Upgrade emulator requires at least 8 GiB free for the base userdata image.' >&2
  exit 1
fi

# Install the image explicitly: managed-device caches do not guarantee avdmanager metadata.
sdkmanager --install 'system-images;android-35;google_apis;x86_64'

# This device belongs only to this job. Never address a physical device or clear user data.
avd_name="legend_upgrade_${GITHUB_RUN_ID:-local}"
printf 'no\n' | avdmanager create avd -n "$avd_name" -k 'system-images;android-35;google_apis;x86_64' -d pixel_2
# The stock Pixel profile requests a 12 GiB data partition, exceeding runner disk
# after the managed-device suite. Bound only this newly created test AVD to 2 GiB.
"$sdk_root/emulator/emulator" -avd "$avd_name" -port 5580 -no-window -no-audio -no-snapshot -no-boot-anim -gpu swiftshader -memory 2048 -cores 2 -partition-size 2048 > "$out/emulator.log" 2>&1 &
emulator_pid=$!
cleanup() {
  result=$?
  "$adb" -s "$serial" logcat -d -b crash -b main -b system > "$out/final-logcat.txt" 2>&1 || true
  "$adb" -s "$serial" shell dumpsys activity exit-info com.jk.operatorperformance > "$out/process-exit-info.txt" 2>&1 || true
  "$adb" -s "$serial" emu kill >/dev/null 2>&1 || true
  exit "$result"
}
trap cleanup EXIT

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
  "$adb" -s "$serial" logcat -d -b crash -b main -b system > "$out/$3-logcat.txt" 2>&1 || true
  if ! grep -F 'OK (1 test)' "$out/$3.txt"; then
    cat "$out/$3.txt" >&2
    echo "Instrumentation failed; Android logs retained in $out/$3-logcat.txt" >&2
    return 1
  fi
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
python3 .github/scripts/verify-v73-upgrade-state.py "$out/before-upgrade.xml" "$out/after-launch.xml"
instrument verify timerAndPointerSurviveProcessDeath force-stop-verification
snapshot before-reboot
"$adb" -s "$serial" reboot
wait_boot
snapshot after-reboot
cmp "$out/before-reboot.xml" "$out/after-reboot.xml"
# Complete a normal launch before attaching instrumentation to the rebooted runtime.
# Keep the pre-launch byte comparison above: launch may legitimately expire a timer.
"$adb" -s "$serial" shell am start -W -n com.jk.operatorperformance/.MainActivity > "$out/reboot-launch.txt"
grep -F 'Status: ok' "$out/reboot-launch.txt"
instrument verify timerAndPointerSurviveProcessDeath reboot-verification

# Second scenario: install 0.40.0 (main 7cd35a5), seed Week 12 Day 3 with a workout
# and a substitution, then install the candidate. The seed uses only keys that build writes.
v73_base=7cd35a5a8a47d52acb8cc84b472067604c269d69
v73_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/legend-v73-upgrade-source.XXXXXX")"
git worktree add --detach "$v73_dir" "$v73_base"
GITHUB_SHA="$v73_base" gradle -p "$v73_dir" :app:assembleDebug --stacktrace
v73_apk="$v73_dir/app/build/outputs/apk/debug/app-debug.apk"
"$sdk_root/build-tools/36.0.0/apksigner" verify --print-certs "$v73_apk" > "$out/v73-before-certificate.txt"
cmp "$out/v73-before-certificate.txt" "$out/after-certificate.txt"
"$adb" -s "$serial" install -r -d "$v73_apk"
"$adb" -s "$serial" shell am force-stop com.jk.operatorperformance
"$adb" -s "$serial" shell pm clear com.jk.operatorperformance
"$adb" -s "$serial" shell am start -W -n com.jk.operatorperformance/.MainActivity > "$out/v73-first-launch.txt"
"$adb" -s "$serial" shell am force-stop com.jk.operatorperformance
python3 - "$out/v73-seed.xml" <<'PY'
import pathlib, sys
path = pathlib.Path(sys.argv[1])
path.write_text("""<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <boolean name="v71Initialized" value="true" />
    <int name="currentWeek" value="12" />
    <int name="currentDay" value="3" />
    <string name="authorityVersion">V73</string>
    <boolean name="v73MigrationApplied" value="true" />
    <int name="v73ActivationWeek" value="1" />
    <boolean name="v72MigrationApplied" value="true" />
    <int name="v72MigrationRevision" value="2" />
    <boolean name="v72ActivationProven" value="true" />
    <int name="engagedAbsoluteDay" value="80" />
    <boolean name="workoutActive" value="true" />
    <string name="workoutAuthority">V73</string>
    <int name="workoutSchema" value="5" />
    <int name="workoutWeek" value="12" />
    <int name="workoutDay" value="3" />
    <int name="movementIndex" value="0" />
    <int name="effortIndex" value="1" />
    <boolean name="complete" value="false" />
    <set name="substitutions">
        <string>v73_slot&#x1f;Home alternate</string>
    </set>
    <boolean name="hasUndo" value="false" />
</map>
""")
PY
seed_b64="$(base64 -w0 "$out/v73-seed.xml")"
# Keep the remote sh -c script quoted. A locally consumed quote makes the
# device shell apply `>` as the shell user, outside run-as, where shared_prefs
# does not exist.
"$adb" -s "$serial" shell "run-as com.jk.operatorperformance sh -c 'mkdir -p shared_prefs && printf %s $seed_b64 | base64 -d > shared_prefs/ares_v71_reference_state.xml'"
snapshot v73-before-upgrade
"$adb" -s "$serial" install -r "$candidate"
snapshot v73-after-install
cmp "$out/v73-before-upgrade.xml" "$out/v73-after-install.xml"
"$adb" -s "$serial" shell am force-stop com.jk.operatorperformance
"$adb" -s "$serial" shell am start -W -n com.jk.operatorperformance/.MainActivity > "$out/v73-relaunch.txt"
snapshot v73-after-launch
python3 .github/scripts/verify-v73-upgrade-state.py "$out/v73-before-upgrade.xml" "$out/v73-after-launch.xml" from-v73
printf 'sourceCommit=%s\nbaselineCommit=%s\nv73BaselineCommit=%s\nprofileUpgrade=PASS\nforceStop=PASS\nreboot=PASS\nv73MidProgramUpgrade=PASS\nphysicalDevice=NOT_RUN\nphoneCertificateCompatibility=NOT_PROVEN\n' "$source_sha" "$baseline" "$v73_base" > "$out/RESULT.txt"
