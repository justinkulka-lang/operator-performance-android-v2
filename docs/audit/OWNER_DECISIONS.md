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

Section 4 says: "Alternatives in Phase 1 remain the substitution map for later phases." Section 7.1 is the ranked pattern order. Trap-bar deadlift is not an RDL alternative.

Shown behavior: every later-phase slot copies the Phase 1 alternative list for the same day and key. A later movement with no Phase 1 twin stays inside that pattern:

- Day 3 vertical pull uses Assisted pull-up and Band pulldown, from the Phase 1 Day 5 vertical pull.
- Day 5 cable or chest-supported row uses T-bar row and One-arm row, from the Phase 1 Day 3 cable or DB row. It does not use the chest-supported row list.

Section 7.1 names that are absent from the Phase 1 cell are not added: trap-bar deadlift, hack squat, leg press as a squat alternative, machine shoulder press, supported split squat, and a straight-knee calf on the seated calf. Phase 2 Day 6 "Leg curl" inherits Phase 1 Day 6, which is Slider curl only.

## 6. Weeks 30–31 rep ranges

Section 6.2 lists set counts and RIR categories and does not restate ranges.

Shown behavior: Phase 4 ranges are kept, sets change to the 6.2 table, and RIR becomes 2–3 for compounds or 1–2 for isolation.

## 7. Unstarted versus active week

Section 10 says to preserve an active v7.1 week, to activate v7.2 at the next unstarted Day 1, and to activate v7.2 immediately when the install happens before an initialized week begins. It also says never to duplicate or omit a completed session.

`clearWorkout()` removes the workout and leaves the pointer where it is. Day 1 with no active workout is therefore the same stored shape as a Day 1 that was started or completed and then reset.

Shown behavior: an initialized Day 1 is retained on v7.1 unless a session-status record positively says that week has not started. Uninitialized installs still open at Week 1 Day 1 under v7.2. A legacy day-8 pointer, which means the previous week was completed, activates v7.2 on the next Day 1. Going forward, saving a workout or advancing a day writes `engagedAbsoluteDay`, and reset of the workout does not delete it.

## 8. v7.1 Week 32

There is no Week 33. An active v7.1 Week 32 stays on v7.1 through Day 7 and then stops.

## 9. DOCX digest

`docs/ARES_Performance_Unified_Master_Authority_v7.2_APPROVED.docx` was hashed in this workspace. SHA-256 is `25a7ce497569913c4a56c057bcaa139290adddf20876a2d736790d5fefd436f3`. `DOCX_SHA256_VERIFIED` is true. The bytes were not edited.

## 10. Session status is not a set log

Section 10 says the app stores current week/day, session status, substitutions, timer state, authority activation state, and permitted settings only. It also says the product must not become a workout logger.

Per-set completion is not required to decide START or RESUME. Those actions need only whether a session record exists and whether that record is complete.

Shown behavior: not started, active, paused/resumable, and completed. Choosing an alternative or starting a manual rest timer creates the session record, because substitutions and timer state are authorized. Resetting the workout clears that record and the rest timer. It does not clear `engagedAbsoluteDay`. The stored schema still has unused effort fields so an older in-progress record can be read; every save clears them and does not write loads, reps, RIR, pace, or results.

