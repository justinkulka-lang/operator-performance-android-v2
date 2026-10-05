# Program authority record

v7.2 is the controlling program. v7.1 remains available only so an in-progress v7.1 week can finish.

- v7.2 runtime source: `app/src/main/java/com/jk/operatorperformance/program/ProgramV72.kt`
- Approved file: `docs/ARES_Performance_Unified_Master_Authority_v7.2_APPROVED.docx`
- v7.2 DOCX SHA-256: `25a7ce497569913c4a56c057bcaa139290adddf20876a2d736790d5fefd436f3`
- That digest was recomputed from the file above. `DOCX_SHA256_VERIFIED` is true.
- v7.1 historical file: `docs/ARES_Performance_Unified_Master_Authority_v7.1_APPROVED.docx`
- v7.1 SHA-256: `1d33dbca214b34dd456a971d32bc1c19d59e3a13069393429fc9e2115e3a036c`
- Topology: 32 weeks × 7 days = 224 days. There is no Week 33.

## Governance constraints

The app displays the fixed approved prescription. It does not select speed or engine variants, trigger deloads, infer missing prescriptions, inherit browsing choices, or automatically recompose the program.

A new install starts at Week 1 Day 1 under v7.2. An initialized v7.1 week finishes under v7.1, including a Day 1 pointer with no active workout, unless session status positively shows that Day 1 has not started. The following Day 1 is v7.2.

The app may persist the current week/day pointer, the active authority, completion marks, substitutions, timers, and local utility settings. It must not collect or persist bodyweight, calories, readiness, sleep, heart rate or HRV, pace or test results, load, reps, RIR, photos, Health Connect data, analytics, cloud-sync data, or automatic program adjustments.

Audit notes for this candidate are in `docs/audit/`.
