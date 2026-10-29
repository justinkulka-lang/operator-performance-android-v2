# LEGEND

LEGEND is an offline Android reference for the approved program. It keeps the week and day pointer and local session state. It does not keep performance records. `docs/PROGRAM_V7_AUTHORITY.md` says bodyweight, calories, readiness, sleep, heart rate or HRV, pace or test results, load, reps, and RIR are not stored. `app/src/main/AndroidManifest.xml` does not declare `android.permission.INTERNET`.

## Controlling authority

LEGEND v7.4 Continuous controls new installs, resets, and every week at or after the recorded v7.4 activation week. The record of that rule is `docs/PROGRAM_V7_AUTHORITY.md`. Its text is `docs/legend-v74/LEGEND_v7.4_Continuous.md` (SHA-256 `f083d0a9fa694034f996c774e02d706a4d65e6b8ecd9d7f0705fef26e3d9e988`). Clarifications C-1 to C-17 are in `docs/legend-v74/CLARIFICATIONS.md`. Amendment 01, approved by the owner on 10/29/2026, is `docs/legend-v74/AMENDMENT_01.md`. Owner approval of the continuous program is `docs/legend-v74/OWNER_APPROVAL.md`.

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

The debug APK is for testing. `.github/scripts/package-debug-candidate.sh` copies it into `app/build/distribution/`. The workflow uploads that directory as `legend-debug-<commit>-attempt-<n>` and keeps it for 90 days.

`assembleRelease` and `bundleRelease` must fail with `RELEASE_SIGNING_REQUIRED` when the owner signing values are absent. That check runs with those values unset.

On a push to this repository, and on a pull request whose head repository is this repository, the signed release step runs only when all four `LEGEND_RELEASE_*` Actions secrets are set. Fork pull requests do not run it. When the secrets are absent, that step is skipped and the debug APK is still published. When they are present, `.github/scripts/package-signed-release.sh` builds the signed release APK for the owner's phone. The script checks the release signer's certificate SHA-256 against `docs/signing/RELEASE_CERT_SHA256.txt`, checks that the release version matches the debug APK, installs the release APK on an emulator, and requires `Status: ok`. The workflow uploads `legend-v7.4-<sha12>-release.apk` and `SHA256SUMS` as `legend-release-<commit>-attempt-<n>` and keeps that artifact for 90 days. The keystore is not uploaded.

## Verification evidence

The same workflow uploads `legend-evidence-<commit>-attempt-<n>` from `app/build/reports/`, `app/build/test-results/`, `app/build/outputs/androidTest-results/`, and `app/build/outputs/managed_device_android_test_additional_output/`. That upload is kept for 90 days and is retained when the job fails. Notes for the v7.3 candidate are in `docs/legend-v73/`. Notes for the earlier v7.2 candidate are in `docs/archive/v7.2-candidate-376925a/`.

## Merge, tag, and release

`docs/legend-v73/OWNER_APPROVAL.md` records that the 4 October 2026 approval does not authorize merge, tag, or release. This workflow does not merge, tag, or publish a release.
