# LEGEND v7.3 — Independent Audit and Correction Packet

Issued: 10/17/2026
Auditor: Claude (independent review; did not write this code)
Recipient: Cursor (builder)
Repository: `justinkulka-lang/operator-performance-android-v2`
Audited commit: `main` at `92b59d04aeca599045ef3ff8d25cc025dd14d305` (from the repository archive)
Controlling authority: v7.3 allocation packet `ARES_v7.3_Allocation_136_146_PROPOSED.md` (SHA-256 `ea902f79f221d62b24eaab2a105a47dfeff208b69b0efb2c05e8ac4490e03d15`) and v7.3 authority `ARES_Performance_Unified_Master_Authority_v7.3_PROPOSED.md` (`0f01fcc8…`), owner-approved per `OWNER_APPROVAL.md`, over the frozen v7.2 DOCX (`25a7ce49…`).

**Superseded items (as of 10/20/2026).** `LEGEND_v7.4_Continuous.md` replaces three items in this packet:
- **B5:** calibration text is removed in v7.4.
- **C2:** the interference guard now lives in v7.4 §8.
- **C3:** pace tables are removed in v7.4.

Do not implement B5, C2 or C3. Every other item stands.

---

## 0. Rules for Cursor — read before changing anything

1. **Confirm the base.** If `main` HEAD is not `92b59d0`, re-verify every finding below against the current HEAD before you change anything. Report any finding that no longer holds instead of implementing it.
2. **Verify, don't trust.** Independently check each finding and every number in this packet against the source and the authority files. Sum every table yourself (this is the owner's standing requirement). If you disagree with a finding, report it with evidence and do not implement it.
3. **Scope.**
   - **Part B** may be implemented once the owner issues this packet to you.
   - **Part C may not be implemented** until the owner's approval text for that item is recorded in the repo (see C0).
   - **Part E** is out of scope.
4. **Branch.** Work on `cursor/legend-v73-audit-corrections`, open a PR, and do not merge, tag or release.
5. **Frozen files.** Do not edit any file CI diff-checks against `28944300f1e7…`:
   - `ProgramV56.kt`, `ProgramV70.kt`, `ProgramV71.kt`, `ProgramV72.kt`, `SessionResolverV72.kt`
   - `ProgramNotesV71.kt`, `ProgramNotesV72.kt`
   - `app/src/test/resources/program-baseline.tsv`

   Do not edit the bytes of any file listed in `docs/legend-v73/SHA256SUMS.txt`.
6. **No training changes.** No set, rep, RIR, movement, alternative, conditioning or schedule change is authorized by this packet. The only resolved-session text allowed to change is listed in D2.
7. **Evidence, not claims.** Report results only from the actual CI run on the exact PR head (see D4). Passing arithmetic is not a passing Android suite.
8. **Add this packet to the repo.** Commit this file unchanged as `docs/legend-v73/INDEPENDENT_AUDIT_2026-10-17.md`.

---

## Part A — Audit report

### A1. Scope and limits

**Audited:**
- All program data and resolvers (`ProgramV73`, `SessionResolverV73`, the v7.2 data and resolver they reuse)
- State and migration (`ProgramStateStore`, `WorkoutProgress`)
- The main UI flow, privacy and the manifest
- The CI workflow and the test inventory
- The committed APK, decoded and hashed

Data parity was checked with independent parsers run against the authority text.

**Not audited:**
- **No build and no tests run.** The Android SDK and Maven repositories were unreachable from the audit workspace.
- **No GitHub CI history.** GitHub Actions was unreachable.
- **No device testing.**

### A2. Findings

No finding changes a prescription.

| # | Severity | Finding | Evidence |
| --- | --- | --- | --- |
| F1 | High | `main` carries v7.3 plus the timer-expiry follow-up, but the repo has no record that this commit passed CI. Its own records also say the merge was not authorized. | The last recorded pass is `972a590` (run 37324374962). `AUDIT_CORRECTIONS.md` and `OWNER_APPROVAL.md` both state "No merge, tag or release is authorized." Device and signing acceptance remain pending. |
| F2 | Medium | The committed APK in `download/` is the old v7.2 build. | `legend-v7.2-376925a6d655-debug.apk`: versionName 0.38.0, versionCode 47, debug v2 signature, SHA-256 `aca7e6cc…f878eb`. Source is 0.39.0 / 48. A device on code 48 rejects it as a downgrade. |
| F3 | Medium | The v7.3 authority requires preserving "24-hour / 6–12-hour / 6-hour interference guidance" but never states it. | The rules are the owner's interference guard: lower-body resistance → hard run at least 24 h; lower-body resistance → Speed at least 6–12 h; hard run → Speed at least 24 h; a 6 h same-day floor. They were never written into v7.2 or v7.3 (open builder item A02). The v7.3 week satisfies the first three. Day 4 (Speed → Upper C) and Day 5 (Upper B → easy aerobic) run back to back, which conflicts with a literal 6 h same-day floor. |
| F4 | Medium | The governance docs describe a superseded state. | `PROGRAM_V7_AUTHORITY.md` says v7.2 controls and new installs start under v7.2; the code starts new installs under v7.3. `docs/audit/` targets v7.2 candidate `376925a`. There is no README. |
| F5 | Low | Arithmetic error in the authority: the 1000 m goal-pace split is 4:08.6; correct is 4:08.5. | 600 s × 1000 / 2414.016 m = 248.548 s, which is 4:08.5 at one decimal. The other five splits are correct. The app reproduces the authority text in `ProgramV72.GOAL_PACE_TABLE`. |
| F6 | Low (latent) | A week-authority history entry is overwritten by stale in-memory state. | `ProgramStateStore.markSessionEngaged` (lines 354–360) saves a history entry from a fresh `load()`. `OperatorPerformanceApp` then persists its older in-memory `state`, which drops that entry (lines 98–101, 213–217). No effect today because `v73ActivationWeek` yields the same authority. If the activation week ever changes without clearing a saved workout, `WorkoutProgress.normalize`'s `require` throws while `TodayScreen` composes (line 344) and the app crashes. |
| F7 | Low | v7.3 sessions dropped the in-session RIR calibration protocol but kept the follow-up "calibration correction" line. | `SessionResolverV72.resistanceDay` attaches `ProgramV72.calibrationNote` to the warm-up row in normal weeks. `SessionResolverV73.resistanceRows` attaches no note, while `GymExecutionUi.sessionExecutionGuidance` still shows `calibrationCorrection`. |
| F8 | Info | The phase purpose copy differs from the authority's phase emphasis. | This is intentional: `LegendVisualIdentityTest.phasePurposeCopyStaysNeutral` bars "goal-pace", "event", "taper" and "final test" from that copy. It is display text only. Record the decision (B3); do not change the code. |
| F9 | Info | Release hardening is still open. | Release minification is off. There is no production signer (it fails closed by design). `allowBackup=false` means the pointer does not carry to a new phone. The rest timer runs in the foreground only, with no notification. |

### A3. Verified clean (zero mismatches)

| Check | Result |
| --- | --- |
| Resistance slots vs v7.3 allocation | 40 of 40 match on movement, base sets, Block 3 sets, reps, RIR, owner muscle, ranked alternatives and home alternate |
| Recovery-week sets (W8/16, W24, W30/31) | All 120 allocation values reproduced by the app's rules: halve and round down (minimum one) for consolidation; halve and round up for realization |
| Totals | 136 / 146 / 59 / 65 / 77. Per-day totals equal the packet's session table and stay within caps of 28/28/24/28/28 (Block 3: 30/30/26/30/30) |
| Muscle matrix | 13 muscles × 5 week types, direct and fractional, all equal to the packet |
| Fractional-credit map | 40 of 40 match; one PRIME owner per slot |
| Conditioning | All 96 D1 Engine, D4 Speed and D5 easy-aerobic prescriptions equal the v7.2 text; the only difference is "x" rendered as "×" |
| Throws | Exactly weeks 3, 4, 5, 6, 10, 11, 13, 15, 17, 20, 25 and 28; always after jumps and before sprints; none on special-endurance, recovery or test weeks |
| Day 4 order | Speed warm-up → jumps → throws → sprints → cooldown → resistance warm-up → Upper C |
| Day 1 | Engine only, all 32 weeks |
| Week 32 | Identical to the v7.2 fixed overlay: 13 sets (Day 3: 8, Day 5: 5); no Upper C, lower lifting or Day 5 aerobic |
| Sprint volume | Maximum 600 m, with a warning on weeks 12, 18, 19, 22 and 23 |
| Privacy | No INTERNET permission in the source or the shipped APK; no network, analytics or health-data code; all backup domains excluded; set taps not stored |
| Migration | Engaged weeks keep their authority; v7.3 activates at the next positively unstarted Day 1; no Week 33 |
| Test inventory | 116 JVM and 24 instrumented `@Test` methods, matching the repo's records (not executed by the auditor) |

### A4. Programming observations (information only; no change)

- **Upper work clusters on Days 3–5.** Chest, upper back, lats, delts and biceps are trained on three consecutive days, then rest four days (Day 6 through Day 2).
- **Day 4 runs about 1 h 45 min to 2 h** (auditor's estimate).

The owner will review both after Block 1. Cursor takes no action on either.

---

## Part B — Builder corrections (no authority change)

### B1 (F1) — Produce evidence for the exact head

- Report whether a CI run exists for `92b59d0` on `main`, with its run ID, attempt and conclusion.
- Run the full pipeline on the PR head and report it (see D4).
- Do not edit `OWNER_APPROVAL.md`. Ratifying the merge is the owner's act (C1).

### B2 (F2) — Remove the stale APK

Delete `download/` (both files). CI already publishes the debug APK and ZIP per run.

If the owner says he installs from this folder, do this instead: replace its contents with the green PR-head artifact, named `legend-v7.3-<versionName>-<commit12>-debug.apk`, plus a `SHA256SUMS` file whose hash you recompute locally.

### B3 (F4, F8) — Bring the governance docs current

**1. Rewrite `docs/PROGRAM_V7_AUTHORITY.md`.** It must state:
- v7.3 controls new installs and resets. Name its files and SHA-256 values and point to `OWNER_APPROVAL.md`.
- v7.2 is frozen and resolves retained historical weeks only.
- v7.1 is legacy.
- The governance constraints already in that file, unchanged.
- The phase purpose copy is intentionally neutral display text, enforced by `phasePurposeCopyStaysNeutral`, and is not a prescription (F8).

**2. Archive the v7.2 audit packet.** Move `docs/audit/` to `docs/archive/v7.2-candidate-376925a/` with no content edits. Update any paths that reference it.

**3. Add a one-page `README.md`** covering:
- What LEGEND is: an offline reference that keeps no performance records.
- The controlling authority and where it lives.
- Frozen authorities and the CI freeze.
- How CI builds and where the APK is published.
- Where verification evidence lives.
- That merge, tag and release require owner approval.

**4. Leave the authority filenames alone.** Do not rename `*_PROPOSED.md` files; their names are bound to `SHA256SUMS.txt`.

### B4 (F6) — Stop stale state from overwriting week-authority history

1. `ProgramStateStore.markSessionEngaged(week, day)` returns the `ProgramUiState` it saved. Return the loaded state unchanged when no history entry is added.
2. `ProgramStateStore.saveWorkout(s)` returns that same state.
3. In `OperatorPerformanceApp`, adopt the returned state:
   - `onAdvance`: `val engaged = stateStore.markSessionEngaged(...); clearWorkout(); persist(engaged.advanceDay())`
   - `persistWorkout`: `state = stateStore.saveWorkout(next)`
4. Before `TodayScreen` receives `activeWorkout`, drop a saved workout whose non-null `authority` differs from `state.authorityForWeek(workout.week)`, and clear it from storage in a side effect, not during composition. Keep the `require` in `WorkoutProgress.normalize`; this change only ensures callers never violate it.
5. Make no change to migration rules, keys, schema or activation logic.

### B5 (F7) — Restore the in-session calibration note on v7.3

In `SessionResolverV73.resistanceRows`, set the warm-up row's `note`:
- `ProgramV72.calibrationNote` when `week !in setOf(8, 16, 24, 30, 31)`
- otherwise `null`

Week 32 already returns the frozen v7.2 sessions.

### B6 — Version identity

Bump `versionCode` to 49 and `versionName` to `0.40.0` so the phone build can be told apart from 0.39.0. Make no package, signing or applicationId change.

---

## Part C — Owner decisions (do not implement until approval is recorded)

### C0. How approval is recorded

Create `docs/legend-v73/AMENDMENT_01.md` only after the owner gives you approval text. It must:
- Quote the owner's words and date verbatim.
- List each approved item (C2, C3).
- Restate the exact amended text.

Add it to `SHA256SUMS.txt` as a new line. Do not edit the existing v7.3 files.

### C1 (F1) — Merge ratification

**Owner act, not Cursor.** After B1 shows a green exact-head run, the owner adds a dated line to `OWNER_APPROVAL.md` that ratifies v7.3 on `main`, or directs a revert.

### C2 (F3) — Write the owner's interference guard into the authority

**Proposed amendment.** Replace "the approved 24-hour / 6–12-hour / 6-hour interference guidance" with:

> Interference guard: lower-body resistance to a hard run, at least 24 hours; lower-body resistance to Speed, at least 6–12 hours; a hard run to Speed, at least 24 hours; a running session and a lower-body resistance session on the same day, at least 6 hours apart. Day 4 Speed followed by Upper C, and Day 5 Upper B followed by easy aerobic, are owner-sequenced and stand as written.

**Owner decision required.** Confirm the 6 h floor applies only between running and lower-body resistance, as written above, or remove it.

**Compliance check (auditor).** The v7.3 week already satisfies every rule above:

| Rule | v7.3 schedule | Status |
| --- | --- | --- |
| Lower → hard run ≥ 24 h | Day 6 → Day 1: 48 h; Day 2 → next Day 1: 6 days | Met |
| Lower → Speed ≥ 6–12 h | Day 2 → Day 4: 48 h | Met |
| Hard run → Speed ≥ 24 h | Day 1 → Day 4: 72 h | Met |
| Same-day running and lower lifting | None scheduled | Met |

**Implementation after approval:**
- Record it in `AMENDMENT_01.md`.
- Close A02 in `AUDIT_CORRECTIONS.md` with a pointer to the amendment.
- Add the approved text to `ProgramNotesV73.interference` (v7.3 only).
- No schedule or prescription change.

### C3 (F5) — Correct the 1000 m goal-pace split

**Proposed amendment.** The 10:00 1.5-mile reference reads:

> 400 m 1:39.4; 600 m 2:29.1; 800 m 3:18.8; 1000 m 4:08.5; 1 mile 6:40.0; 1.5 miles 10:00.0

**Implementation after approval:** v7.3 only, because `ProgramV72.kt`, `SessionResolverV72.kt` and `ProgramNotesV72.kt` are frozen.

1. Add `ProgramV73.GOAL_PACE_TABLE` with the corrected string.
2. In `V73SessionBuilder`, for Day 1 rows, replace the substring `ProgramV72.GOAL_PACE_TABLE` with `ProgramV73.GOAL_PACE_TABLE` in the row `note`.
3. Build `ProgramNotesV73.runImpact` from the v7.3 table instead of reusing `ProgramNotesV72.runImpact`. Keep `testGate`, `ANCHOR_NOTE` and `easyRunNote`.
4. Narrow `ProgramV73Test.allConditioningRowsAndFinalWeekAreRetainedExactly` so it allows exactly this substitution in goal-pace weeks and nothing else.

v7.2 output stays byte-identical.

---

## Part D — Tests and definition of done

### D1. New or updated tests

**T1 (B4) — unit tests:**
- Engage a week, advance using the returned state, and assert the persisted history still contains that week.
- `saveWorkout` returns a state whose history includes the engaged week.
- A saved workout with a mismatched authority is excluded from Today and cleared from storage, and Today renders without an exception.

**T2 (B5) — unit tests:**
- The v7.3 warm-up row note equals `ProgramV72.calibrationNote` in every normal week, Days 2–6.
- It is `null` in weeks 8, 16, 24, 30 and 31.
- Week 32 is unchanged.

**T3 (C3 only, if approved):**
- Compute each split in the test as `600 × d / 2414.016`, rounded to 0.1 s; do not hardcode the expected strings.
- No v7.3 row note or v7.3 notes list contains `4:08.6`.
- The v7.2 snapshot is byte-identical.

**T4 (B6):** `BuildConfig` and manifest values read 0.40.0 / 49.

### D2. Allowed resolved-session diff

Emit the v7.3 `resolved-sessions.tsv` before and after your changes. The only lines allowed to differ are:

- `ROW v73_d{2..6}_resistance_warmup` note fields in normal weeks (B5).
- If C3 is approved: Engine goal-pace note fields in weeks 13, 18, 20, 23, 26, 27, 28, 29, 30 and 31.

Any other diff line is a defect.

### D3. Must stay identical

- The frozen v7.1 and v7.2 snapshot and baseline.
- Every set count and total in `ProgramAudit.validateV73()` and `ProgramV73Test`.
- Migration behavior and stored key names.

### D4. Report back to the owner

1. PR head commit SHA.
2. CI run ID and attempt, with its conclusion.
3. Test counts from the downloaded XML: JVM executed / failed / errored / skipped, and full-emulator methods executed / failed.
4. Lint errors and warnings.
5. APK SHA-256, recomputed locally from the downloaded artifact.
6. The D2 diff, attached as a file.
7. Each finding F1–F9 marked implemented, deferred to the owner (Part C) or disputed (with evidence).

Physical Galaxy S26 Ultra acceptance and installed-certificate continuity remain separate owner gates. Do not claim them.

---

## Part E — Out of scope for this branch

- **F9 release items:** minification, the production signer, backup policy and the lock-screen or notification rest timer.
- **The A4 observations.**
- **Program changes of any kind** to the allocation, conditioning, schedule, throws or recovery weeks.
- **Renaming the app, its package or its phases.**
