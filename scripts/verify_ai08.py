"""Recomputes every hand-derived number in AI08, and checks lesson code against sources."""
import glob
import pathlib
import re

M = 1e6
S_IN, S_OUT, READ = 2.0, 10.0, 0.10
print("L1 worst", 120 * 3, "Q2", 3 * 3 * 3)
per = 1500 * S_IN / M + 150 * S_OUT / M
print("L6 per", per, "sync", 50000 * per, "restart", 70000 * per, "batch", 50000 * per / 2)
q3 = 2000 * S_IN / M + 200 * S_OUT / M
print("L6 Q3", q3, 200000 * q3, 200000 * q3 / 2, "fail rate", 1 - 230 / 10000)
print("L6 retries", 3 * 3)
avg = 2e6 / (30 * 16 * 3600)
print("L8 avg", round(avg, 2), "peak", round(avg * 5, 1), "concurrent", round(avg * 5 * 20))
call = 6000 * READ / M + 2000 * S_IN / M + 300 * S_OUT / M
print("L8 call", round(call, 4), "q", round(call * 3, 4), "month", round(call * 3 * 2e6))
b = 4000 * S_IN / M + 400 * S_OUT / M
print("L8 batch each", b / 2, "total", b / 2 * 1e6, "standard", b * 1e6)
print("E1", 45 * 4, "c", 11 * 2)
e4 = 3000 * S_IN / M + 300 * S_OUT / M
print("E4", e4, 500000 * e4 / 2, 300000 * e4, 100000 * e4)
print("L7 eval", round(5 / 6 * 100))

ROOT = pathlib.Path(__file__).resolve().parent.parent
code = "\n".join(p.read_text(encoding="utf-8") for p in (ROOT / "code/ai08-java/src").rglob("*.java"))
lines = {line.strip() for line in code.splitlines()}
for md in sorted(glob.glob(str(ROOT / "02-modules/08-java/notes-*.md"))):
    text = pathlib.Path(md).read_text(encoding="utf-8")
    for block in re.findall(r"```java\n(.*?)```", text, re.S):
        for line in block.splitlines():
            if line.strip() and line.strip() not in lines:
                print(pathlib.Path(md).name, "NOT IN SOURCE:", line.strip())
print("code check done")
