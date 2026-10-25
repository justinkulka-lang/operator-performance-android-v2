# LEGEND

LEGEND is an offline Android reference for the approved program. It keeps the week and day pointer and local session state. It does not keep performance records. `docs/PROGRAM_V7_AUTHORITY.md` says bodyweight, calories, readiness, sleep, heart rate or HRV, pace or test results, load, reps, and RIR are not stored. `app/src/main/AndroidManifest.xml` does not declare `android.permission.INTERNET`.

## Controlling authority

LEGEND v7.4 Continuous controls new installs, resets, and every week at or after the recorded v7.4 activation week. The record of that rule is `docs/PROGRAM_V7_AUTHORITY.md`. Its text is `docs/legend-v74/LEGEND_v7.4_Continuous.md` (SHA-256 `f083d0a9fa694034f996c774e02d706a4d65e6b8ecd9d7f0705fef26e3d9e988`). Clarifications C-1 to C-11 are in `docs/legend-v74/CLARIFICATIONS.md`. Owner approval is `docs/legend-v74/OWNER_APPROVAL.md`.

## Frozen authorities and the CI freeze

v7.3, v7.2, and v7.1 are frozen and resolve retained historical weeks only. v7.3 remains `docs/legend-v73/ARES_Performance_Unified_Master_Authority_v7.3_PROPOSED.md`, approved in `docs/legend-v73/OWNER_APPROVAL.md`, with issued hashes in `docs/legend-v73/SHA256SUMS.txt`. v7.2 resolves retained historical weeks from `docs/ARES_Performance_Unified_Master_Authority_v7.2_APPROVED.docx`. v7.1 remains the legacy file `docs/ARES_Performance_Unified_Master_Authority_v7.1_APPROVED.docx`.

`.github/workflows/android-ci.yml` checks the v7.2 DOCX digest and checks `docs/legend-v73/SHA256SUMS.txt` on every run. The CI freeze is two `git diff --exit-code` checks. The v7.2 files must match commit `28944300` (`28944300f1e73015c70b8e342bcc90173022cd25`):

- `app/src/main/java/com/jk/operatorperformance/program/ProgramV56.kt`
- `app/src/main/java/com/jk/operatorperformance/program/ProgramV70.kt`
- `app/src/main/java/com/jk/operatorperformance/program/ProgramV71.kt`
- `app/src/main/java/com/jk/operatorperformance/program/ProgramV72.kt`
- `app/src/main/java/com/jk/operatorperformance/program/SessionResolverV72.kt`
- `app/src/main/java/com/jk/operatorperformance/program/ProgramNotesV71.kt`
- `app/src/main/java/com/jk/operatorperformance/program/ProgramNotesV72.kt`
- `app/src/test/resources/program-baseline.tsv`

The v7.3 sources must match commit `7cd35a5` (`7cd35a5a8a47d52acb8cc84b472067604c269d69`):

- `app/src/main/java/com/jk/operatorperformance/program/ProgramV73.kt`
- `app/src/main/java/com/jk/operatorperformance/program/SessionResolverV73.kt`
- `app/src/main/java/com/jk/operatorperformance/program/ProgramNotesV73.kt`

## How CI builds and where the APK is published

The workflow `LEGEND versioned candidate` runs on push to `main`, `construction/**`, `cursor/**`, and `codex/**`, on pull requests, and on `workflow_dispatch`. The job uses Java 17, Gradle 9.6.0, compile SDK 36, and build-tools 36.0.0. It runs `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`, and `:app:assembleDebugAndroidTest`, then the Pixel 2 API 35 emulator suite, five passes of `V72KeyUiTest`, and `.github/scripts/upgrade-emulator-acceptance.sh`.

`assembleRelease` and `bundleRelease` must fail with `RELEASE_SIGNING_REQUIRED` when the owner signing values are absent.

`.github/scripts/package-debug-candidate.sh` copies the debug APK into `app/build/distribution/`. The workflow uploads that directory as `legend-debug-<commit>-attempt-<n>` and keeps it for 90 days.

## Verification evidence

The same workflow uploads `legend-evidence-<commit>-attempt-<n>` from `app/build/reports/`, `app/build/test-results/`, `app/build/outputs/androidTest-results/`, and `app/build/outputs/managed_device_android_test_additional_output/`. That upload is kept for 90 days and is retained when the job fails. Notes for the v7.3 candidate are in `docs/legend-v73/`. Notes for the earlier v7.2 candidate are in `docs/archive/v7.2-candidate-376925a/`.

## Merge, tag, and release

`docs/legend-v73/OWNER_APPROVAL.md` records that the 4 October 2026 approval does not authorize merge, tag, or release. This workflow does not merge, tag, or publish a release.
