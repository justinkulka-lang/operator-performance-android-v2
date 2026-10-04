# LEGEND final integration plan — no merge authorized

Frozen input: `9cfbd896e7d7103e9833d0563a463a5eac91983c`, branch `cursor/legend-identity-dc41`.
Corrections are isolated on `codex/legend-final-corrections`. The package, version, program, persisted identifiers, and authority documents are not changed.

## Integration route

1. Review the final correction diff against the frozen input separately from the complete accumulated diff against `main`.
2. Open a draft integration PR to `main` from the corrected branch. Existing draft PRs #13–#16 remain untouched; do not merge the stack independently and then duplicate it through this integration PR.
3. Freeze the exact source SHA that passes the single `LEGEND v7.2 candidate` workflow. Its checkout, embedded build label, test evidence, APK hash, and signing certificate must refer to that same source. Distribution-only commits must explicitly name the earlier source commit.
4. Evaluate full unit/lint/instrumentation results, five consecutive Key lifecycle passes, exact program snapshot parity, authority hash, privacy, and negative signing gates. Do not turn failed runs into passes by reporting only retries.
5. Complete physical-device acceptance separately. Emulator evidence is never a Galaxy S26 Ultra pass.
6. Before production signing, the owner supplies the four existing `OPERATOR_RELEASE_*` values through the approved secret channel and confirms the production certificate/upgrade lineage and intended distribution channel. No key is generated or borrowed for production.
7. A fresh production APK/AAB must pass certificate, alignment, manifest, version, embedded SHA, installation/update, and program parity verification. The negative signing gate proves refusal only; it is not positive verification of owner signing.
8. Obtain explicit owner approval of that frozen candidate before any merge, version change, tag, store upload, or release. None of those actions is authorized by this correction task.

## Signing and update boundary

`com.jk.operatorperformance` and versionCode 47/versionName 0.38.0 remain unchanged for this debug correction. A newly generated debug certificate does not update the historical debug certificate (`c2c1cef90b518dc623b3acb111d1fdfd557134d40db56570f71571c1701e5df8`). The original private key is not in this repository. Do not uninstall or clear data to bypass this boundary. The production version and certificate lineage require an owner decision before publishing.

## CI consolidation

`.github/workflows/android-ci.yml` is the sole current pipeline. Duplicate v7.1 CI/audit workflows, automatic construction-branch mutation workflows, and their encoded payloads are retired from this branch. They remain recoverable in Git history. The pipeline has read-only repository permissions, no signing secrets, no branch writes, and no merge/publish step. Any external required-check configuration referring to removed job names must be updated by the repository administrator before integration; it cannot be assumed from an unprotected feature branch.

## Remaining acceptance boundaries

- Physical checklist: NOT RUN unless separately evidenced.
- Production private key/certificate lineage: OWNER INPUT REQUIRED.
- Positive release-signing verification: NOT RUN without that input.
- Independent final review: must review the actual corrected source, not an older APK.
- Merge, tag, release, and store listing: NOT AUTHORIZED.
