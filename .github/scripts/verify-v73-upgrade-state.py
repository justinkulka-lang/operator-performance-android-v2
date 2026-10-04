"""Compare allowed upgrade migration against untouched session/timer state."""
import sys
import xml.etree.ElementTree as ET

def prefs(path):
    out = {}
    for node in ET.parse(path).getroot():
        if node.tag == "set": value = tuple(sorted(x.text or "" for x in node))
        else: value = node.get("value", node.text)
        out[node.get("name")] = value
    return out
before, after = map(prefs, sys.argv[1:])
allowed = {"v73MigrationApplied", "v73ActivationWeek", "historicalWeekAuthorities"}
assert {k: v for k, v in before.items() if k not in allowed} == {k: v for k, v in after.items() if k not in allowed}, "Upgrade changed existing pointer/session/timer/settings"
assert after.get("v73MigrationApplied") == "true"
assert after.get("v73ActivationWeek") == "2", "Engaged Week 1 must defer activation"
assert "1:V72" in after.get("historicalWeekAuthorities", ())
print("PASS: old session/timers unchanged; V72 Week 1 retained, V73 scheduled for Week 2")
