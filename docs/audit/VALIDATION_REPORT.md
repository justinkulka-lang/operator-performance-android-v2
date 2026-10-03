# Validation

Local checks on this correction. This is not a production-readiness claim, and it is not an independent audit. Galaxy S26 Ultra and reboot are still open. An API 35 force-stop kept RESUME.

| Gate | Result |
| --- | --- |
| `:app:testDebugUnitTest` | Pass. 49 tests, 0 failures, 0 errors, 0 skipped. Includes `V72AuthorityComparisonTest`, `V72AlternativeMapTest` (36 Phase 1 cells and 100 later slots), `AuthorityMigrationTest`, and `WorkoutProgressTest`. |
| `:app:lintDebug` | Pass. 0 errors. 23 warnings: VectorPath 9, GradleDependency 5, UseKtx 4, MonochromeLauncherIcon 2, ApplySharedPref 1, ObsoleteSdkInt 1, OldTargetApi 1. None were suppressed. |
| `:app:assembleDebug` | Rebuilt after the final commit with `GITHUB_SHA` set to that commit. The APK SHA-256 is the post-commit build identity and is not stored in this file, because writing it back would change the commit. |
| Instrumented UI | API 35 emulator `ares-dev-api35`. `V71LaunchUiTest` 1 passed. `V72FontScaleUiTest` 1 passed. `V72ReferenceUiTest`: longest session, Week 32, and search passed; airplane, rotation, Week, Program, Tools timer, and recreate passed; density 560 with a 720 px width kept START or RESUME, the workout rest line, and Tools visible. |
| Galaxy S26 Ultra | Pending. |
| Reboot and process death | Process death: API 35 `am force-stop` after START WORKOUT relaunched Week 1 Day 1 with SESSION PAUSED and RESUME WORKOUT. Reboot was not run. |

Package `com.jk.operatorperformance`, versionCode 47, and versionName 0.38.0 are unchanged. The app stores only the first 12 hex characters of `GITHUB_SHA`. The merged debug manifest has no `INTERNET` permission. `allowBackup` is false. Full backup, cloud backup, and device transfer exclude root, file, database, shared preferences, and external storage.

Tools export still writes `operator_v54_reference_state` plus the legacy training, reference, and benchmark namespaces. It does not write `ares_v71_reference_state` and it does not add load, rep, or result fields.

Previous APK identities are obsolete:

- `48f0f394a49ef6e8845e81775973d227ad0ea9edf90c32350cf3d1240011194c` embedded `BUILD_COMMIT=LOCAL`.
- `2a64957ecf2a4813d5620395cc28caa35422f3e8e77553d190c2eec276a1970e` was built from `1fcd3f3861a1`.
- `3d8c91267a7c456d12bcc8f6c9cfb82d83e57b8fdd8bb79d03b0a7b3f3d5b610` was built from `1d14821a4da3`.

Base CI runs `36364996205` and `36364996241` failed on the starting commit because `build-tools;37.1.0` does not exist. Those workflows now request API 36. Their historical failures are not results for this candidate.
