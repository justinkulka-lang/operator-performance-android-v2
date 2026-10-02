# Migration

Prefs remain `ares_v71_reference_state`. New keys are `authorityVersion`, `retainedV71Week`, and `v72MigrationApplied`.

`AuthorityMigration.apply` runs once.

| Stored state | Result |
| --- | --- |
| Not initialized | Week 1 Day 1, v7.2, not started. The old Week 2 Day 3 default is not used. |
| Initialized, Day 1, no active workout | Stay on that week and day, activate v7.2. |
| Initialized, day greater than 1, or an active workout | Keep the pointer, substitutions, and workout. Finish that week under v7.1. |
| Advance from the retained week's Day 7 | Next week Day 1 under v7.2. Retention clears. |
| Week 32 Day 7 | Stay there. No Week 33. |

Explicit START is Week 1 Day 1 under v7.2. Explicit pointer reset also clears retention and uses v7.2. Moving the current pointer off the retained week clears retention so a stale v7.1 week is not pinned forever.

The Day-greater-than-1 rule is a narrow reading. The store cannot tell an untouched product default from a real mid-week pointer when both are initialized on a day after Day 1. See `OWNER_DECISIONS.md`.
