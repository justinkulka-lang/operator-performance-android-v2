# LEGEND v7.4 clarifications

## C-1 (binding; it corrects a defect in §9 Tests)

The §9 blocklist entry "anchor" is too broad, because the approved Home alternates on slots 4, 26 and 37 say "anchored to rack" and "anchored high on the rack". Use "300 m anchor" and "anchor formula" in the blocklist instead of "anchor". Every other blocklist term stays as written.

## C-2

C-2. Under V74, "set as current" from Week or Program browsing changes only the day within the current week. Block, block week, loop and absolute week change only by Complete Day / Next Day, by Consolidate-now, or by the explicit reset.

## C-3

C-3. Consolidate-now is available only when all of these hold: the current week's authority is V74; currentDay is 1; no workout is active; session-status evidence for the current week is NOT_STARTED (UNKNOWN does not qualify); and the current block week is not 8.

## C-4

C-4. The loop number is stored. It starts at 1 and increments when block IV week 8 advances to block I week 1. This equals the §9 definition (completed Block IV consolidation weeks plus one).

## C-5

C-5. Mapping absolute week n to V74 position (used only for a fresh start, the migration activation week, and entering V74 from a retained week): block = ((n − 1) div 8) mod 4 + 1; blockWeek = (n − 1) mod 8 + 1; loop = (n − 1) div 32 + 1. This matches §9 for n ≤ 32 and extends it, so a retained v7.3 Week 32 is followed by Week 33 = Loop 2 · Block I · Week 1.

## C-6

C-6. Every rule keyed to a week number — the Week 32 overlay, weeks 30–31 realization, weeks 8/16/24 recovery text, timed tests, the 600 m warning, phase lookups by week — applies only to V71, V72 and V73 sessions. No V74 session may reach any v7.1–v7.3 week-bounded function, including the default value of SessionReference.phase, which calls ProgramV71.blockForWeek.

## C-7

C-7. The owner reinstalls the app for every update, which erases saved state, so a fresh install must be able to start at a chosen position.

1. The start gate offers two actions:
   - "START WEEK 1 • DAY 1" (existing behavior, now V74)
   - "START AT A POSITION", which opens a picker for Block (I–IV), Block week (1–8) and Day (1–7), followed by a confirmation dialog that repeats the chosen position.
2. Starting at a position creates a V74 state: v74Block, v74BlockWeek and currentDay as chosen; v74Loop = 1; currentWeek (absolute) = (block − 1) × 8 + blockWeek; v74ActivationWeek = 1; empty history; no engagement record; no workout.
3. The Tools reset control offers the same two actions, with the same confirmation. This is the only other place block and week may be set directly; C-2 still governs browsing.
4. Tests:
   - Each picker choice persists across a store reload, with the correct absolute week (for example Block III Week 4 Day 2 → Week 20, Loop 1).
   - Advancing from a picked position follows the normal rule.
   - Block IV Week 8 Day 7 → Week 33 = Loop 2 · I · 1.
   - [instrumented] A fresh install can start at a position and Today shows the matching header.
5. This addition does not change any other 2b rule. Report it under its own heading.
