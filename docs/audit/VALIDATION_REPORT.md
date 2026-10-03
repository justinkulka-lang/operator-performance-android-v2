# Validation

Local checks on this correction. This is not a production-readiness claim, and it is not an independent audit.

| Gate | Result |
| --- | --- |
| `:app:testDebugUnitTest` | Pass. 38 tests, 0 failures, 0 errors, 0 skipped. Includes `V72AuthorityComparisonTest` and the corrected migration cases. |
| `:app:lintDebug` | Pass. 0 errors. Warnings are the existing dependency, icon, shared-prefs `apply`, and target-API findings. None were suppressed to force a pass. |
| `:app:assembleDebug` | Rebuilt after this commit with `GITHUB_SHA` set to the commit. The review ZIP `SHA256SUMS` is the APK identity. |
| Instrumented UI | Not re-run for this correction. The earlier `V71LaunchUiTest` result does not cover the cooldown, curl alternative, or migration fix. |
| Emulator walkthrough | The earlier start-gate screenshots do not show Week 8 or the incline-curl alternative. |
| Galaxy S26 Ultra | Pending. Do not start it until this correction is the candidate under review. |

Previous APK identities are obsolete:

- `48f0f394a49ef6e8845e81775973d227ad0ea9edf90c32350cf3d1240011194c` embedded `BUILD_COMMIT=LOCAL`.
- `2a64957ecf2a4813d5620395cc28caa35422f3e8e77553d190c2eec276a1970e` was built from `1fcd3f3861a1` before these corrections.

Package `com.jk.operatorperformance`, versionCode 47, and versionName 0.38.0 are unchanged. The app stores only the first 12 hex characters of `GITHUB_SHA`.

Base CI runs `36364996205` and `36364996241` failed on the starting commit because `build-tools;37.1.0` does not exist. Those workflows now request API 36. Their historical failures are not results for this candidate.

Font scaling and runtime offline behavior remain unverified. The manifest has no `INTERNET` permission; that is not a runtime offline test.
