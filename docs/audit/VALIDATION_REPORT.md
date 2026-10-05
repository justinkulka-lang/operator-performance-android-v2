# Validation

Corrected source commit: `376925a6d655134f748f0141677ce814ccbb9c4e`.

That commit is the debug APK source. Later evidence and download commits do not change the application. This build is **not production-ready**. Galaxy S26 Ultra was not attached, so that checklist is **NOT RUN**.

| Gate | Result |
| --- | --- |
| Authority file | `docs/ARES_Performance_Unified_Master_Authority_v7.2_APPROVED.docx` SHA-256 `25a7ce497569913c4a56c057bcaa139290adddf20876a2d736790d5fefd436f3`. |
| `:app:testDebugUnitTest` | Pass. 83 tests, 0 failures, 0 skipped. Command: `GITHUB_SHA=a90405e6784f869ac90818f45e86d5e625abc267 /usr/local/bin/gradle :app:testDebugUnitTest --offline`, run on the corrected main sources before the instrumentation-only commit `376925a`. That commit does not change unit-test or application sources. |
| `:app:lintDebug` | Pass. 0 errors. 14 warnings: GradleDependency 5, UseKtx 4, MonochromeLauncherIcon 2, ApplySharedPref 1, ObsoleteSdkInt 1, OldTargetApi 1. None were suppressed. |
| `:app:assembleDebug` | Pass. `GITHUB_SHA` was set to `376925a6d655134f748f0141677ce814ccbb9c4e`. Embedded `BUILD_COMMIT` is `376925a6d655`. |
| Debug APK | SHA-256 `aca7e6ccb1139175508e67e20aed311f1f5c6cf76f097772917d13f14f3878eb`. Package `com.jk.operatorperformance`, label `LEGEND`, versionName `0.38.0`, versionCode `47`. V2 signature, certificate DN `C=US, O=Android, CN=Android Debug`, certificate SHA-256 `c2c1cef90b518dc623b3acb111d1fdfd557134d40db56570f71571c1701e5df8`. `zipalign -c 4` exit 0. No `INTERNET` permission. |
| Galaxy S26 Ultra | **NOT RUN.** No handset was attached. |
| Release signing | **Not configured.** `OPERATOR_RELEASE_KEYSTORE_PATH`, `OPERATOR_RELEASE_STORE_PASSWORD`, `OPERATOR_RELEASE_KEY_ALIAS`, and `OPERATOR_RELEASE_KEY_PASSWORD` are unset. No release APK was produced. |

## Program parity

Resolved v7.1 and v7.2 sessions for weeks 1–32 and days 1–7: 448 sessions, 2773 rows, 3221 lines. The dump includes session week, day, title, authority, and phase name, god name, week range, status, and purpose, plus every row id, title, prescription, kind, alternatives, note, effort count, effort label, rest seconds, rest-after-last-effort, and optional flag.

| Dump | SHA-256 |
| --- | --- |
| Before presentation edits | `7d0b8e9ee332a84ab1f4e80216e6c1ab0f63e9dcae2cea70334100b1ba540a71` |
| After presentation edits, same fields | `7d0b8e9ee332a84ab1f4e80216e6c1ab0f63e9dcae2cea70334100b1ba540a71` |
| Comparison | Byte-identical. |

A second dump adds phase god name and phase status. Its SHA-256 is `760477e42084f9635535d0ccf9aa1f26624046b675d00451a2468a4a68078167`. Removing those two columns reproduces the first file exactly. `app/src/main/java/com/jk/operatorperformance/program` has no diff against `a90405e`.

## Instrumentation

Device: API 35 emulator `ares-dev-api35` (`emulator-5554`). Command: `GITHUB_SHA=376925a6d655134f748f0141677ce814ccbb9c4e /usr/local/bin/gradle :app:connectedDebugAndroidTest --offline`.

First full run, 11 tests, 3 failures:

| Test | Result |
| --- | --- |
| `V71LaunchUiTest.explicitStartGateIsShown` | Pass, 30.096 s |
| `V72FontScaleUiTest.enlargedFontKeepsTheLongestSessionAndStartReachable` | Pass, 70.753 s. START and RESUME heights were asserted at 56 dp or more. |
| `V72KeyUiTest.keyOpensScrollsAndReturns` | Fail, 45.649 s. `performScrollTo` could not find the Upper B sentence after it left the lazy list. |
| `V72KeyUiTest.keyRemainsReachableAtLargeFontAndLandscape` | Fail, 44.355 s. Same `performScrollTo` miss for the Upper B sentence. |
| `V72ReferenceUiTest.narrowAndEnlargedDisplayKeepsResumeAndRestVisible` | Fail, 99.653 s. `ComposeTimeoutException` after 45 s waiting for `today_screen` at line 143, density 560 and size 720x1280. The log shows repeated activity destroy/create and skipped frames. It does not contain an ANR line. |
| `V72ReferenceUiTest` other three tests | Pass. |
| `V72TimerPersistenceUiTest` three tests | Pass. |

Reruns:

- Key class after the scroll helper: `keyOpensScrollsAndReturns` passed in 93.097 s. The landscape test failed because `tools_screen` was not displayed after RETURN TO TOOLS.
- A later full Key class run logged `finished: keyOpensScrollsAndReturns`, then the landscape test stopped producing frames for about 60 s while the process kept garbage-collecting. That run was stopped. It did not write a result XML.
- Isolated landscape rerun: fail, 123.241 s. `ComposeTimeoutException` after 10 s waiting for `tools_screen`. The activity was destroyed at the end of that wait.
- Isolated narrow-display rerun: **pass, 106.983 s.**

Portrait Key instrumentation now opens the page from Tools, scrolls to the state section and the Upper B sentence, and returns to Tools. Landscape font-scale return is still failing on this emulator because the activity is destroyed during that sequence. Manual landscape capture of Today at font scale 2.0 showed RESUME WORKOUT.

## Upgrade

The installed v7.2 debug certificate matched the corrected APK (`c2c1cef9…`). `adb install -r` of `aca7e6cc…` over the prior compatible debug build did not uninstall or clear data. `ares_v71_reference_state.xml` was byte-identical before and after installation. It held Week 2 Day 1, an active workout, substitution `d1_incline` / `Smith incline`, `workoutTimerEnd`, `utilityRestTimerEndEpochMillis`, and `manualRestTimerSeconds` 90. The relaunch showed SESSION PAUSED and RESUME WORKOUT.

## Limitations

The previous `download/legend-v7.2-debug.apk` blob at `c46fb32` is the stale `f55eff0503b7` artifact, SHA-256 `06ffb55644c51a328fef7eaaeaa45c8e1a5fa039d4b200d24694ad9a8b149a65`. The distribution commit removes that path from the branch tip. The corrected file is `download/legend-v7.2-376925a6d655-debug.apk`.

No production signature, no Play listing, and no Galaxy S26 Ultra pass. Do not merge or release from this report.
