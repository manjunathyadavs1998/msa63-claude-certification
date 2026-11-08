"""Recomputes every hand-derived number in AI11, and checks lesson Java against the sources."""
import glob
import pathlib
import re

print("L1 ratio at 100", round(0.0569 / 0.0061, 1), "param share", 160 / 1600)
print("L2 fixed", 2 * 3 * 730 + 15000, "req", (500 * .1 + 150 * .5) / 1e6,
      "breakeven", (2 * 3 * 730 + 15000) / ((500 * .1 + 150 * .5) / 1e6))
print("L2 range", round(8.33 / .46, 1), "90 v 5", .9 / .05)
kv = lambda tokens: 2 * 32 * 16 * 128 * 2 * tokens / 1e9
print("L2 kv 2k", kv(2000), "32k", kv(32000), "left", 80 - 70, "per 2k", kv(2000), "n", 10 / kv(2000))
print("L2 Q2", 6 / (3000 * .25 * 3600) * 1e6, 6 / (3000 * .75 * 3600) * 1e6)
print("L3 toy weights", 8 * 32 + 32)
print("L4 hook", [round(100 * .99 ** n, 1) for n in (5, 20, 50, 200)])
print("L5 diverge", 4234 / 10000, "theory", 2 * .7 * .3, "dup", (101968 - 100000) * .013)
print("E2 kv8k", kv(8000), "fit", 20 / kv(8000), "cost", 4 / (3000 * .4 * 3600) * 1e6,
      "32k", kv(32000), "avg", 2 / 3 * kv(32000) + 1 / 3 * kv(8000), "fit", 20 / (2 / 3 * kv(32000) + 1 / 3 * kv(8000)))

ROOT = pathlib.Path(__file__).resolve().parent.parent
sources = list((ROOT / "code/ai11-advanced/src").rglob("*.java"))
lines = {line.strip() for p in sources for line in p.read_text(encoding="utf-8").splitlines()}
for md in sorted(glob.glob(str(ROOT / "02-modules/11-advanced/notes-*.md"))):
    text = pathlib.Path(md).read_text(encoding="utf-8")
    for block in re.findall(r"```java\n(.*?)```", text, re.S):
        for line in block.splitlines():
            if line.strip() and line.strip() not in lines:
                print(pathlib.Path(md).name, "NOT IN SOURCE:", line.strip())
print("code check done")
