# ARES v7.3 — implementation correction map

Status: planning only. The proposed authority's exact 136/146 allocation must be reviewed before app implementation. No app code changed during packet preparation. Current reference fix is draft PR #18 at `28944300f1e73015c70b8e342bcc90173022cd25`; v7.2 integration candidate is draft PR #17 at `cc6478819dceb97b11c0de40895fee33d05de3f8`. Neither is assumed merged. Verify live state at implementation start.

## Change-control dependencies

1. Owner review of the exact revised allocation, proposed 30/26 Block 3 caps, ranked alternatives and nine-total-throw convention.
2. Freeze the accepted authority bytes and actual SHA-256; approved v7.2 original remains unchanged. Keep approval evidence separate from file integrity.
3. Resolve the candidate base and carry forward the mixed-Speed reference fix. Do not use historical main as though it already contained v7.2.
4. Use an isolated v7.3 branch and draft PR. Do not merge, tag, release or overwrite retained v7.2 data.

## Source and behavior map

Paths below are repository-relative. Existing paths were inspected in the v7.2 candidate; names labelled NEW are planned, not files already in the repository.

| Area / files | Required correction | Acceptance evidence |
|---|---|---|
| NEW `docs/ARES_Performance_Unified_Master_Authority_v7.3_APPROVED.md` after acceptance; NEW `docs/ARES_AUTHORITY_v7.3_CHANGES.md` | Commit accepted authority and amendment ledger with actual hashes. Record exact approval; never rename proposal approved prematurely. | Byte hashing, source/authority identity test; original v7.2 DOCX unchanged. |
| NEW `program/ProgramV73.kt` | Add immutable v7.3 normal and peak slot tables, role maps, alternatives and weekly overlays; version 7.3; no load/result input. Blocks total 136/136/146/136. | Independent expected tables transcribed from accepted authority, all slots/doses matched. |
| `program/ProgramModel.kt` | Add V73 authority identity and only necessary data fields for per-block prescriptions/alternatives. One PRIME owner per physical set. Keep old types and values backward compatible. | Enum serialization/migration tests; retained authority snapshots. |
| NEW `program/SessionResolverV73.kt`; `program/SessionResolver.kt` | Dispatch V73 explicitly. D1 zero resistance. D4 warm-up/jumps/allowed throws/sprints/running cooldown/resistance warm-up/Upper C. Preserve D5 aerobic order and week-32 fixed overlay. | All 224 new sessions audited, ordered row assertions, unchanged conditioning comparison. |
| `program/ProgramV72.kt`, `program/SessionResolverV72.kt` | Retain legacy v7.2 prescriptions and approved reference-group fix. Do not apply new slots or volumes retroactively. | Original/fixed v7.2 parity tests stay green. |
| V73 short-upper logic | Do not create an `omitShortUpper` branch for V73: Engine never lifts. Retain V72 helper for engaged old-authority weeks. | V73 D1 zero resistance for every week; old-authority parity. |
| V73 block purpose/text | Hypertrophy first in weeks 17–24; existing conditioning secondary. Describe 146-set peak without claiming an individual optimum. | App/authority copy comparison; weekly resistance maximum occurs in Block 3. |
| V73 sets and volume | Normal caps 28/24; peak caps 30/26. Direct targets independently checked against matrix. Fractional references never fulfil direct targets. No inherited 8-set/1.75/two-exposure build gates. | Inspectable muscle × actual-week matrix, session totals, normal/recovery classification. |
| V73 synergist maps | Use explicit movement-sensitive maps. Leg curls have no glute credit; pullover receives no automatic row/pulldown biceps credit. | Independent role-map expectations; unique PRIME ownership; fractional sum checks. |
| V73 consolidation | W8/16 floor(base/2), min 1, 59 total; W24 floor(peak/2), min 1, 65; 3–4 RIR. | Per-slot expected reductions and actual totals; no target inflation during recovery. |
| V73 realization | W30/31 ceil(base/2), 77 total, 43.38% reduction, familiar loads, compounds 2–3/isolations 1–2 RIR. | Per-slot sets and relative reduction independently calculated. |
| V73 week 32 | Exact v7.2 §6.3, 13 total, no lower lifting or Upper C; do not invent repetition ranges or apply new normal alternatives accidentally. | Original week-32 snapshot and authority comparison. |
| V73 throws | Nine total hip-driven scoop/rotational throws, 3×3; only weeks 3/4/5/6/10/11/13/15/17/20/25/28. Preserve existing jump rows; zero sprint-meter credit. | Exact allowed/omitted week lists, row ordering, sprint sums unchanged. |
| V73 sprint references | Preserve separate acceleration/fly groups: 7 efforts in W13/W20, 6 in W28/W30; 180/160 m. Keep approach/distance text literal. | Port `V72MixedSpeedReferenceTest` expectations to V73; no per-rep completion tests. |
| NEW `program/ProgramNotesV73.kt` | Complete guidance for new day roles, doses, alternatives, intensity and block purpose. Retain restored safety, RIR definition, calibration response, recovery/energy prerequisites and pace-reference limits. | DOCX/accepted authority comparisons and relevant UI visibility. |
| `program/ProgramNotesV56.kt`, V70/V71/V72 notes | Keep retained-authority content intact; route active guidance by authority. Do not globally rewrite Day 1/Day 4 statements for all versions. | Older authority views/reference searches preserved. |
| `program/ProgramAudit.kt` | Add V73 checks for every actual week; direct targets only on normal weeks, explicit reduced overlays, caps, Engine-only, Upper C exceptions, zero sleds, throw meters and unchanged 600 m warning. | Printed machine-readable and readable matrices; failures identify week/day/slot. |
| `state/ProgramStateStore.kt` | Add V73 activation evidence/revision. Keep engaged V72 week intact; next positively unstarted Day 1 activates V73. Ambiguous older evidence must not erase work. Week 32 terminates without Week 33. | Restart/persistence, engaged/unstarted/completed/ambiguous boundary cases and authority identity checks. |
| `state/WorkoutProgress.kt` | Keep session-level status only. No new effort/checklist fields. Bind saved record to its authority so older sessions are never resolved with new rows. Existing timer/substitution handling retained. | Session complete/resumable status and timer survive normalization; no new completion inferred. |
| V73 slot IDs / migration | Namespace new V73 slots; explicit retire/create for old D1 work moved to D4. Preserve old IDs for retained sessions; no rename-based completion transfer. | Active/completed old-session tests; invalid substitution IDs rejected/cleared safely. |
| `state/LegacyDataManager.kt` | Inspect cleanup/reset paths for new activation/state fields. Keep legacy export/tracking boundaries truthful; no deletion of retained week under normal browsing. | Reset/clear/legacy migration regression tests. |
| `program/ProgramExport.kt`; `state/ToolsDataStore.kt`; `state/LegacyDataManager.kt` | Inspect routing and export descriptions. Preserve current legacy-data-only export unless owner explicitly authorizes an expanded export. Do not falsely advertise V73 state as exported. | Payload namespace tests and pre-export UI truthfulness. |
| `ui/OperatorPerformanceApp.kt` | Route start/current pointer/reference views by authority; new day titles and phase purpose. Clearly expose retained versus active authority. Pointer/reset dialogs disclose consequences. | V73 start, retained-week navigation, read-only browsing, no silent state loss. |
| `ui/GymExecutionUi.kt`, `SessionFocus.kt`, `KeyScreen.kt` | Show 30/26-set peak sessions, new Upper C/throws and correct guidance. Preserve START/RESUME ergonomics and manual timer. No new result/per-rep tracking. | Large-font/landscape/scroll/accessibility and lifecycle tests with long new sessions. |
| `ui/ProgramSearch.kt` | Index active V73 movements/alternatives/guidance, with scoped retained-authority results when available. Do not import wrong-day text. | Searches find new glute/hamstring/Upper C slots and correct authority/day. |
| `ui/EquipmentInventoryUi.kt` | Inspect for existing inventory assumptions; do not introduce generation gating or automatic equipment-selected training. Home alternatives are reference replacements. No machine finisher. | Missing equipment does not mutate the original prescription/history. |
| `ui/BuildIntegrityCard.kt`, build metadata | Display actual authority/hash and exact source commit. Coordinate app version metadata without changing application ID, signer or unrelated visual identity. | Decoded manifest, embedded commit/hash, package/version verification. |
| `.github/workflows/android-ci.yml` | Retain source pinning for V72; add V73 identity and independent parity/matrix checks. Preserve full unit/lint/build and relevant instrumented/persistence gates. Do not disable protection to make a changed prescription pass. | Exact-head run, expected authority hashes, all required steps concluded honestly. |
| `.github/scripts/package-debug-candidate.sh`, upgrade/process scripts | Update distribution naming/provenance for actual active candidate authority where needed. Add built-APK prohibited-string scan and manifest inspection. Preserve fail-closed release signing and safe same-signer upgrade tests. | APK SHA-256, real commit label, no INTERNET/analytics permissions, explicit installed-signer limitations. |
| `docs/audit/*` reports, migration/validation/traceability | Update only after real implementation/verification. Separate source, CI, artifact, emulator and physical evidence. | Actual commands/test counts/artifact hashes; no builder narrative substituted for evidence. |

`program/`, `state/`, and `ui/` abbreviate `app/src/main/java/com/jk/operatorperformance/`.

## Regression plan

| Existing tests / planned additions | Required coverage |
|---|---|
| NEW `ProgramV73Test`, `V73AuthorityComparisonTest`, `V73VolumeMatrixTest` | Independent normal/peak tables, targets, all 32 recovery overlays, explicit fractional map, cap checks and printed artifact. |
| `ProgramSnapshotTest` | Keep immutable V71 and fixed V72 expectations. Add V73 accepted baseline separately. No global regeneration that hides legacy drift. |
| `SessionResolverTest`, `SprintWarningTest`, NEW `V73MixedSpeedReferenceTest` | Correct grouping/order, 180/160 m, 600 m cap/warning, zero jump/throw meters, unchanged anchors/conditioning. |
| `ProgramV56Test`, V70/V71 tests, V72 parity/authority/alternative tests | Preserve retained-authority behavior; historical day titles must remain historical, not rewritten to V73. |
| `AuthorityMigrationTest`, `ProgramStateStorePersistenceTest`, `ProgramUiStateTest`, `WorkoutProgressTest` | V72-to-V73 boundary, ambiguous evidence, engaged/completed session status, moved IDs, no Week 33, reset consequences and restart. |
| `ProgramExportTest`, `LegacyExportTest`, tools migration tests | Truthful namespace coverage, preserved legacy data, no new result/state export without authorization. |
| `GymExecutionUiTest`, `SessionFocusTest`, search tests | New sessions and alternatives visible; long-session ergonomics; no result entry/checklist added. |
| `MasterAuditUiTest`, `ReferenceUiTest`, `ReleaseReadinessUiTest`, navigation/accessibility tests | Scope title/content assertions by authority. Add V73 Engine-only/Upper C and full 32-week references. |
| NEW V73 launch/reference/font-scale/timer/persistence instrumented tests; existing V72 Key/lifecycle suites | Retained-authority launch, V73 activation, readable peak sessions, START/RESUME, timer persistence, rotation/back/recreation. Keep existing regression coverage. |
| Physical Galaxy S26 Ultra | Airplane-mode launch/reboot, retained data, all new session references at chosen font scale. Pending unless actually performed; do not clear installed data to overcome certificate mismatch. |

## Required handoff evidence

Report exact final commit/tree, authority checksum, every changed file/test, declared versus executed test counts, actual commands and exact-head CI URL. Export direct/fractional per-week matrices, overlays and cap checks. Inspect the assembled APK, not just source, for prohibited content and manifest permissions; record SHA-256 and actual signer. Report approval state, physical-device limitations and signing lineage honestly. Do not equate passing arithmetic with physiological suitability or owner approval.

## Disclosed programming judgments requiring review

- Exact 146-set peak and 30/26 caps are a proposal under the owner's permission to increase Block 3 caps; not already an accepted numerical ceiling.
- Base targets are revised direct values that fit 136 physical sets. Upper-back/calf/trap/forearm/abs direct volume remains level in Block 3 while the overall block is highest.
- Trunk work uses 1 RIR; unilateral set means both sides; nine rotational throws are total rather than per side. No extra conditioning is added.
- Machine/cable home alternatives disclose different loading curves. Fractional weights are reference estimates and never fill a missing direct target.
- Weeks 30/31 use per-slot ceiling-halving to hit 77 sets and a 43.38% cut. This replaces the literal old realization table under the owner's decision.
