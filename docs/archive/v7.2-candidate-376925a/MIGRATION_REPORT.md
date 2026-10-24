# Migration

Prefs remain `ares_v71_reference_state`. Keys are `authorityVersion`, `retainedV71Week`, `v72MigrationApplied`, `v72MigrationRevision`, `v72ActivationProven`, and `engagedAbsoluteDay`.

`engagedAbsoluteDay` is session status only. It stores the absolute day index `(week - 1) × 7 + day` once a workout is saved or a day is advanced. `clearWorkout()` does not remove it. It does not store loads, reps, RIR, or results.

`AuthorityMigration.apply` runs once, before `v72MigrationApplied` is set.

| Stored state | Result |
| --- | --- |
| Not initialized | Week 1 Day 1, v7.2, not started. The old Week 2 Day 3 default is not used. |
| Initialized Day 1, no workout, no session-status record | Keep that week on v7.1. A missing workout is not proof the week never started. |
| Initialized Day 1, no workout, session status shows the day was engaged | Keep that week on v7.1. This is the reset-after-completion case. |
| Initialized Day 1, no workout, session status record shows this week has not started | Stay on that week and day, activate v7.2. |
| Initialized day greater than 1, or an active workout | Keep the pointer. Finish that week under v7.1. |
| Legacy v7.0 day 8, week below 32 | Next week Day 1 under v7.2. Day 8 is a completed week, not a cleared Day 1 workout. |
| Advance from the retained week's Day 7 | Next week Day 1 under v7.2. Retention clears. The advanced day is marked engaged before the workout is cleared. |
| Week 32 Day 7 | Stay there. No Week 33. |

Explicit START is Week 1 Day 1 under v7.2 and clears session status. Explicit pointer reset does the same. Moving the current pointer off the retained week clears retention.

v7.1 never wrote `engagedAbsoluteDay`. The first upgrade therefore treats an initialized Day 1 as unknown and retains v7.1. That can keep a genuinely unstarted Day 1 on v7.1 for the rest of that week. It does not replace a completed v7.1 Day 1 with a different v7.2 Day 1.

`v72MigrationApplied` alone cannot tell the earlier Day 1 heuristic from the corrected decision, because both sets wrote that flag and neither wrote a revision. The first load on this build runs one repair when the flag is set and `v72MigrationRevision` is below 2, then writes revision 2 so the repair does not run again.

The repair does not move the pointer and does not create Week 33.

| Already stored before revision 2 | Repair |
| --- | --- |
| Not initialized | Leave the v7.2 start gate. |
| `v72ActivationProven` | Leave the explicit v7.2 start in place. Older builds did not write this flag. |
| Retained v7.1 week, including an active or completed Week 32 | Leave that week on v7.1. |
| Day 1, no workout, session status proves this week has not started | Keep v7.2 and record the proof. |
| Day 1, no workout, no positive proof | Return that week to v7.1. This is the earlier heuristic, an ambiguous Week 1 Day 1, and a Day 1 that was completed or reset before the pointer advanced. |
| Day 2 or later, or an open workout, already on v7.2 with no retained week | Leave it. The earlier heuristic retained v7.1 whenever the day was past Day 1 or a workout was active, so this shape was produced by later progress under v7.2. |

An explicit START or pointer reset on this build sets `v72ActivationProven`, so a later revision check does not turn that start back into v7.1. A start saved by the previous build has the same stored shape as an ambiguous v7.1 Week 1 Day 1 and is returned to v7.1. Resetting the pointer writes a proven v7.2 start.

`AuthorityMigrationTest` and `ProgramStateStorePersistenceTest` cover a fresh install, an unstarted initialized Day 1, an active Day 1, a completed Day 1 before the pointer advances, a reset Day 1, Day 2 or later, an already-migrated legacy device, and an active v7.1 Week 32. A completed v7.1 week does not become v7.2 on the same Day 1. Advancing from retained Day 7 opens the next Day 1 under v7.2. Week 32 Day 7 does not create Week 33.
