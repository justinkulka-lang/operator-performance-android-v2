# Independent audit request

Please audit commit `376925a6d655134f748f0141677ce814ccbb9c4e` on `cursor/legend-identity-dc41` against the approved v7.2 DOCX, not against this summary. This packet is not itself an independent audit and is not a production-readiness claim.

The debug APK built from that commit is `download/legend-v7.2-376925a6d655-debug.apk`.

- SHA-256: `aca7e6ccb1139175508e67e20aed311f1f5c6cf76f097772917d13f14f3878eb`
- Embedded build label: `376925a6d655`
- Package `com.jk.operatorperformance`, label `LEGEND`, versionName `0.38.0`, versionCode `47`
- A zip of that same APK plus `SHA256SUMS` is `download/legend-v7.2-376925a6d655-debug.zip`

`download/legend-v7.2-debug.apk` is the older `f55eff0503b7` file. Do not review it as this candidate.

1. Recompute SHA-256 of `docs/ARES_Performance_Unified_Master_Authority_v7.2_APPROVED.docx`. The local recomputation matched `25a7ce497569913c4a56c057bcaa139290adddf20876a2d736790d5fefd436f3`.
2. Diff `ProgramV72` and `V72SessionBuilder` against every section 4 row, every section 5 row, and sections 3.2 and 6.1–6.3. `V72AuthorityComparisonTest` transcribes those rows and checks them. It does not use the program object as both expected and actual.
3. Confirm weeks 8 and 16 include the Engine cooldown. Confirm `V72AlternativeMapTest` against Section 4 and Section 7.1: later slots inherit the Phase 1 cell, and trap-bar deadlift is not an RDL alternative.
4. Confirm an initialized Day 1 without a session-status record stays on v7.1, and that a recorded not-started Day 1 is the case that activates v7.2.
5. Confirm `ProgramV71` still serves a retained week and that one session never mixes the two authorities.
6. Re-run `:app:testDebugUnitTest`, `:app:lintDebug`, and `:app:assembleDebug` at the candidate commit with `GITHUB_SHA` set to `376925a6d655134f748f0141677ce814ccbb9c4e`. Confirm package `com.jk.operatorperformance`, versionCode 47, and versionName 0.38.0. Confirm the APK embeds `376925a6d655`.
7. Run the Galaxy S26 Ultra checklist in `DEVICE_CHECKLIST.md`. An API 35 emulator covered the checks in `VALIDATION_REPORT.md`. The physical device was not attached. Landscape Key return failed on the emulator after the activity was destroyed; the portrait Key test passed on rerun.
8. Do not merge, tag, release, rename the package, or change production signing from this audit alone.

This packet does not claim conformity, production readiness, release approval, or a completed independent audit.
