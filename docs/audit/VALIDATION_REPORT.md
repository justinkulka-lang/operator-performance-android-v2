# Validation

Local checks on this candidate. This is not a production-readiness claim.

| Gate | Result |
| --- | --- |
| `:app:testDebugUnitTest` | Pass. 27 tests, 0 failures, 0 errors, 0 skipped. |
| `:app:assembleDebug` | Pass. |
| `:app:lintDebug` | Pass. 0 errors. 26 pre-existing-style warnings (dependency, icon, shared-prefs `apply`, target API). None were suppressed to force a pass. |
| Instrumented UI | Pass on emulator `ares-dev-api35` (API 35, software rendering). `V71LaunchUiTest.explicitStartGateIsShown` passed in 56.9 s. |
| Emulator walkthrough | Fresh install shows ARES v7.2 and START WEEK 1 • DAY 1. After start, Today is v7.2 Week 1 Day 1, Engine baseline, with START WORKOUT still visible after the session list scrolls. |
| Galaxy S26 Ultra | Pending. |

Debug APK:

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Copy: `/opt/cursor/artifacts/ares-v72-debug.apk`
- Package: `com.jk.operatorperformance`
- versionCode / versionName: 47 / 0.38.0
- SHA-256: `48f0f394a49ef6e8845e81775973d227ad0ea9edf90c32350cf3d1240011194c`
- `BUILD_COMMIT`: `LOCAL` because `GITHUB_SHA` was not set for this build.

The APK was built from the v7.2 source tree before the audit-doc commit. Workflow-only edits do not change the APK bytes. Re-run assemble on CI to embed the commit SHA.

Base CI runs `36364996205` and `36364996241` failed on the starting commit because `build-tools;37.1.0` does not exist. Those workflows now request API 36. Their historical failures are not results for this candidate.
