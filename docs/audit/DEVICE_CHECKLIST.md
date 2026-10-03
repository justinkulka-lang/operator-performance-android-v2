# Device checklist

This file records what was run. It does not claim production readiness. No Galaxy S26 Ultra was attached, so every physical row below is **not run**. An API 35 software emulator (`ares-dev-api35`, `emulator-5554`) ran the emulator column.

| Check | Emulator | Galaxy S26 Ultra |
| --- | --- | --- |
| Fresh install shows START WEEK 1 • DAY 1 | Pass. `V71LaunchUiTest` opened the start gate. | Not run |
| Migration from an installed v7.1 profile | Not run as an on-device upgrade. Store tests cover the decision. | Not run |
| All seven day types | Not walked as seven separate screens. Local search resolves all 224 sessions. UI opened Week 1 Day 1, the longest session, Week 32 Day 2, and Week 32 Day 7. | Not run |
| Longest session | Pass. Font scale 1.5 reached the last line. | Not run |
| Week 32 does not expose Week 33 | Pass. Week 32 Day 7 shows the rest line and no next week. | Not run |
| Font scale 1.5 | Pass. `V72FontScaleUiTest`. | Not run |
| Landscape | Pass. `rotationAirplaneResumeAndNavigation` rotated during a session and still showed RESUME. | Not run |
| Dark mode | Pass. Night mode stayed on during the reference tests. The app uses the fixed dark theme. | Not run |
| Density 560 | Pass. 720×1280 at density 560 kept START or RESUME, the workout rest countdown, and Tools. | Not run |
| Airplane mode | Pass. Search for Behind-body cable curl and Week 32 Day 2 opened local rows with airplane mode on. | Not run |
| Process death | Pass. After `am force-stop`, Week 1 Day 1 showed SESSION PAUSED and RESUME WORKOUT. Prefs were byte-identical. | Not run |
| Reboot | Pass for stored state. `adb reboot` returned `sys.boot_completed=1`. `ares_v71_reference_state.xml` was byte-identical, including both timer ends, the 60 s choice, Week 1 Day 1, and `engagedAbsoluteDay` 1. A later launch showed SESSION PAUSED and RESUME WORKOUT. The first post-boot launch hit a System UI ANR on this emulator; the stored file was already identical before that launch. | Not run |
| Search | Pass. Airplane-mode UI search plus `ProgramSearchOfflineTest` for all 224 local sessions. | Not run |
| Export | Pass in unit tests. `LegacyExportTest` shows the export omits `ares_v71_reference_state` and does not invent loads, reps, RIR, pace, bodyweight, or calories. A Tools save-to-file was not captured on a device. | Not run |
| START or RESUME stays visible while the session scrolls | Pass. The Today footer stayed displayed after the session list was swiped. | Not run |
| No per-set completion, effort marks, or result fields | Pass. The workout screen had no `set_done`, `load_field`, `rep_field`, `result_field`, or LOG SET control. | Not run |

Physical checks still required on a Galaxy S26 Ultra test profile:

1. Fresh install shows START WEEK 1 • DAY 1 and opens Week 1 Day 1 Engine baseline with no short upper.
2. An initialized v7.1 pointer still shows the v7.1 session and the "FINISHING THIS WEEK UNDER v7.1" line. Check Week 2 Day 3, and also Week 5 Day 1 after the workout has been reset. The next Day 1 after that retained week is v7.2.
3. Week 8 Day 1 shows the easy-run opening, the 25-minute easy run, and an 8–10 minute cooldown. The cooldown is not described as omitted.
4. TODAY, WEEK, PROGRAM, and TOOLS all open. Browsing another week does not change the current pointer and does not copy the retained week's authority.
5. START and RESUME stay visible without scrolling the session. Large font and display size still show the full session. The day dialog scrolls to the last coaching line.
6. Dark theme remains readable.
7. Rotate the device during a rest timer, kill the process, and reboot. The timer end and the session pointer return.
8. Week 12 Speed shows the 600 m workload warning. Week 14 Speed does not.
9. Week 9 Day 3 Incline curl offers Behind-body cable curl, not a generic band or cable curl.
10. Week 32 Day 2 and Day 6 are bike and mobility. Day 5 has no easy aerobic. Day 7 says the program is complete and does not offer Day 1 of Week 33.
11. Tools export does not ask for loads, reps, pace, or bodyweight. Airplane mode still opens every session.
