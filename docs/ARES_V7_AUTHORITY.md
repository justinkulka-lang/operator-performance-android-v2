# ARES Performance v7.0 Authority Record

- Approved source: `01-ARES_Performance_Unified_Master_Authority_v7.0_APPROVED.docx`
- SHA-256: `0f9bd7981dc0671e836165a2b3efa70113c09cfb26e76ffc9c623ebc452dcca0`
- Runtime source: `app/src/main/java/com/jk/operatorperformance/program/ProgramV70.kt`
- Program topology: 28 cycles × 8 days = 224 days (32 calendar weeks)

## Governance constraints

The app displays the fixed approved prescription. It does not select speed or engine variants, trigger deloads, infer missing prescriptions, inherit browsing choices, or automatically recompose the program.

Planned overlays exist only in Cycles 7, 14, 21, 26, 27, and 28. Days 4 and 8 are rest days. Cycle 28 ends at Day 8; no Cycle 29 is authorized.

Existing v5.7 preferences are left intact. v7.0 uses a new preference namespace and starts only after the user explicitly confirms Cycle 1, Day 1. There is no automatic week/day conversion.

The app may persist the current cycle/day pointer, completion marks, substitutions, timers, and local utility settings. It must not collect or persist bodyweight, calories, readiness, sleep, heart rate or HRV, pace or test results, load, reps, RIR, photos, Health Connect data, analytics, cloud-sync data, or automatic program adjustments.

Test and checkpoint results remain external. Goal pace is displayed as fixed reference text only; no result entry is required.
