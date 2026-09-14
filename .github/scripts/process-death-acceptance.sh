#!/usr/bin/env bash
set -euo pipefail

package="com.jk.operatorperformance"
activity="$package/.MainActivity"
apk="app/build/outputs/apk/debug/app-debug.apk"
emulator_log="$RUNNER_TEMP/operator-v56-process-death-emulator.log"
avd_dir="$HOME/.android/avd/operator-v56-process-death-api35.avd"

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
  --name operator-v56-process-death-api35 \
  --package "system-images;android-35;default;x86_64" \
  --device "pixel_2"
printf '\ndisk.dataPartition.size=2048M\n' >> "$avd_dir/config.ini"
"$ANDROID_HOME/emulator/emulator" \
  -avd operator-v56-process-death-api35 \
  -partition-size 2048 \
  -no-window -no-audio -no-boot-anim -no-snapshot-save \
  -gpu swiftshader_indirect -accel on > "$emulator_log" 2>&1 &
emulator_pid=$!

timeout 90 adb wait-for-device
timeout 240 bash -c 'until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d "\r")" == "1" ]]; do sleep 2; done'
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

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
    <boolean name="workoutActive" value="true" />
    <int name="workoutSchemaVersion" value="2" />
    <int name="workoutWeek" value="1" />
    <int name="workoutDay" value="1" />
    <boolean name="workoutDeloadMode" value="false" />
    <boolean name="workoutMachineInventoryConfigured" value="false" />
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

echo "Launching seeded v5.6 focus state"
adb shell am start -W -n "$activity" >/dev/null
sleep 1

echo "Killing application process"
adb shell am force-stop "$package"
test -z "$(adb shell pidof "$package" 2>/dev/null | tr -d '\r')"

echo "Relaunching after true process death"
adb shell am start -W -n "$activity" >/dev/null
sleep 1

find_text_and_tap() {
  local wanted="$1"
  local dump="$RUNNER_TEMP/window.xml"
  local coords=""
  for _ in $(seq 1 12); do
    adb shell uiautomator dump /sdcard/window.xml >/dev/null
    adb pull /sdcard/window.xml "$dump" >/dev/null
    coords="$(python3 - "$dump" "$wanted" <<'PY'
import re, sys, xml.etree.ElementTree as ET
root = ET.parse(sys.argv[1]).getroot()
wanted = sys.argv[2]
for node in root.iter('node'):
    if node.attrib.get('text') == wanted:
        match = re.fullmatch(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', node.attrib.get('bounds', ''))
        if match:
            x1, y1, x2, y2 = map(int, match.groups())
            print(f'{(x1+x2)//2} {(y1+y2)//2}')
            raise SystemExit(0)
raise SystemExit(1)
PY
    )" && break || true
    adb shell input swipe 540 1500 540 500 250
    sleep 0.3
  done
  test -n "$coords"
  adb shell input tap $coords
}

find_text_and_tap "RESUME WORKOUT"
sleep 1
adb shell uiautomator dump /sdcard/window.xml >/dev/null
adb pull /sdcard/window.xml "$RUNNER_TEMP/resumed.xml" >/dev/null
grep -q 'text="SET 2 OF 4"' "$RUNNER_TEMP/resumed.xml"
grep -q 'text="REST"' "$RUNNER_TEMP/resumed.xml"

restored_state="$(adb shell run-as "$package" cat "$remote_prefs" | tr -d '\r')"
grep -q 'name="workoutMovementIndex" value="0"' <<< "$restored_state"
grep -q 'name="workoutEffortIndex" value="2"' <<< "$restored_state"
grep -q 'd1_squat#1' <<< "$restored_state"
grep -q "name=\"workoutRestTimerEndEpochMillis\" value=\"$timer_end_ms\"" <<< "$restored_state"

echo "True v5.6 process-death focus/completion/timer restoration passed."
