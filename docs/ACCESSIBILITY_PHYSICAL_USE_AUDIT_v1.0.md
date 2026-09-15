# ARES Performance — Accessibility & Physical-Use Audit v1.0

Scope: Android reference-first app only. This audit does not alter ARES v5.7 training content and does not introduce health, readiness, performance logging, analytics, or network behavior.

## Acceptance standard

ARES is used while training, often one-handed, under fatigue, with sweat, variable lighting, and intermittent attention. The physical-use standard therefore prioritizes fast recognition, large targets, persistent primary controls, recoverable state, and screen-reader-readable execution state.

## Automated acceptance coverage

- Bottom navigation retains four destinations only: TODAY / WEEK / PROGRAM / TOOLS.
- Primary navigation targets are at least 48 dp high.
- Workout Mode primary SET DONE / NEXT EFFORT action remains in a persistent bottom bar.
- Primary Workout Mode control remains reachable at 2.0 system font scale on a compact 360 × 640 test display.
- Program SEARCH / JUMP remains reachable at 2.0 system font scale on the same compact display.
- Workout reset control is at least 48 dp high and requires explicit confirmation.
- Rest timers publish TalkBack content description, state description, and polite live-region updates.
- Workout Map rows expose explicit completed / current / upcoming semantics rather than relying only on color or glyph shape.
- Program search results and session cards are large press targets and open the canonical session reference directly.
- Active workout completion state, substitutions, undo state, and active workout rest timer survive process/app restart through the existing local state store.
- Resetting a workout clears execution state only; it does not modify the current program pointer or training prescription.
- Workout Mode keeps the display awake only while the execution surface is active.
- Haptic feedback supplements the primary in-workout completion action and timer completion; it is not the sole status signal.

## Program search / jump boundaries

Search is local and offline. It indexes only Weeks 1–32 from the canonical ARES program resolver using the currently selected Foundation Speed / Engine reference where v5.7 permits manual reference selection. Search does not create, infer, or expose Week 33+ content. Manual deload mode is deliberately excluded from the search index so search results remain a reference to the populated program rather than an adaptive output.

Supported search examples include compact week/day syntax (`W17`, `D3`, `W17 D3`) and plain-language prescription terms such as `300 m`, `threshold`, or an exercise name. Quick-jump chips expose the current week plus phase boundaries and Week 32.

## Session-state ergonomics

TODAY displays a SAVED WORKOUT banner whenever the current session has persisted execution state. The user can resume directly after app restart. A separate RESET TODAY'S WORKOUT action requires confirmation and clears only local execution state for the current session: completion marks, selected substitutions, undo state, and active workout rest timer. Program pointer changes continue to clear stale workout execution state.

## Build / authority integrity

TOOLS contains a read-only BUILD / AUTHORITY INTEGRITY card showing app version, versionCode, package, embedded source commit, ARES authority version and SHA-256, build type, and offline/local-only status. This is reference information only and does not phone home.

CI and Master Audit additionally require:

- source package/version/authority verification;
- APK-level package/version verification;
- exact commit embedded in the debug APK;
- rejection of partially configured owner signing credentials;
- unsigned release acceptance only while persistent owner signing is not configured;
- once owner signing is configured, a required pinned `OPERATOR_RELEASE_CERT_SHA256` certificate fingerprint and exact match against `apksigner` output;
- APK SHA-256 provenance files bound to the GitHub commit, package, version, authority SHA-256, signing state, and signing certificate fingerprint;
- offline/privacy/stale-architecture scans and the existing process-death acceptance suite.

## Deferred until owner signing setup

The permanent owner signing certificate cannot be pinned until the owner generates the keystore on an owner-controlled computer. Once generated, the certificate SHA-256 should be saved as the repository secret `OPERATOR_RELEASE_CERT_SHA256` alongside the four existing signing secrets. The first permanent signed lineage should then receive the separately approved version bump and a full CI + Master Audit certification pass.

## Non-goals

This pass does not add bodyweight, calories/macros, readiness, sleep, HR/HRV, pace/results, loads/reps/RIR logging, benchmark storage, Health Connect, cloud accounts, notifications, analytics, automatic progression, or any additional training prescription.
