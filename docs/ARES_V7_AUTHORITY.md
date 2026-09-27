# ARES Performance v7.1 Authority Record

- Approved source: `ARES_Performance_Unified_Master_Authority_v7.1_APPROVED.docx`
- SHA-256: `1d33dbca214b34dd456a971d32bc1c19d59e3a13069393429fc9e2115e3a036c`
- Runtime source: `app/src/main/java/com/jk/operatorperformance/program/ProgramV71.kt`
- Program topology: 32 weeks × 7 days = 224 days

## Governance constraints

The app displays the fixed approved prescription. It does not select speed or engine variants, trigger deloads, infer missing prescriptions, inherit browsing choices, or automatically recompose the program.

Planned consolidations occur in Weeks 8, 16, and 24. Weeks 30 and 31 are realization weeks, and Week 32 contains final testing. Day 7 is the only rest day in every week. Week 32 Day 5 is a very easy bike-and-mobility recovery primer, not a second rest day. Week 32 ends at Day 7; no Week 33 is authorized.

v7.1 migrates an initialized v7.0 pointer into the seven-day structure. Legacy Days 1–7 retain the same week/day; legacy Day 8 maps to the following Week Day 1. If no initialized v7.0 pointer exists, this owner build starts at the confirmed current position, Week 2 Day 3.

The app may persist the current week/day pointer, completion marks, substitutions, timers, and local utility settings. It must not collect or persist bodyweight, calories, readiness, sleep, heart rate or HRV, pace or test results, load, reps, RIR, photos, Health Connect data, analytics, cloud-sync data, or automatic program adjustments.

Test and checkpoint results remain external. Goal pace is displayed as fixed reference text only; no result entry is required.
