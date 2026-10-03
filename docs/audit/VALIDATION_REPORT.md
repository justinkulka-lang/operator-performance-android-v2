# Validation

Local checks on this correction. This build is **not production-ready**. Galaxy S26 Ultra was not attached, so the physical checklist is not run. This is not an independent audit.

| Gate | Result |
| --- | --- |
| `:app:testDebugUnitTest` | Pass. 75 tests, 0 failures, 0 skipped. Includes migration, timer persistence, offline search, and export tests. |
| `:app:lintDebug` | Pass. 0 errors. 23 warnings: VectorPath 9, GradleDependency 5, UseKtx 4, MonochromeLauncherIcon 2, ApplySharedPref 1, ObsoleteSdkInt 1, OldTargetApi 1. None were suppressed. |
| `:app:assembleDebug` | Rebuilt after the final commit with `GITHUB_SHA` set to that commit. The APK SHA-256 is the post-commit build identity and is not stored in this file, because writing it back would change the commit. |
| Instrumented UI | API 35 emulator `ares-dev-api35`. Launch gate passed. Font scale 1.5 passed. Reference tests passed for the longest session, Week 32, density 560, landscape, night mode, and airplane-mode search. `V72TimerPersistenceUiTest` recreated the activity with both timers still running. |
| Galaxy S26 Ultra | Not run. |
| Process death | Pass. Seeded Week 1 Day 1, a 180 s workout rest, and a 60 s utility timer. `am force-stop` left the preference file byte-identical. The verify test and the relaunch showed SESSION PAUSED and RESUME WORKOUT. |
| Reboot | Pass for stored state on the emulator. `adb reboot` reached `sys.boot_completed=1`. The preference file was byte-identical after boot, before the app was relaunched. A later launch showed SESSION PAUSED and RESUME WORKOUT at Week 1 Day 1. The first post-boot UI attempt was blocked by a System UI ANR. |

Package `com.jk.operatorperformance`, versionCode 47, and versionName 0.38.0 are unchanged. The app stores only the first 12 hex characters of `GITHUB_SHA`. The merged debug manifest has no `INTERNET` permission. `allowBackup` is false. Full backup, cloud backup, and device transfer exclude root, file, database, shared preferences, and external storage.

Tools export reads `operator_v54_reference_state` plus the legacy training, reference, and benchmark namespaces. It does not read `ares_v71_reference_state`. It does not add loads, reps, RIR, pace results, bodyweight, or calories. `operator_v54_reference_state` remains because it holds the historical pointer migration and the plate calculator. The three legacy namespaces remain so retained v5.7 tracking can be copied before the user deletes it.

Manual timer persistence stores `manualRestTimerSeconds`, `utilityRestTimerEndEpochMillis`, and `workoutTimerEnd` with `commit = true`. `clearWorkout()` removes the workout timer and does not remove `engagedAbsoluteDay`. Marking a session complete clears the workout timer and does not write per-set completion. The allowed durable fields are the pointer, session status, substitutions, manual timer state, authority activation, and permitted settings.

Previous APK identities are obsolete:

- `48f0f394a49ef6e8845e81775973d227ad0ea9edf90c32350cf3d1240011194c` embedded `BUILD_COMMIT=LOCAL`.
- `2a64957ecf2a4813d5620395cc28caa35422f3e8e77553d190c2eec276a1970e` was built from `1fcd3f3861a1`.
- `3d8c91267a7c456d12bcc8f6c9cfb82d83e57b8fdd8bb79d03b0a7b3f3d5b610` was built from `1d14821a4da3`.

Base CI runs `36364996205` and `36364996241` failed on the starting commit because `build-tools;37.1.0` does not exist. Those workflows now request API 36. Their historical failures are not results for this candidate.
