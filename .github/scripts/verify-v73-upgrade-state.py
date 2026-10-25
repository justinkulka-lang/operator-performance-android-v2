"""Compare allowed upgrade migration against untouched session/timer state."""
import re
import sys
import xml.etree.ElementTree as ET

# SharedPreferences stores the substitution separator as U+001F. XML 1.0 rejects
# that character, so the seed writes &#x1f; and Android rewrites it as &#31;.
# Expat refuses both. A private-use stand-in survives parsing and is restored
# before the substitution is compared.
_UNIT_SEPARATOR_REF = re.compile(r"&#(?:0*31|x0*1[fF]);")
_UNIT_SEPARATOR_STANDIN = "\ue000"

def prefs(path):
    text = _UNIT_SEPARATOR_REF.sub("&#xE000;", open(path, encoding="utf-8").read())
    out = {}
    for node in ET.fromstring(text):
        if node.tag == "set": value = tuple(sorted(restore(x.text or "") for x in node))
        else: value = restore(node.get("value", node.text))
        out[node.get("name")] = value
    return out

def restore(value):
    if value is None: return None
    return value.replace(_UNIT_SEPARATOR_STANDIN, "\u001f")
before, after = map(prefs, sys.argv[1:3])
mode = sys.argv[3] if len(sys.argv) > 3 else "from-v72"
if mode == "from-v73":
    for key in ("currentWeek", "currentDay", "authorityVersion", "workoutActive", "workoutWeek", "workoutDay", "workoutAuthority", "substitutions"):
        assert before.get(key) == after.get(key), f"{key} changed: {before.get(key)!r} -> {after.get(key)!r}"
    assert after.get("currentWeek") == "12" and after.get("currentDay") == "3"
    assert after.get("v74ActivationWeek") == "13", "Week 12 Day 3 must activate V74 at Week 13"
    assert "12:V73" in after.get("historicalWeekAuthorities", ())
    print("PASS: v7.3 Week 12 Day 3 workout and substitution unchanged; V74 scheduled for Week 13")
else:
    allowed = {"v73MigrationApplied", "v73ActivationWeek", "historicalWeekAuthorities", "v74MigrationApplied", "v74ActivationWeek"}
    assert {k: v for k, v in before.items() if k not in allowed} == {k: v for k, v in after.items() if k not in allowed}, "Upgrade changed existing pointer/session/timer/settings"
    assert after.get("v73MigrationApplied") == "true"
    assert after.get("v73ActivationWeek") == "2", "Engaged Week 1 must defer activation"
    assert "1:V72" in after.get("historicalWeekAuthorities", ())
    assert after.get("v74MigrationApplied") == "true"
    assert after.get("v74ActivationWeek") == "2", "Engaged Week 1 must defer V74 activation"
    print("PASS: old session/timers unchanged; V72 Week 1 retained, V73 and V74 scheduled for Week 2")
