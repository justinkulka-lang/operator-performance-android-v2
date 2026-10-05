# v7.2 mixed Speed reference correction

Owner-authorized correction against the approved v7.2 DOCX. Its SHA-256 remains `25a7ce497569913c4a56c057bcaa139290adddf20876a2d736790d5fefd436f3`.

Weeks 13 and 20 resolve four 30 m accelerations, followed by three 20 m fly repetitions (180 m). Weeks 28 and 30 resolve four 30 m accelerations, followed by two 20 m fly repetitions (160 m). The original full-recovery suffix, including week 30's no-jumps instruction, is carried into both reference groups. No approach distance or repetitions are added. The original combined authority prescription remains in the conditioning table; its aggregate effort count is corrected.

Only these four resolved sessions change. Resistance, other conditioning sessions, warm-ups, cooldowns, sprint cap/warnings, eligibility, and authority bytes are unchanged. No v7.3 work is included.

Session-level completion is unchanged. No repetition checklist, completion gate, per-effort persistence, or new state is introduced. Existing active/completed records keep their status, pointer and timer. Obsolete effort fields remain ignored/cleared by the existing normalization. Old combined Speed ids have no authorized substitutions and are not mapped to completed repetitions in the new groups.

Regression coverage: `V72MixedSpeedReferenceTest` verifies ordered groups/counts/distances, total sprint meters, recovery/no-jumps text, retained stop rules/cooldown, unaffected weeks, and legacy-state normalization. The existing frozen snapshot fixture is byte-identical to the base. `ProgramSnapshotTest` explicitly permits only the four authorized row splits and compares every other resolved field across all 448 v7.1/v7.2 sessions. Its expected row count rises from 2773 to 2777. Reports retain the original baseline, approved corrected baseline, and actual snapshot.

CI's source-identity check permits these two source files only at their pinned corrected SHA-256 values. The remaining program/state source freeze, DOCX digest check, full unit suite, lint/build and existing emulator/signing/package checks remain enabled. Tests/build run remotely through that workflow because this workspace has no Gradle or Android SDK; exact-commit CI results must be inspected before claiming success.

Changed files: ProgramV72.kt, SessionResolverV72.kt, V72MixedSpeedReferenceTest.kt, ProgramSnapshotTest.kt, android-ci.yml, and this report. The snapshot test was modified to recognize the authorized reference correction; no training-dose assertion was weakened.

This is a draft correction for review. No merge, tag, release, package/signing change, or production-readiness claim is authorized.
