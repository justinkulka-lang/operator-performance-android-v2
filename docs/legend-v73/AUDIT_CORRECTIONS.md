# LEGEND v7.3 audit corrections — candidate, not release

Baseline audited source: e64f55ab21c3d8ac92edc8e61b8fc4fcc0f2cd45. Changes below preserve the accepted authority bytes, allocation, conditioning, migration boundaries and historical prescriptions.

## Implemented corrections awaiting exact-head CI

- A01: plate items reject nonfinite/out-of-range weights and counts outside 1–20. The UI admits at most 12 types, bounds target/equipment/collar values and reports invalid input. Legacy invalid entries are excluded with an explicit warning; loading does not erase raw preferences. The solver uses a bounded-knapsack search with at most 250,001 units per layer, gcd normalization, target-bound pruning and cancellation checks. Calculation runs on Dispatchers.Default, outside composition, and stale results cannot be displayed for changed inputs. Tests compare nearest/exact results with exhaustive small inventories, verify reconstructed counts, exercise the maximum accepted search and cancellation, reject oversized/complex inputs, and check entered/stored invalid counts in the packaged UI.
- A03: two new packaged-workout tests explicitly exercise Week 17 Day 2 (30 resistance sets) and Day 4 (26 resistance sets, plus the approved jump/throw/Speed prelude) at 2.0 font in portrait and landscape. They scroll to the last prescription and substitution controls, start a real manual timer, recreate/resume without losing the timer or selected substitution, and reach completion/return actions. Compose-root captures are retained for preparation, final rows, timers and end controls.
- A04: resolver identities classify plyometrics and added throws as Speed preparation for both the focus line and row symbol. All authorities and all 32 Speed days have regression assertions; no prescription is altered.

## A02 remains dependent on exact current authority wording

The accepted packet names 24-hour / 6–12-hour / 6-hour interference guidance but does not map those windows to exposures. The supplied v7.1 DOCX, frozen v7.2 authority and 4 October review do not resolve the missing contexts. Historical conversation retrieval found differing generations: a user-authorized 12-hour lower/Speed and 24-hour hard-run/lower and hard-run/Speed reference, later assistant reports of a directional 48-hour Speed-to-lower rule, and a user instruction that the six-hour concept is documentation only, never a new schedule requirement or doubles generator. Those records do not justify silently declaring a new current 6–12-hour rule. The owner selected supplying exact wording; no actual wording has yet been provided. Keep A02 open until the relevant text or an explicit amendment is supplied. Do not change training prescriptions to guess this item closed.

## Verification and signing boundary

Declared new counts: 116 JVM @Test methods and 22 full-suite instrumented methods, including the two host-mode methods that return without assertions in the ordinary suite. Counts and passes must be recorded from the new matching CI XML, not inferred from declarations.

Debug builds on fresh CI runners use generated debug certificates. These changes do not invent a production signer, expose private keys, clear phone data, change the package, or claim compatibility with the historically installed app. Positive owner signing, installed-certificate continuity and physical Galaxy S26 Ultra acceptance remain separate gates. No merge, tag or release is authorized.
