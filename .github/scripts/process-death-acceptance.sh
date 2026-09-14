#!/usr/bin/env bash
set -euo pipefail

package="com.jk.operatorperformance"
activity="$package/.MainActivity"
apk="app/build/outputs/apk/debug/app-debug.apk"
emulator_log="$RUNNER_TEMP/ares-v57-process-death-emulator.log"
avd_dir="$HOME/.android/avd/ares-v57-process-death-api35.avd"

cleanup() {
  if [[ -n "${emulator_pid:-}" ]]; then
    kill "$emulator_pid" 2>/dev/null || true
  fi
}
trap cleanup EXIT

test -e /dev/kvm
sudo chmod 666 /dev/kvm
echo no | avdmanager create avd \
  --force \
  --name ares-v57-process-death-api35 \
  --package "system-images;android-35;default;x86_64"
printf '\ndisk.dataPartition.size=2048M\n' >> "$avd_dir/config.ini"
"$ANDROID_HOME/emulator/emulator" \
  -avd ares-v57-process-death-api35 \
  -partition-size 2048 \
  -no-window -no-audio -no-boot-anim -no-snapshot-save \
  -gpu swiftshader_indirect -accel on > "$emulator_log" 2>&1 &
emulator_pid=$!

timeout 90 adb wait-for-device
timeout 240 bash -c 'until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d "\r")" == "1" ]]; do sleep 2; done'
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell input keyevent KEYCODE_WAKEUP || true
adb shell wm dismiss-keyguard || true

test -f "$apk"
adb install "$apk"
adb shell pm clear "$package" >/dev/null

timer_end_ms=$(( $(date +%s%3N) + 300000 ))
state_file="$RUNNER_TEMP/operator_v54_reference_state.xml"
cat > "$state_file" <<EOF
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <int name="currentWeek" value="1" />
    <int name="currentDay" value="1" />
    <boolean name="manualDeloadMode" value="false" />
    <boolean name="machineInventoryConfigured" value="false" />
    <string name="aresSpeedVariant">ACCELERATION</string>
    <string name="aresEngineVariant">THRESHOLD</string>
    <boolean name="workoutActive" value="true" />
    <int name="workoutSchemaVersion" value="3" />
    <int name="workoutWeek" value="1" />
    <int name="workoutDay" value="1" />
    <boolean name="workoutDeloadMode" value="false" />
    <boolean name="workoutMachineInventoryConfigured" value="false" />
    <string name="workoutAresSpeedVariant">ACCELERATION</string>
    <string name="workoutAresEngineVariant">THRESHOLD</string>
    <int name="workoutMovementIndex" value="0" />
    <int name="workoutEffortIndex" value="2" />
    <boolean name="workoutComplete" value="false" />
    <set name="workoutCompletedEffortIds">
        <string>d1_squat#1</string>
    </set>
    <long name="workoutRestTimerEndEpochMillis" value="$timer_end_ms" />
    <boolean name="workoutHasUndo" value="true" />
    <int name="workoutUndoMovementIndex" value="0" />
    <int name="workoutUndoEffortIndex" value="1" />
    <boolean name="workoutUndoComplete" value="false" />
    <set name="workoutUndoCompletedEffortIds" />
    <boolean name="migrationV54Complete" value="true" />
</map>
EOF

remote_seed="/data/local/tmp/operator_v54_reference_state.xml"
remote_prefs="/data/user/0/$package/shared_prefs/operator_v54_reference_state.xml"
adb push "$state_file" "$remote_seed" >/dev/null
adb shell chmod 644 "$remote_seed"
adb shell run-as "$package" mkdir -p "/data/user/0/$package/shared_prefs"
adb shell run-as "$package" cp "$remote_seed" "$remote_prefs"
adb shell run-as "$package" cat "$remote_prefs" | grep -q 'name="workoutEffortIndex" value="2"'

wait_for_activity() {
  for _ in $(seq 1 20); do
    if adb shell dumpsys activity activities 2>/dev/null | grep -q "$package/.MainActivity"; then
      return 0
    fi
    sleep 0.5
  done
  echo "ERROR: MainActivity did not become active." >&2
  return 1
}

dump_ui() {
  local destination="$1"
  local remote_dump="/sdcard/window.xml"
  rm -f "$destination"
  for attempt in $(seq 1 15); do
    adb shell rm -f "$remote_dump" >/dev/null 2>&1 || true
    adb shell input keyevent KEYCODE_WAKEUP >/dev/null 2>&1 || true
    adb shell wm dismiss-keyguard >/dev/null 2>&1 || true
    if adb shell uiautomator dump "$remote_dump" >/dev/null 2>&1 && \
       adb shell test -s "$remote_dump" && \
       adb pull "$remote_dump" "$destination" >/dev/null 2>&1 && \
       grep -q '<hierarchy' "$destination"; then
      return 0
    fi
    echo "UI dump not ready (attempt $attempt/15); retrying..." >&2
    sleep 1
  done
  echo "ERROR: UI hierarchy never became available." >&2
  adb shell dumpsys window windows >&2 || true
  adb shell dumpsys activity activities >&2 || true
  tail -n 120 "$emulator_log" >&2 || true
  return 1
}

find_text_and_tap() {
  local wanted="$1"
  local dump="$RUNNER_TEMP/window.xml"
  local coords=""
  for attempt in $(seq 1 16); do
    dump_ui "$dump"
    coords="$(python3 - "$dump" "$wanted" <<'PY'
import re, sys, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
wanted = sys.argv[2]
for node in root.iter('node'):
    text = node.attrib.get('text', '')
    description = node.attrib.get('content-desc', '')
    if wanted in text or wanted in description:
        match = re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', node.attrib.get('bounds', ''))
        if match:
            x1, y1, x2, y2 = map(int, match.groups())
            print(f'{(x1+x2)//2} {(y1+y2)//2}')
            raise SystemExit(0)
raise SystemExit(1)
PY
    )" && break || true
    echo "Waiting for '$wanted' (attempt $attempt/16); scrolling..." >&2
    adb shell input swipe 540 1500 540 450 250
    sleep 0.3
  done
  if [[ -z "$coords" ]]; then
    echo "ERROR: Could not find '$wanted' after scrolling." >&2
    cat "$dump" >&2 || true
    return 1
  fi
  adb shell input tap $coords
}

assert_ui_contains() {
  local pattern="$1"
  local dump="$2"
  if ! grep -q "$pattern" "$dump"; then
    echo "ERROR: UI dump does not contain expected pattern: $pattern" >&2
    cat "$dump" >&2 || true
    return 1
  fi
}

echo "Launching seeded ARES v5.7 focus state"
adb shell am start -W -n "$activity" >/dev/null
wait_for_activity
sleep 1

echo "Killing application process"
adb shell am force-stop "$package"
test -z "$(adb shell pidof "$package" 2>/dev/null | tr -d '\r')"

echo "Relaunching after true process death"
adb shell am start -W -n "$activity" >/dev/null
wait_for_activity
sleep 1

# The restored active-workout state must surface as RESUME, not create a new workout.
find_text_and_tap "RESUME WORKOUT"
sleep 1
dump_ui "$RUNNER_TEMP/resumed.xml"
assert_ui_contains 'text="SET 2 / 4"' "$RUNNER_TEMP/resumed.xml"
assert_ui_contains 'REST' "$RUNNER_TEMP/resumed.xml"

restored_state="$(adb shell run-as "$package" cat "$remote_prefs" | tr -d '\r')"
grep -q 'name="workoutSchemaVersion" value="3"' <<< "$restored_state"
grep -q 'name="workoutMovementIndex" value="0"' <<< "$restored_state"
grep -q 'name="workoutEffortIndex" value="2"' <<< "$restored_state"
grep -q 'd1_squat#1' <<< "$restored_state"
grep -q "name=\"workoutRestTimerEndEpochMillis\" value=\"$timer_end_ms\"" <<< "$restored_state"

echo "True ARES v5.7 process-death focus/completion/timer restoration passed."
