"""Recomputes every hand-derived number in AI09, and checks lesson Java against the sources."""
import glob
import pathlib
import re

# L2: published table, ratio of model-alone to improved safeguards.
for name, base, improved in [("S4", 13.8, 2.0), ("S4.5", 13.8, 1.9), ("H4.5", 12.0, 2.4), ("O4.5", 8.0, 1.4)]:
    print("L2 ratio", name, round(base / improved, 1))
for p in (0.1, 0.01, 0.001):
    print("L2 p", p, [round(100 * (1 - (1 - p) ** n), 1) for n in (1, 10, 100, 1000)])


def screen(traffic, share, recall, fa):
    attacks = traffic * share
    caught = attacks * recall
    alarms = (traffic - attacks) * fa
    return round(attacks), round(caught), round(attacks - caught), round(alarms), round(100 * caught / (caught + alarms), 1)


print("L5 hook", screen(1e6, 0.001, 0.95, 0.01), "one in", round((950 + 9990) / 950, 1))
print("L5 strict", screen(1e6, 0.001, 0.99, 0.05))
print("L5 lenient", screen(1e6, 0.001, 0.80, 0.001))
print("L5 common", screen(1e6, 0.05, 0.95, 0.01))
print("L5 both", 100 * 0.05 * 0.05)
print("L5 Q3", screen(1e6, 0.0005, 0.90, 0.02), "flags", 450 + 19990)
a = screen(2e6, 0.002, 0.98, 0.03)
b = screen(2e6, 0.002, 0.90, 0.005)
print("E5 A", a, "flags", a[1] + a[3], "cost", (a[1] + a[3]) * 0.01)
print("E5 B", b, "flags", b[1] + b[3], "cost", (b[1] + b[3]) * 0.01)
print("E5 extra caught", a[1] - b[1], "extra cost", round((a[1] + a[3] - b[1] - b[3]) * 0.01, 2))
print("E5 both", 0.02 * 0.10)
print("L1 Luhn pass share", sum(1 for n in range(10000, 110000) if sum(
    (d if i % 2 == 0 else (d * 2 - 9 if d * 2 > 9 else d * 2))
    for i, d in enumerate(map(int, reversed(str(n))))) % 10 == 0) / 100000)

ROOT = pathlib.Path(__file__).resolve().parent.parent
sources = list((ROOT / "code/ai09-security/src").rglob("*.java")) + list((ROOT / "code/ai08-java/src").rglob("*.java"))
code = "\n".join(p.read_text(encoding="utf-8") for p in sources)
lines = {line.strip() for line in code.splitlines()}
for md in sorted(glob.glob(str(ROOT / "02-modules/09-security/notes-*.md"))):
    text = pathlib.Path(md).read_text(encoding="utf-8")
    for block in re.findall(r"```java\n(.*?)```", text, re.S):
        for line in block.splitlines():
            if line.strip() and line.strip() not in lines:
                print(pathlib.Path(md).name, "NOT IN SOURCE:", line.strip())
print("code check done")
