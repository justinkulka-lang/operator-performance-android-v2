# Independent audit request

Please audit the candidate on `cursor/ares-v72-full-app-dc41` against the approved v7.2 DOCX, not against this summary. This packet is not itself an independent audit and is not a production-readiness claim.

1. Recompute SHA-256 of `docs/ARES_Performance_Unified_Master_Authority_v7.2_APPROVED.docx`. The local recomputation matched `25a7ce497569913c4a56c057bcaa139290adddf20876a2d736790d5fefd436f3`.
2. Diff `AresProgramV72` and `V72SessionBuilder` against every section 4 row, every section 5 row, and sections 3.2 and 6.1–6.3. `V72AuthorityComparisonTest` transcribes those rows and checks them. It does not use the program object as both expected and actual.
3. Confirm weeks 8 and 16 include the Engine cooldown. Confirm `V72AlternativeMapTest` against Section 4 and Section 7.1: later slots inherit the Phase 1 cell, and trap-bar deadlift is not an RDL alternative.
4. Confirm an initialized Day 1 without a session-status record stays on v7.1, and that a recorded not-started Day 1 is the case that activates v7.2.
5. Confirm `AresProgramV71` still serves a retained week and that one session never mixes the two authorities.
6. Re-run `:app:testDebugUnitTest`, `:app:lintDebug`, and `:app:assembleDebug` at the candidate commit. Confirm package `com.jk.operatorperformance`, versionCode 47, and versionName 0.38.0. Confirm the APK embeds the 12-character commit prefix.
7. Run the Galaxy S26 Ultra checklist in `DEVICE_CHECKLIST.md`. An API 35 emulator has covered font scale 1.5, density 560, night mode, landscape, airplane-mode search, force-stop, and an emulator reboot that kept the timer file and RESUME. The physical device was not attached.
8. Do not merge, tag, release, rename the package, or change production signing from this audit alone.

This packet does not claim conformity, production readiness, release approval, or a completed independent audit.
