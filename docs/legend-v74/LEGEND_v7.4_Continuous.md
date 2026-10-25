# LEGEND v7.4 Continuous — Program Authority and Implementation Spec

**Issued:** 10/20/2026
**Author:** Claude (independent auditor)
**Builder:** Cursor
**Owner approval:** On 10/20/2026 the owner wrote: "Do a full scale audit then Go with the recommended." This file is that recommendation, audited. It is approved when the owner hands it to Cursor.

**What this supersedes:**
- `LEGEND_v7.4_Amendment_PROPOSED.md` (Option B, 32-week). Do not implement it.
- In `INDEPENDENT_AUDIT_2026-10-17.md`, items B5 (calibration note), C2 (interference wording; it now lives in §8 of this file) and C3 (pace split; the pace tables are removed).

All other items in that packet still apply: B1–B4, B6, C1 and Part D.

**Base:** the v7.3 allocation (`ea902f79…`), with the changes below. v7.1, v7.2 and v7.3 remain frozen for retained historical weeks.

---

## 1. Intent

LEGEND is a lifestyle program, not preparation for a test. It runs continuously with no end date. It delivers the most hypertrophy the evidence supports alongside the most running the evidence supports without costing muscle.

There are no timed tests, pace targets or finish line:
- **Lifting** progresses by double progression.
- **Running** progresses by effort. As fitness rises and bodyweight falls, the same effort is faster.

---

## 2. Audit of this design

I checked everything below with scripts run against the data in this file. Cursor must re-verify it independently.

| Check | Result |
| --- | --- |
| Weekly totals (checked weeks 1–33, 40, 64 and 65, which span Loops 1–3) | Normal weeks in Blocks I, III and IV: 136 sets. Normal weeks in Block II: 146. Consolidation weeks in Blocks I, III and IV: 59. Consolidation week in Block II: 66. Loop 2 and Loop 3 match Loop 1. |
| Session caps | Never exceeded. Caps are 28/28/24/28/28; Block II caps are 30/30/26/30/30. |
| Direct sets per muscle, normal week | Quads 16, hamstrings 14, glutes 8, chest 15, lats 10, upper back 10, delts 15, biceps 9, triceps 7, calves 13, traps 7, forearms 5, abs 7. That is 14–16 sets for the large muscles, well under the 20–24 sets the owner ruled out. |
| Direct sets per muscle, Block II | Quads 18, hamstrings 16, lats 11, triceps 8, traps 9, forearms 7. Every other muscle stays at its normal-week count. |
| Weekly exposures | Every muscle is trained at least twice a week. Quads, hamstrings, glutes and calves: D2 and D6. Chest, upper back, lats, delts and biceps: D3, D4 and D5. Triceps: D3 and D4. Traps: D3 and D5. Forearms: D4 and D5. Abs: D2, D4 and D6. |
| Interference guard (§8) | Met every week. Lower → hard run: 48 h or more. Lower → Speed: 48 h. Hard run → Speed: 72 h. Running and lower lifting never fall on the same day. |
| Sprint volume | 80–500 m per session. No 600 m sessions remain. |
| Estimated running volume | Roughly 6–9 miles a week, warm-ups and cooldowns included. That is close to the current program's dose; it was not raised. |
| Owner rules | No pull-ups or T-bar rows anywhere. Hip thrust present. Flat barbell bench leads Upper A. Incline barbell press is the second press. Both leg curls are seated. |
| Deloads | A consolidation week every 8 weeks (the backstop), plus owner-triggered early consolidation (§6). The app stores no performance data. |
| Remaining known trade-offs | Upper-body work stays clustered on D3–D5. Review it if Day 5 overhead press or pulldown stalls for two straight weeks. Day 4 is the longest session. In Block II, chest, delts and biceps get no peak sets; this keeps the 146 cap during the owner's cut to 215–220 lb. |

---

## 3. Continuous structure

**Loop:** 32 weeks, made of four 8-week blocks, repeated forever. The week counter never resets. For example, Week 33 is Loop 2 · Block I · Week 1.

| Block | Display | Resistance | Weeks 1–7 | Week 8 |
| --- | --- | --- | --- | --- |
| I | I · Draft | Base allocation | 136 sets | Consolidation, 59 sets |
| II | II · Build | Peak allocation | 146 sets | Consolidation, 66 sets |
| III | III · Backstop | Base allocation, heavier main lifts (§4) | 136 sets | Consolidation, 59 sets |
| IV | IV · Live | Base allocation, two rotated lifts (§4) | 136 sets | Consolidation, 59 sets |

**Consolidation week:** halve each slot's sets for that block, round down, minimum one set. Use 3–4 RIR at the same loads, and keep rep ranges unchanged.

The **running cycle (§5)** follows the block week, so every block runs the same 8-week cycle.

**Day order is unchanged:**
- D1: Engine alone
- D2: Lower A
- D3: Upper A
- D4: Speed, cooldown, then Upper C
- D5: Upper B, then easy aerobic
- D6: Lower B
- D7: Rest

**Removed for v7.4:**
- Week 30–31 realization
- the Week 32 final-test overlay
- the "program complete" state
- the 224-day and "no Week 33" invariants
- timed baselines, checkpoints and finals
- the test eligibility gate

---

## 4. Resistance allocation

Exercises, sets, reps and RIR match v7.3 except for these changes:
- **Peak block:** the peak moves from Block III to Block II.
- **Weak-point sets:** traps and forearms receive part of the peak. They take the peak sets formerly assigned to D3 lat row, D5 incline DB press, D4 lateral raise and D4 incline curl.
- **Hip thrust:** its peak set moves to the D6 leg curl. Glutes are already dominant, and the owner rejected glute priority.
- **Day 6 leg curl:** lying becomes seated.
- **Prohibited alternatives:** pull-up and T-bar row are replaced.
- **Block III:** back squat, flat bench and standing overhead press at 4–6 reps; RDL at 6–8.
- **Block IV:** back squat is performed as safety-bar squat, and leg press as hack squat. The original lift becomes the rotated lift's first alternative. Safety-bar squat alternatives: back squat, front squat, Smith squat. Hack squat alternatives: leg press, Smith squat, heel-elevated DB squat.

Rest is 2–4 min on compounds (1–2 RIR rows) and 1–2 min on isolations. Unilateral sets count both sides; reps are per side.

| # | Day | Movement | Sets I/III/IV | Sets II | Consol. I/III/IV | Consol. II | Reps | Reps III | RIR | Rest | Form cue | Ranked alternatives | Home alternate |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | D2 Lower A | Back squat (IV: Safety-bar squat) | 4 | 4 | 2 | 2 | 6–8 | 4–6 | 1–2 | 2–4 min | — | 1 Safety-bar squat; 2 front squat; 3 Smith squat | Barbell back/front squat |
| 2 | D2 Lower A | Leg press (IV: Hack squat) | 3 | 4 | 1 | 2 | 10–12 | same | 1–2 | 2–4 min | — | 1 hack squat; 2 Smith squat; 3 heel-elevated DB squat | Heel-elevated DB squat; different loading curve |
| 3 | D2 Lower A | Leg extension | 4 | 4 | 2 | 2 | 12–15 | same | ≥1 | 1–2 min | — | 1 seated band knee extension; 2 reverse Nordic; 3 Spanish squat | Seated band knee extension, hip flexed; different resistance curve |
| 4 | D2 Lower A | Seated leg curl | 5 | 6 | 2 | 3 | 10–15 | same | ≥1 | 1–2 min | Hips flexed; full stretch at the bottom. | 1 seated band leg curl; 2 lying leg curl; 3 slider curl | Seated band leg curl anchored to rack, hips flexed; lying/slider alternatives lose hip-flexed bias |
| 5 | D2 Lower A | Glute-biased 45° hip extension | 4 | 4 | 2 | 2 | 10–15 | same | ≥1 | 1–2 min | Glute-biased; hips drive the movement. | 1 cable pull-through; 2 glute-biased DB RDL; 3 glute-biased good morning | Glute-biased DB RDL; unsupported, different resistance curve |
| 6 | D2 Lower A | Standing calf raise | 5 | 5 | 2 | 2 | 8–12 | same | ≥1 | 1–2 min | Full stretch at the bottom. | 1 Smith calf raise; 2 leg-press calf raise; 3 DB standing calf raise | DB standing calf raise off block, rack support |
| 7 | D2 Lower A | Weighted crunch | 3 | 3 | 1 | 1 | 10–15 | same | 1 | 1–2 min | — | 1 cable crunch; 2 band crunch; 3 machine crunch | Weighted crunch |
| 8 | D3 Upper A | Flat barbell bench press | 4 | 4 | 2 | 2 | 6–8 | 4–6 | 1–2 | 2–4 min | — | 1 DB bench; 2 Smith bench; 3 machine chest press | Barbell/DB bench |
| 9 | D3 Upper A | Chest-supported upper-back row | 4 | 4 | 2 | 2 | 8–10 | same | 1–2 | 2–4 min | Elbows 45–60° from the torso. | 1 chest-supported DB row; 2 wide-elbow cable row; 3 barbell row | Chest-supported DB row, elbows 45–60° |
| 10 | D3 Upper A | Incline barbell press | 4 | 4 | 2 | 2 | 8–10 | same | 1–2 | 2–4 min | — | 1 incline DB press; 2 Smith incline; 3 machine incline | Incline barbell/DB press |
| 11 | D3 Upper A | Lat-biased one-arm cable row | 3 | 3 | 1 | 1 | 10–12 | same | 1–2 | 2–4 min | Drive the elbow toward the hip. | 1 one-arm DB row; 2 tucked-elbow chest-supported row; 3 one-arm band row | One-arm DB row, elbow toward hip |
| 12 | D3 Upper A | DB lateral raise | 3 | 3 | 1 | 1 | 12–20 | same | 0–1 | 1–2 min | — | 1 cable lateral; 2 machine lateral; 3 band lateral | DB lateral raise |
| 13 | D3 Upper A | Overhead triceps extension | 3 | 4 | 1 | 2 | 10–15 | same | 0–1 | 1–2 min | Upper arm stays overhead. | 1 overhead DB extension; 2 overhead EZ-bar extension; 3 overhead band extension | Overhead DB/EZ extension; shoulder stays flexed |
| 14 | D3 Upper A | Incline DB curl | 4 | 4 | 2 | 2 | 10–15 | same | 0–1 | 1–2 min | Shoulder stays extended behind the torso. | 1 behind-body cable curl; 2 behind-body band curl | Incline DB curl; shoulder remains extended |
| 15 | D3 Upper A | Paused DB shrug | 3 | 4 | 1 | 2 | 10–15 | same | 0–1 | 1–2 min | One-second pause at the top. | 1 barbell shrug; 2 Smith shrug; 3 cable shrug | DB/barbell shrug |
| 16 | D4 Upper C | Cable fly | 4 | 4 | 2 | 2 | 12–15 | same | 0–1 | 1–2 min | Controlled, pain-free stretch. | 1 DB fly; 2 pec deck; 3 band fly | DB fly, controlled pain-free stretch; different loading curve |
| 17 | D4 Upper C | DB pullover | 2 | 2 | 1 | 1 | 10–15 | same | 0–1 | 1–2 min | Controlled, pain-free stretch. | 1 cable pullover; 2 straight-arm pulldown; 3 band pullover | DB pullover; cable/pulldown loading curves differ |
| 18 | D4 Upper C | Chest-supported upper-back row | 3 | 3 | 1 | 1 | 10–12 | same | 1–2 | 2–4 min | Elbows 45–60° from the torso. | 1 chest-supported DB row; 2 wide-elbow cable row; 3 supported band row | Chest-supported DB row, elbows 45–60° |
| 19 | D4 Upper C | DB lateral raise | 2 | 2 | 1 | 1 | 12–20 | same | 0–1 | 1–2 min | — | 1 cable lateral; 2 machine lateral; 3 band lateral | DB lateral raise |
| 20 | D4 Upper C | Chest-supported rear-delt fly | 3 | 3 | 1 | 1 | 12–20 | same | 0–1 | 1–2 min | — | 1 reverse pec deck; 2 cable rear-delt fly; 3 band rear-delt fly | Chest-supported DB rear-delt fly |
| 21 | D4 Upper C | Incline DB curl | 2 | 2 | 1 | 1 | 10–15 | same | 0–1 | 1–2 min | Shoulder stays extended behind the torso. | 1 behind-body cable curl; 2 behind-body band curl | Incline DB curl; shoulder remains extended |
| 22 | D4 Upper C | Overhead triceps extension | 4 | 4 | 2 | 2 | 10–15 | same | 0–1 | 1–2 min | Upper arm stays overhead. | 1 overhead DB extension; 2 overhead EZ-bar extension; 3 overhead band extension | Overhead DB/EZ extension; shoulder stays flexed |
| 23 | D4 Upper C | DB wrist curl | 2 | 4 | 1 | 2 | 12–20 | same | 0–1 | 1–2 min | — | 1 barbell wrist curl; 2 cable wrist curl; 3 band wrist curl | Supported DB/barbell wrist curl |
| 24 | D4 Upper C | Cable crunch | 2 | 2 | 1 | 1 | 10–15 | same | 1 | 1–2 min | — | 1 weighted crunch; 2 band crunch | Weighted/band crunch; resistance curves differ |
| 25 | D5 Upper B | Standing overhead press | 3 | 3 | 1 | 1 | 6–8 | 4–6 | 1–2 | 2–4 min | — | 1 seated DB press; 2 standing DB press; 3 Smith overhead press | Barbell/DB overhead press |
| 26 | D5 Upper B | Lat pulldown | 5 | 6 | 2 | 3 | 8–12 | same | 1–2 | 2–4 min | — | 1 single-arm cable pulldown; 2 machine pulldown; 3 seated band pulldown | Seated or kneeling band pulldown anchored high on the rack; overhead stretch retained |
| 27 | D5 Upper B | Chest-supported upper-back row | 3 | 3 | 1 | 1 | 8–12 | same | 1–2 | 2–4 min | Elbows 45–60° from the torso. | 1 chest-supported DB row; 2 wide-elbow cable row; 3 supported band row | Chest-supported DB row, elbows 45–60° |
| 28 | D5 Upper B | Incline DB press | 3 | 3 | 1 | 1 | 10–12 | same | 1–2 | 2–4 min | — | 1 incline barbell press; 2 Smith incline; 3 machine incline | Incline DB/barbell press |
| 29 | D5 Upper B | DB lateral raise | 2 | 2 | 1 | 1 | 12–20 | same | 0–1 | 1–2 min | — | 1 cable lateral; 2 machine lateral; 3 band lateral | DB lateral raise |
| 30 | D5 Upper B | Chest-supported rear-delt fly | 2 | 2 | 1 | 1 | 12–20 | same | 0–1 | 1–2 min | — | 1 reverse pec deck; 2 cable rear-delt fly; 3 band rear-delt fly | Chest-supported DB rear-delt fly |
| 31 | D5 Upper B | DB hammer curl | 3 | 3 | 1 | 1 | 10–15 | same | 0–1 | 1–2 min | — | 1 rope hammer curl; 2 cross-body DB curl; 3 band neutral-grip curl | DB hammer curl |
| 32 | D5 Upper B | DB reverse wrist curl | 3 | 3 | 1 | 1 | 12–20 | same | 0–1 | 1–2 min | — | 1 barbell reverse wrist curl; 2 cable reverse wrist curl; 3 band reverse wrist curl | Supported DB/barbell reverse wrist curl |
| 33 | D5 Upper B | Paused DB shrug | 4 | 5 | 2 | 2 | 10–15 | same | 0–1 | 1–2 min | One-second pause at the top. | 1 barbell shrug; 2 Smith shrug; 3 cable shrug | DB/barbell shrug |
| 34 | D6 Lower B | Barbell Romanian deadlift | 4 | 4 | 2 | 2 | 8–10 | 6–8 | 1–2 | 2–4 min | — | 1 DB RDL; 2 Smith RDL; 3 band RDL | Barbell/DB RDL |
| 35 | D6 Lower B | Barbell hip thrust | 4 | 4 | 2 | 2 | 8–12 | same | 1–2 | 2–4 min | Squeeze hard at lockout. | 1 Smith hip thrust; 2 DB hip thrust; 3 band hip thrust | Barbell/DB hip thrust; shortened-position emphasis retained |
| 36 | D6 Lower B | Bulgarian split squat | 3 | 3 | 1 | 1 | 8–10/side | same | 1–2 | 2–4 min | Knee travels forward; reps per side. | 1 Smith split squat; 2 DB split squat; 3 reverse lunge | DB Bulgarian split squat; knee-forward quad emphasis |
| 37 | D6 Lower B | Seated leg curl | 5 | 6 | 2 | 3 | 10–15 | same | ≥1 | 1–2 min | Hips flexed; full stretch at the bottom. | 1 seated band leg curl; 2 lying leg curl; 3 slider curl | Seated band leg curl anchored to rack, hips flexed; lying/slider alternatives lose hip-flexed bias |
| 38 | D6 Lower B | Leg extension | 2 | 3 | 1 | 1 | 12–15 | same | ≥1 | 1–2 min | — | 1 seated band knee extension; 2 reverse Nordic; 3 Spanish squat | Seated band knee extension, hip flexed; different resistance curve |
| 39 | D6 Lower B | Seated calf raise | 8 | 8 | 4 | 4 | 10–15 | same | ≥1 | 1–2 min | Knees bent; full stretch at the bottom. | 1 seated DB calf raise; 2 bent-knee Smith calf raise; 3 seated barbell calf raise | Seated DB calf raise, knees bent, block under forefeet |
| 40 | D6 Lower B | Hanging leg raise | 2 | 2 | 1 | 1 | 10–15 | same | 1 | 1–2 min | No low-back arch. | 1 captain’s-chair raise; 2 reverse crunch; 3 lying leg raise | Rack hanging leg raise or reverse crunch; avoid lumbar extension |

**Session totals**

| Day | Normal week I/III/IV | Normal week II | Consolidation I/III/IV | Consolidation II | Cap I/III/IV | Cap II |
| --- | --- | --- | --- | --- | --- | --- |
| D2 Lower A | 28 | 30 | 12 | 14 | 28 | 30 |
| D3 Upper A | 28 | 30 | 12 | 14 | 28 | 30 |
| D4 Upper C | 24 | 26 | 11 | 12 | 24 | 26 |
| D5 Upper B | 28 | 30 | 11 | 12 | 28 | 30 |
| D6 Lower B | 28 | 30 | 13 | 14 | 28 | 30 |
| Total | 136 | 146 | 59 | 66 | 136 | 146 |

**RIR rules (unchanged):**
- Compounds: 1–2 RIR
- Upper-body isolation: 0–1 RIR
- Lower-body isolation: at least 1 RIR
- Trunk: 1 RIR
- No mandatory failure or intensity techniques.

---

## 5. Running cycle (repeats every block)

Every session comes from approved v7.2 rows. Goal-pace and percentage targets are converted to effort.

| Block week | D1 Engine | D4 Speed | Sprint m | D5 easy aerobic | Jumps | Throws |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 3 × 6 min threshold; 2 min easy | 6 × 20 m acceleration; 3 min recovery | 120 | Bike 25 min easy | — | — |
| 2 | 5 × 3 min controlled interval; 2 min easy | 6 × 30 m acceleration; 3–4 min recovery | 180 | Easy run 20 min or bike 30 min | — | — |
| 3 | 20 min continuous threshold | 4 × (20 m build + 20 m fly); 5 min recovery | 80 | Bike 30 min easy | Yes | Yes |
| 4 | 6 × 400 m controlled interval; 90 s easy | 8 × 30 m acceleration; 3–4 min recovery | 240 | Easy run 20 min | Yes | Yes |
| 5 | 3 × 1 mile threshold; 1 min easy | 3 × 150 m at about 90% effort; 5–6 min recovery | 450 | Bike 30 min easy | Yes | — |
| 6 | 4 × 1000 m controlled interval; 2 min easy | 5 × (20 m build + 20 m fly); 5 min recovery | 100 | Easy run 22 min | Yes | Yes |
| 7 | 6 × 400 m at hard, even 1.5-mile effort; 90 s easy | 2 × 250 m at about 90–93% effort; 7–8 min recovery | 500 | Easy run 25 min | Yes | — |
| 8 (consolidation) | Easy run 25 min; easy-only warm-up | 4 × 20 m relaxed acceleration; 3 min recovery | 80 | Bike 20 min easy | — | — |

**Jumps:** the existing v7.2 plyometrics row, after the Speed warm-up.

**Throws:** the existing v7.3 throws row, placed after the jumps and before the sprints. No throws in special-endurance weeks (5 and 7).

**Effort guide:**

| Session type | Effort |
| --- | --- |
| Easy | RPE 2–3, conversational |
| Threshold | RPE 7–8, short phrases only, even pace |
| Controlled interval | RPE 8; the last rep matches the first |
| 1.5-mile effort | RPE 8–9, hard but repeatable |
| Speed | Full recovery between reps; quality over quantity |

Warm-ups and cooldowns are unchanged from v7.3. The 110% easy-run rule is retired: easy runs are fixed at 20–25 minutes and do not escalate.

---

## 6. Autoregulation

1. **Backstop:** every block week 8 is a consolidation week.
2. **Consolidate now (new control).** The owner can turn the current week into that block's consolidation week, at an unstarted Day 1 only, after a confirmation step.
   - The following week starts the next block at Week 1.
   - The running cycle follows to its week 8.
3. **Set removal (existing rule):** if a movement declines more than 5% on two consecutive exposures, remove one set from it at the next exposure. Restore the set when performance returns. The app does not do this automatically.

---

## 7. What the app shows (text cleanup)

**Each exercise row shows:**
- sets × reps @ RIR
- rest time
- the §4 form cue, when one exists
- ranked alternatives, with the home alternate labeled "Home"

**Each running row shows:**
- the prescription
- its effort line from §5

**One global line stays:** "Stop any exercise that causes pain; don't replace the stopped reps."

**Remove from all v7.4 screens, rows and Tools notes:**
- medical, clearance and participation text
- injury-threshold disclaimers and the 600 m workload warning
- "record outside the app" and "the app does not store" notices
- authority-limits text
- calibration text and calibration correction
- the 300 m anchor formula
- goal-pace tables and percentage-velocity notes
- the test eligibility gate
- "MISSING" classifications
- the long consolidation and realization overlay sentences; use the label "Consolidation week · half sets · 3–4 RIR" instead
- fractional-credit displays

**Tools → Notes keeps four short sections:**
- Week structure
- Progression: double progression, load increments, RIR
- Autoregulation (§6)
- Running effort guide (§5)

---

## 8. Interference guard (authority text; the schedule already satisfies it)

- Lower-body resistance → hard run: at least 24 hours.
- Lower-body resistance → Speed: at least 6–12 hours.
- Hard run → Speed: at least 24 hours.
- A running session and a lower-body resistance session on the same day: at least 6 hours apart.

Day 4 (Speed, then Upper C) and Day 5 (Upper B, then easy aerobic) are owner-sequenced and stand as written.

This text appears in the authority only, not in the app.

---

## 9. Implementation (Cursor)

**Rules from the 10/17 packet apply:**
- Verify against the current head.
- Sum every table independently.
- Leave frozen files untouched.
- Work on a branch, open a PR, do not merge.
- Report evidence per Part D4.
- Commit this file unchanged as `docs/legend-v74/LEGEND_v7.4_Continuous.md`, together with an `OWNER_APPROVAL.md` that quotes the approval line above.

**Branch order:**
1. Packet Part B (except B5).
2. This program, on `cursor/legend-v74-continuous`.
3. §10 design, on its own branch, only after (2) is merged.

### State model

- Pointer = `absoluteWeek` (unbounded, starting at 1), `block` (1–4), `blockWeek` (1–8), `day` (1–7). `loop` is derived as the count of completed Block IV consolidation weeks plus one.
- Normal advance: Day 7 → next week. After blockWeek 8 the block increments; after Block IV, `block` returns to 1.
- Remove every `require(week in 1..32)`, every `TOTAL_WEEKS` and 224-day bound, and the end-of-program screen, for V74.
- Key historical authority by `absoluteWeek` with no upper bound.

### Migration

- Add `ProgramAuthority.V74`.
- Activate at the next positively unstarted Day 1. Engaged v7.3 weeks keep v7.3.
- A v7.3 week N maps to `absoluteWeek` N, `block` = ⌈N ÷ 8⌉, `blockWeek` = ((N − 1) mod 8) + 1.
- Freeze the v7.3 sources in CI once V74 lands.

### Display

- Header: "Week 41 · II · Build · Week 1 of 8". Show "Loop 2" small on the Program screen only.
- The Program screen browses one loop (Blocks I–IV).
- Block copy stays neutral:
  - I "Base volume."
  - II "Peak volume."
  - III "Heavier main lifts."
  - IV "Rotated lifts."

### Tests

- Totals equal §3 for weeks 1–96, with Loop 2 and Loop 3 identical to Loop 1.
- Caps hold every week.
- The per-muscle direct counts in §2 hold.
- Block III reps and Block IV names and alternatives are correct.
- No row title or alternative contains "pull-up" or "T-bar" in any week.
- Running rows equal §5 for every block week.
- Throws appear only in block weeks 3, 4 and 6; jumps only in weeks 3–7.
- No V74 text contains any §7 removed item. Check this with a string blocklist, including "MISSING", "goal pace", "anchor", "calibration", "validated injury" and "outside the app".
- Consolidate-now:
  - It is only available at an unstarted Day 1.
  - It sets the week to block week 8.
  - The next week is block +1, week 1.
  - It survives process death and reboot.
- Advancing past Week 32 produces Week 33 = Block I Week 1, with no crash and no end screen. Repeat at Week 64.
- Migration:
  - An engaged v7.3 week is retained.
  - The next unstarted Day 1 resolves to V74.
  - The v7.1, v7.2 and v7.3 snapshots are byte-identical.

---

## 10. Visual identity (separate branch, after §9 merges)

**Concept:** The gentleman spy. A Cold War case officer's study: leather, walnut, brass, tobacco and linen paper. It should feel quiet, old and expensive. The LEGEND tradecraft identity stays; this warms and dresses it.

**Palette** (contrast verified against WCAG AA):

| Role | Color | Contrast |
| --- | --- | --- |
| Background (espresso) | #15110D | — |
| Surface | #211A14 | — |
| Primary text (bone) | #E8DFCE | 14.2:1 on background; 13.0:1 on surface |
| Secondary text | #B5A892 | 7.3:1 on surface |
| Accent (aged brass) | #B08D57 | 6.1:1 on background; 5.6:1 on surface |
| Oxblood | #6E2424 | Fill for the current-day band and phase marker only; bone text on it is 8.2:1 |
| Olive | #6F7250 | 3.8:1; non-text marks only, as the single tactical nod |

**Type:** bundle all fonts offline (the app has no Internet permission), and include each OFL license.
- **Titles and day names:** an old-style serif, Libre Caslon Text or EB Garamond.
- **Labels:** small caps.
- **Sets, reps and timers:** the existing monospace.

**Signature motif: the cigar band.**
- The current day's card carries a full-width oxblood band with thin brass rules above and below.
- The day name sits in bone small caps.
- Only today gets the band.

**Details:**
- Roman-numeral phases.
- Hairline brass dividers.
- Day icons redrawn as stamped hallmarks, keeping the map-key "Legend" screen.
- App icon: an engraved monogram "L" inside a brass ring.

**Copy voice:** dry and understated. No exclamation points and no slogans. Keep the tagline "Built to hold."

**Avoid:**
- literal cigars, pipes or smoke
- whiskey glasses or mustaches
- wood-grain or stitched-leather textures
- skulls, flags, camo, stencil fonts, aggressive slogans
- anything resembling an existing brand mark

**Acceptance:**
- AA contrast at 2.0 font scale and in landscape.
- No layout regression in the existing emulator suite.
- Screenshots of Today, Week, Workout and Program attached to the PR.
