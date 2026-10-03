# Migration

Prefs remain `ares_v71_reference_state`. Keys are `authorityVersion`, `retainedV71Week`, `v72MigrationApplied`, and `engagedAbsoluteDay`.

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

A device that already applied the earlier Day-1 heuristic cannot be repaired from prefs alone: the cleared workout is gone, and `v72MigrationApplied` will not run the decision again.
