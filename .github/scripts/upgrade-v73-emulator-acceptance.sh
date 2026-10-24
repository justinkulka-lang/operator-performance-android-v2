#!/usr/bin/env bash
set -euo pipefail
source_sha="${1:?candidate SHA required}"
test "$(git rev-parse HEAD)" = "$source_sha"
v73_base=7cd35a5a8a47d52acb8cc84b472067604c269d69
sdk_root="${ANDROID_HOME:?SDK required}"
adb="$sdk_root/platform-tools/adb"
serial=emulator-5582
out="$PWD/app/build/reports/upgrade-v73-persistence"
candidate="$PWD/app/build/outputs/apk/debug/app-debug.apk"
mkdir -p "$out"
exec > >(tee -a "$out/step.log") 2>&1

v73_dir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/legend-v73-upgrade-source.XXXXXX")"
git worktree add --detach "$v73_dir" "$v73_base"
GITHUB_SHA="$v73_base" gradle -p "$v73_dir" :app:assembleDebug --stacktrace
v73_apk="$v73_dir/app/build/outputs/apk/debug/app-debug.apk"
"$sdk_root/build-tools/36.0.0/apksigner" verify --print-certs "$v73_apk" > "$out/v73-before-certificate.txt"
"$sdk_root/build-tools/36.0.0/apksigner" verify --print-certs "$candidate" > "$out/after-certificate.txt"
cmp "$out/v73-before-certificate.txt" "$out/after-certificate.txt"

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

sdkmanager --install 'system-images;android-35;google_apis;x86_64'
avd_name="legend_v73_upgrade_${GITHUB_RUN_ID:-local}"
printf 'no\n' | avdmanager create avd -n "$avd_name" -k 'system-images;android-35;google_apis;x86_64' -d pixel_2
"$sdk_root/emulator/emulator" -avd "$avd_name" -port 5582 -no-window -no-audio -no-snapshot -no-boot-anim -gpu swiftshader -memory 2048 -cores 2 -partition-size 2048 > "$out/emulator.log" 2>&1 &
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
user_is_unlocked() {
  if "$adb" -s "$serial" shell dumpsys activity | tr -d '\r' | grep -q 'User #0: state=RUNNING_UNLOCKED'; then
    return 0
  fi
  "$adb" -s "$serial" logcat -d -b main -b system | tr -d '\r' | grep -q 'android.intent.action.USER_UNLOCKED'
}
launch_when_unlocked() {
  local dest="$1"
  : > "$dest"
  local attempt
  for attempt in {1..30}; do
    if user_is_unlocked \
        && "$adb" -s "$serial" shell am start -W -n com.jk.operatorperformance/.MainActivity > "$dest" 2>&1 \
        && grep -F 'Status: ok' "$dest"; then
      return 0
    fi
    "$adb" -s "$serial" shell input keyevent 82 >/dev/null 2>&1 || true
    sleep 2
  done
  echo 'Launch failed before the activity resolved' >&2
  if [[ -s "$dest" ]]; then cat "$dest" >&2; else echo 'am start produced no output' >&2; fi
  "$adb" -s "$serial" shell dumpsys activity | tr -d '\r' | grep -F 'User #0:' >&2 || true
  return 1
}
snapshot() {
  "$adb" -s "$serial" exec-out run-as com.jk.operatorperformance cat shared_prefs/ares_v71_reference_state.xml > "$out/$1.xml"
}

wait_boot
"$adb" -s "$serial" install "$v73_apk"
"$adb" -s "$serial" shell am force-stop com.jk.operatorperformance
"$adb" -s "$serial" shell pm clear com.jk.operatorperformance
launch_when_unlocked "$out/v73-first-launch.txt"
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
cmp "$out/v73-seed.xml" "$out/v73-before-upgrade.xml"
"$adb" -s "$serial" install -r "$candidate"
snapshot v73-after-install
cmp "$out/v73-before-upgrade.xml" "$out/v73-after-install.xml"
"$adb" -s "$serial" shell am force-stop com.jk.operatorperformance
launch_when_unlocked "$out/v73-relaunch.txt"
snapshot v73-after-launch
python3 .github/scripts/verify-v73-upgrade-state.py "$out/v73-before-upgrade.xml" "$out/v73-after-launch.xml" from-v73
printf 'sourceCommit=%s\nv73BaselineCommit=%s\nv73MidProgramUpgrade=PASS\nphysicalDevice=NOT_RUN\nphoneCertificateCompatibility=NOT_PROVEN\n' "$source_sha" "$v73_base" > "$out/RESULT.txt"
