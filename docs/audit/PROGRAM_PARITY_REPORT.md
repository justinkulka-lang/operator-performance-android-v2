# Program parity

v7.2 is the runtime source for every week that is not an explicitly retained v7.1 week. v7.1 remains callable and is covered by `ProgramV71Test`.

## Structure

- 32 weeks × 7 days = 224 days. Absolute day is `(week - 1) × 7 + day`.
- Day order is Engine then short upper, Lower A, Upper A, Speed, Upper B then easy aerobic, Lower B, Rest.
- Week 1 Day 1, Week 24 Day 1, and Week 32 Day 1 omit short upper.
- Weeks 8 and 16 use the easy-only engine opening: 5 minutes of easy movement and no strides.
- Every Engine session, including those two weeks, and every Speed session then adds the 8–10 minute cooldown from section 3.2.
- Every later-phase slot inherits the Phase 1 alternative cell for the same day and key, including Behind-body cable curl on the Day 3 curl. Day 3 vertical pull and Day 5 cable row use the same-pattern ranks because those movements have no Phase 1 twin on that day.
- Plyometrics are only on Speed in weeks 3–7, 9–15, 17–23, and 25–29.

## Volume

Normal-phase working sets match section 4:

| Phase | Total | Chest | Back | Quads | Hamstrings | Biceps direct | Triceps direct |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 104 | 11 | 13 | 13 | 12 | 8 | 6 |
| 2 | 100 | 11 | 14 | 14 | 12 | 8 | 6 |
| 3 | 88 | 10 | 12 | 11 | 10 | 8 | 6 |
| 4 | 71 | 8 | 10 | 8 | 8 | 6 | 6 |

Presses are not counted as direct triceps. Pulls are not counted as direct biceps. Romanian deadlift alternatives do not include a trap-bar deadlift.

## Conditioning

Quality sprint volume is the repetition distance only. Build and fly approach metres are excluded. Sessions at exactly 600 m are weeks 12, 18, 19, 22, and 23, and those sessions show the workload warning. Sessions under 600 m do not.

## Pace

Displayed goal-pace text is `400 m 1:39.4; 600 m 2:29.1; 800 m 3:18.8; 1000 m 4:08.6; 1 mile 6:40.0; 1.5 miles 10:00.0`.
`targetSeconds` uses `valid300 × distance / 300 / percentage`. The 46-second table is not used as the athlete's anchor.

## Overlays

- Weeks 8, 16, and 24 halve each movement's sets, round down, keep at least one, and use 3–4 RIR.
- Weeks 30–31 use the section 6.2 set counts. Phase 4 rep ranges are kept. Compounds use 2–3 RIR and isolation uses 1–2 RIR.
- Week 32 lifting rows that state sets and RIR only are shown as `N set(s) @ 3 RIR`. No rep range was added. Day 2 and Day 6 are very easy bike plus mobility. Day 5 has no easy aerobic.
