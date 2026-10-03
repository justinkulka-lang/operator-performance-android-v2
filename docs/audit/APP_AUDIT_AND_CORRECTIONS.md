# App audit and corrections

The previous runtime was v7.1: Lower A on Day 1, Speed on Day 3, Engine on Day 6, a 500 m sprint warning, pace text of 1:40 / 2:30 / 3:20 / 4:10, and a fresh start at Week 2 Day 3.

## What changed

- `AresProgramV72` is the immutable v7.2 prescription. `AresProgramV71` is unchanged so an in-progress week and the existing v7.1 CI greps still resolve.
- `SessionResolver.resolve(week, day, authority)` defaults to v7.2. Search, week browse, program browse, workout mode, and CSV export use that dispatch. A retained v7.1 week is not mixed into other weeks.
- Today keeps START / RESUME in a footer above the navigation bar. Session reference dialogs scroll inside a bounded column.
- Tools notes, the integrity card, and the local export label identify v7.2. The integrity card reports the recomputed DOCX digest.
- Weeks 8 and 16 keep the easy-only opening and now include the Engine cooldown.
- Later-phase Day 3 curls use Behind-body cable curl.
- Migration no longer treats Day 1 without a workout as proof the week never started.
- Package id, versionCode 47, versionName 0.38.0, and release signing are unchanged.
- Two workflows that installed the missing `build-tools;37.1.0` package now install API 36 and build-tools 36.0.0.

## What was not added

No result entry, load or rep log, bodyweight, calories, readiness, sleep, heart-rate, photos, Health Connect, analytics, cloud sync, or automatic program rewrite. No Week 33. No extra training.
