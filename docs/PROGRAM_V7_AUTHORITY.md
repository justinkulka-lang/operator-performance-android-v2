# Program authority record

v7.3 is the controlling program for a new install and for every week at or after its recorded activation week. v7.2 remains the approved prescription for weeks retained under v7.2. v7.1 remains available only so an in-progress v7.1 week, or a week already recorded as v7.1, can finish under v7.1.

LEGEND v7.4 Continuous was approved by the owner on 10/20/2026 and is pending implementation in Task 2. It is not the runtime program. This record does not implement it.

- v7.3 runtime source: `app/src/main/java/com/jk/operatorperformance/program/ProgramV73.kt`
- v7.3 proposal: `docs/legend-v73/ARES_Performance_Unified_Master_Authority_v7.3_PROPOSED.md`
- v7.3 proposal SHA-256: `0f01fcc8a91109f81c3df7760b705a0d8de1848cdfce758c944d026e6e30894f`
- Owner approval of that proposal is `docs/legend-v73/OWNER_APPROVAL.md` (4 October 2026). This correction does not edit that file.
- Issued v7.3 hashes in `docs/legend-v73/SHA256SUMS.txt` stay unchanged.
- v7.2 runtime source: `app/src/main/java/com/jk/operatorperformance/program/ProgramV72.kt`
- Approved v7.2 file: `docs/ARES_Performance_Unified_Master_Authority_v7.2_APPROVED.docx`
- v7.2 DOCX SHA-256: `25a7ce497569913c4a56c057bcaa139290adddf20876a2d736790d5fefd436f3`
- That digest was recomputed from the file above. `DOCX_SHA256_VERIFIED` is true.
- v7.1 historical file: `docs/ARES_Performance_Unified_Master_Authority_v7.1_APPROVED.docx`
- v7.1 SHA-256: `1d33dbca214b34dd456a971d32bc1c19d59e3a13069393429fc9e2115e3a036c`
- Topology: 32 weeks × 7 days = 224 days. There is no Week 33.

## Governance constraints

The app displays the fixed approved prescription for the authority recorded for that week. It does not select speed or engine variants, trigger deloads, infer missing prescriptions, inherit browsing choices, or automatically recompose the program.

The phase purpose copy is intentionally neutral display text, enforced by `LegendVisualIdentityTest.phasePurposeCopyStaysNeutral`, and is not a prescription.

A new install starts at Week 1 Day 1 under v7.3. An initialized historical week finishes under the authority already recorded for it. Moving the pointer, changing a utility timer, or saving a workout must not erase a week-authority assignment already recorded. An explicit reset to Week 1 Day 1 under v7.3 is the path that clears those assignments. The following week after a retained week uses the activation boundary already stored. No week after 32 is created.

The app may persist the current week/day pointer, the active authority, the per-week authority history, completion marks, substitutions, timers, and local utility settings. It must not collect or persist bodyweight, calories, readiness, sleep, heart rate or HRV, pace or test results, load, reps, RIR, photos, Health Connect data, analytics, cloud-sync data, or automatic program adjustments.

Today resolves the authority recorded for the current week. A saved session or pointer that names a different authority is not applied to that reference.

Audit notes for earlier candidates remain in `docs/archive/v7.2-candidate-376925a/`. v7.3 implementation evidence is in `docs/legend-v73/`.
