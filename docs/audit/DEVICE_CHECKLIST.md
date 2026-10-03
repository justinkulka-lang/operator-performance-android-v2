# Galaxy S26 Ultra checklist

An API 35 software emulator ran the checks below. Physical Galaxy S26 Ultra checks are still pending. Reboot was not run. Do not treat the debug APK as production-ready.

Emulator results on this candidate:

- Font scale 1.5 reached START and the last line of the longest v7.2 session.
- Display density 560 and a 720×1280 px viewport, in night mode, kept START or RESUME visible, showed the workout rest countdown, and opened Tools. The START control was at least 48 dp.
- Airplane mode, night mode, and landscape rotation covered Today, the workout rest timer, Week, Program, the Tools timer, and a configuration recreate that still showed RESUME.
- Week 32 Day 7 shows the rest prescription and does not offer Week 33.
- Search and the longest-session dialog were exercised with the network available. Search reads the local program only.
- `am force-stop` after START WORKOUT relaunched the same Week 1 Day 1 with SESSION PAUSED and RESUME WORKOUT. Reboot was not run.

These emulator results do not close the physical checklist.

Install the debug APK over a copy of the current v7.1 build only on a test profile. Confirm:

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
11. Tools export does not ask for loads, reps, pace, or bodyweight. Airplane mode still opens every session. This last item is a device check; a manifest review is not a substitute.
