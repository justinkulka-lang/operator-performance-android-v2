# Independent audit request

Please audit the candidate on `cursor/ares-v72-full-app-dc41` against the approved v7.2 DOCX, not against this summary.

1. Recompute SHA-256 of `ARES_Performance_Unified_Master_Authority_v7.2_APPROVED.docx` and compare it with `25a7ce497569913c4a56c057bcaa139290adddf20876a2d736790d5fefd436f3`. This workspace did not contain that file.
2. Diff `AresProgramV72` and `V72SessionBuilder` against every section 4 row, every section 5 row, and sections 6.1–6.3. Confirm Week 32 does not invent repetition ranges and percentage sessions do not invent an anchor.
3. Confirm `AresProgramV71` still serves a retained week and that one session never mixes the two authorities.
4. Re-run `:app:testDebugUnitTest`, `:app:lintDebug`, and `:app:assembleDebug` at the candidate commit. Confirm package `com.jk.operatorperformance`, versionCode 47, and versionName 0.38.0.
5. Run the Galaxy S26 Ultra checklist in `DEVICE_CHECKLIST.md`.
6. Do not merge, tag, release, rename the package, or change production signing from this audit alone.

The local debug APK SHA-256 is `48f0f394a49ef6e8845e81775973d227ad0ea9edf90c32350cf3d1240011194c`. Rebuild before comparing a CI artifact.
