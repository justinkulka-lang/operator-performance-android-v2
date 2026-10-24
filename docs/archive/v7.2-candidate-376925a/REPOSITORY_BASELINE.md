# Repository baseline

Recorded before the v7.2 correction, then updated with the candidate branch.

| Item | Value |
| --- | --- |
| Repository | `justinkulka-lang/operator-performance-android-v2` |
| Remote | `origin` → `https://github.com/justinkulka-lang/operator-performance-android-v2` |
| Starting branch | `construction/ares-v7-unified-authority` |
| Starting HEAD | `3d8556557504fbbb53fa12d738741c05caac7465` |
| Starting tree | Clean |
| Work branch | `cursor/ares-v72-full-app-dc41` |
| Open pull requests at start | None |
| applicationId | `com.jk.operatorperformance` |
| versionCode | 47 |
| versionName | 0.38.0 |
| minSdk / compileSdk / targetSdk | 28 / 36 / 36 |

`main` is an ancestor of the starting HEAD. This correction does not restart from `main`.

## Authority files

- v7.1 DOCX is in the repo at `docs/ARES_Performance_Unified_Master_Authority_v7.1_APPROVED.docx`.
- Recomputed SHA-256: `1d33dbca214b34dd456a971d32bc1c19d59e3a13069393429fc9e2115e3a036c`.
- The v7.2 DOCX was not in the workspace or the handoff upload. Expected SHA-256 `25a7ce497569913c4a56c057bcaa139290adddf20876a2d736790d5fefd436f3` was **not** recomputed from DOCX bytes.
- The extracted v7.2 text used with the owner's pasted authority has SHA-256 `f66ac294ab785c1377683f84d80e28c80e9180b52f70b62a020b6001934c3135`. That digest is not the DOCX digest.

## Base CI at `3d85565`

Mixed. Several v7.1 and v5.7 workflows succeeded. Two failed in about 35 seconds on 2026-09-28:

- v7.1 CI, run `36364996205`: `sdkmanager` could not find `build-tools;37.1.0`.
- v7.1 Authority Audit, run `36364996241`: same missing platform/build-tools package.

The app's compile SDK is 36. Those two workflows now install `platforms;android-36` and `build-tools;36.0.0`, matching the workflows that already passed.
