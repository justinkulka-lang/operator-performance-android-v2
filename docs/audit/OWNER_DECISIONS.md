# Owner decisions

These items were not invented as new training. Each one names the provision, the sessions it affects, and the narrow behavior used so the rest of the correction could proceed.

## 1. No valid 300 m anchor

v7.2 section 5.1 defines percentage speed from a valid external 300 m time and does not keep the v7.1 RPE 8 fallback. The app does not store a result and does not use 46 seconds as current capacity.

Affected Speed days: weeks 7, 9, 12, 14, 18, 19, 21, 22, 23, 26, 27, and 29.

Shown behavior: the formula and a statement that v7.2 gives no fallback.

## 2. Week 32 rep ranges

Section 6.3 gives sets and 3 RIR for several lifts and does not give repetition ranges. Phase 4 ranges were not copied onto those rows.

Affected sessions: Week 32 Day 3 and Day 5.

Shown behavior: `2 sets @ 3 RIR` or `1 set @ 3 RIR`.

## 3. RIR calibration

Section 3.1 allows one replacement set to technical failure every fourth normal week and also says lower-body isolation keeps at least 1 RIR. The phrase "every fourth normal week" is not a modulo-4 calendar.

Shown behavior: the rule is coaching text on normal-week warm-ups. The app never adds a set and never picks the week.

## 4. Fly approaches

Section 5 writes the build or acceleration distance in the same row as the fly. No row asks for a fly without a numerical approach.

Shown behavior: the written distances only. No extra approach and no extra repetitions.

## 5. Substitution precedence

Section 4 says Phase 1 alternatives remain the later-phase map. Section 7.1 is the pattern order. Trap-bar deadlift is not an RDL alternative.

Shown behavior: Phase 1 slots use the Phase 1 table. Later phases use the section 7.1 order, capped at four alternatives, with RDL limited to barbell, Smith, and dumbbell RDL.

## 6. Weeks 30–31 rep ranges

Section 6.2 lists set counts and RIR categories and does not restate ranges.

Shown behavior: Phase 4 ranges are kept, sets change to the 6.2 table, and RIR becomes 2–3 for compounds or 1–2 for isolation.

## 7. Unstarted versus active week

Section 10 says to preserve an active v7.1 week and to activate v7.2 immediately when the initialized week has not begun. Legacy prefs do not have a separate "week started" flag.

Shown behavior: Day 1 with no active workout is treated as unstarted. Any later day, or an active workout, retains v7.1 for that week only. This can misread a pointer that was left on Day 1 after a reset workout.

## 8. v7.1 Week 32

There is no Week 33. An active v7.1 Week 32 stays on v7.1 through Day 7 and then stops.

## 9. DOCX digest

The approved v7.2 file was not available to hash. The expected digest is displayed as unverified.
