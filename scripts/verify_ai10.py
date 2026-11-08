"""Recomputes every hand-derived number in AI10, and checks lesson Java against the sources."""
import glob
import pathlib
import re

M = 1e6
print("L1 multipliers", 3 * 3, 3 * 3 * 3, "recovered", 9010 - 8000, 8032 - 8000)
print("L2 Haiku vs Opus task", round(0.0268 / 0.0009))
A, B = 0.002, 0.01
print("L2 Q2 retry", A / 0.8, B / 0.98, "person", A + 0.2 * 2, B + 0.02 * 2)
print("L3 handwritten share", round((198 + 202 + 197) / 20000 * 100, 1))
print("L4 turn3 share", round(0.03 / 0.0446 * 100), "tool share", round(30000 / 41200 * 100))
print("L4 factor", round(0.0241 / 0.0041, 1))
print("L5 report share", round(100 / 1400 * 100, 1), "spend share", round(20 / (4.68 + 20 + 6.64) * 100))
print("L5 globex vs acme", round(0.0166 / 0.0052, 1))
print("L5 call", 4000 * 2 / M + 500 * 10 / M)
print("L6 downtime ratio", round((1 - .99999) / (1 - .99999) , 1), round((1 - 0.999799) / (1 - 0.9999990), 0))
print("L6 Q2 weeks", 8146 * 2 / 2000)
# L7 estimation
day = 5e6
avg = day / 86400
peak = 289
print("L7 avg", round(avg, 1), "peak", round(avg * 5), "conc", 289 * 12, "ops", 289 * 2)
print("L7 usage GB", day * 500 / 1e9, "content GB", day * 20000 / 1e9)
req = 2000 * 2 / M + 2000 * 0.1 / M + 400 * 10 / M
print("L7 req", req, "day", req * day, "month", req * day * 30)
print("L7 rpm", 289 * 60, "itpm", 17340 * 2000 / 1e6, "otpm", 17340 * 400 / 1e6)
print("L7 hold", (100 // 4 + 1000) * 2 + 500 * 10, "left", 15000 - 8120)
print("L7 128k hold", 128000 * 10 / M, "replicas", 3472 / 1000)
# Exercises
rows = [(500, 12000, 80), (600, 12000, 90), (20500, 12000, 350), (300, 32000, 120)]
cost = [i * 2 / M + r * 0.1 / M + o * 10 / M for i, r, o in rows]
print("E2", [round(c, 4) for c in cost], round(sum(cost), 4), "t3 share", round(cost[2] / sum(cost) * 100))
rows2 = [(500, 12000, 80), (600, 12000, 90), (2500, 12000, 350), (300, 14000, 120)]
c2 = [i * 2 / M + r * 0.1 / M + o * 10 / M for i, r, o in rows2]
print("E2 new", [round(c, 4) for c in c2], round(sum(c2), 4), "lower", round((1 - sum(c2) / sum(cost)) * 100))
print("E3", 620 / 1000, 1700 / 2000, 2880 / 3000, 3960 / 4000, "accept", (2880 + 3960) / 7000,
      "err", 380 + 300, 380 + 300 + 120 + 40, round(680 / 840 * 100), "left", 160)
c = 1500 * 0.10 / M + 150 * 0.50 / M
hits = 3e6 * .35
print("E4", c, c * 3e6, "saved", hits * c, "lookup", 3e6 * 2e-5, "wrong", hits * .002, hits * .002 * .5,
      "net", hits * c - 3e6 * 2e-5 - hits * .002 * .5, "bank", hits * .002 * 50)
print("E5 per arm week", 400 / 2, 431 / 200)
print("AI08 L8 fix", 6000 * .1 / M + 2000 * 2 / M + 300 * 10 / M)
print("AI06 L9 fix", 5000 * .1 / M + 3000 * 2 / M + 300 * 10 / M)

ROOT = pathlib.Path(__file__).resolve().parent.parent
sources = list((ROOT / "code/ai10-production/src").rglob("*.java"))
lines = {line.strip() for p in sources for line in p.read_text(encoding="utf-8").splitlines()}
for md in sorted(glob.glob(str(ROOT / "02-modules/10-production/notes-*.md"))):
    text = pathlib.Path(md).read_text(encoding="utf-8")
    for block in re.findall(r"```java\n(.*?)```", text, re.S):
        for line in block.splitlines():
            if line.strip() and line.strip() not in lines:
                print(pathlib.Path(md).name, "NOT IN SOURCE:", line.strip())
print("code check done")
