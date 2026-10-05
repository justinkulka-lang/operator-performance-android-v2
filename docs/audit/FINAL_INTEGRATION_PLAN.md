# LEGEND v7.3 final integration plan — no merge authorized

## Candidate and approved scope

The isolated candidate branch is `codex/legend-v73-approved-program`, draft PR #19. Owner approval on 4 October 2026 accepted the issued 136/146 packet; the unchanged proposal filenames and bytes are linked to the separate `docs/legend-v73/OWNER_APPROVAL.md` record. The approved v7.2 DOCX remains immutable. Version `0.39.0 (48)` preserves application ID `com.jk.operatorperformance`.

## Integration route

1. Review the cumulative draft stack in order: PR #17 (`codex/legend-final-corrections` to `main`), PR #18 (mixed-Speed reference fix to PR #17), PR #19 (approved v7.3 implementation to PR #18). Existing PRs #13–#16 are historical development stages; do not merge their overlapping diffs again. No stack merge is authorized now.
2. Freeze the latest PR #19 head and its exact successful `LEGEND versioned candidate` run. Checkout SHA, full embedded source SHA, APK checksum, signer and evidence must identify that same source. Historical passes do not validate a subsequent commit.
3. Independently inspect the downloaded XML, program matrices, resolved sessions, upgrade preferences, decoded APK and rendered captures. Normal weeks total 136/146; weeks 8/16/24 are 59/59/65; weeks 30/31 are 77; week 32 remains the immutable 13-set exception. Preserve all conditioning, legacy authority checks and week-32 termination.
4. After explicit integration authorization, merge or retarget the draft stack sequentially without duplicate integration. Recheck conflicts and run the full suite on the resulting exact main commit; no feature-branch green claim substitutes for integration validation.
5. Complete Galaxy S26 Ultra acceptance separately. Test the real installed certificate before attempting an update. A same-certificate CI upgrade is emulator evidence only. Never uninstall or clear phone data to force installation.
6. For a production build, the owner supplies the four existing `OPERATOR_RELEASE_*` values through the approved secret channel and establishes the existing certificate/upgrade lineage and distribution channel. No production key is generated or borrowed. Reconcile the intended production version at that point.
7. Verify positive production signing, package/version, alignment, manifest, full source provenance, installation/update and program parity on the exact APK/AAB. The current missing-credential refusal proves only the negative signing gate.
8. Obtain explicit authorization before merge, tag, release, store upload or publishing. Program implementation approval does not authorize these actions.

## CI and evidence boundary

`.github/workflows/android-ci.yml` is the sole active read-only pipeline. It has no signing secrets, branch-write, merge or publishing step. Removed duplicate and branch-mutating workflows remain in history. Required-check configuration must be inspected and reconciled by an authorized repository administrator before integration if it refers to retired jobs; no feature-branch check establishes that configuration.

A fresh GitHub runner may create a different debug certificate. The currently verified CI certificate is not a production lineage and must not be claimed compatible with the historically installed app. Artifact-only or documentation-only commits also require a newly matching APK if that newer SHA is presented as the build source.

## Remaining acceptance gates

- Exact latest draft-head CI, final artifact and screenshot review: record actual evidence after the run.
- Physical Galaxy S26 Ultra acceptance: NOT RUN without real device evidence.
- Historical installed-certificate compatibility: NOT PROVEN.
- Positive owner release signing: NOT RUN without owner credentials.
- Main integration checks: pending an explicitly authorized integration.
- Merge, tag, release and publishing: NOT AUTHORIZED.
