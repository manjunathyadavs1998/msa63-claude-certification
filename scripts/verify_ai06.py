"""Recomputes every hand-derived number in AI06, and checks exam-style answer balance."""
import glob
import re
from collections import Counter

M = 1e6
OPUS_IN, OPUS_OUT, SONNET_IN, SONNET_OUT, CACHE_READ = 4.0, 20.0, 2.0, 10.0, 0.20

print("L1 reliability", [round(p ** n, 3) for p, n in [(.99, 20), (.95, 10), (.90, 10)]])
print("L1 Q4", round(.97 ** 15, 3), round(.9 ** (1 / 15), 4))

start, add, turns = 5000, 1000, 10
total = sum(start + add * i for i in range(turns))
reads = sum(start + add * (i - 1) for i in range(1, turns))
writes = start + add * (turns - 1)
print("L2 uncached", total, total * OPUS_IN / M, "reads", reads, "writes", writes,
      "cached $", round(reads * CACHE_READ / M + writes * OPUS_IN * 1.25 / M, 4))

print("L4 integrations", 5 * 4, 5 + 4, 10 * 20, 10 + 20, "Q4", 6 * 15, 6 + 15, 6 * 15 - 21)
print("L5 script calls generic", 3 + 3 + 3 + 3 + 3 + 1, "typed", 3 + 3 + 1 + 1 + 1 + 1)
print("L6 sim sequential", 4 * 300, "ideal parallel", 2 * 300)
print("L8 rates", 30000 * .01, 30000 * .001)

cases = 200_000
per_min = cases / (30 * 24 * 60)
print("L9 per min", round(per_min, 2), "concurrent", round(per_min * 5, 1), "peak",
      round(per_min * 5 * 4, 1))
sonnet = 5000 * 0.10 / M + 3000 * SONNET_IN / M + 300 * SONNET_OUT / M
opus = 5000 * CACHE_READ / M + 3000 * OPUS_IN / M + 300 * OPUS_OUT / M
print("L9 per call", sonnet, "case", sonnet * 6, "month", sonnet * 6 * cases,
      "opus call", opus, "opus month", round(opus * 6 * cases))
print("L9 value", .8 * cases, .8 * cases * 5, "one point", cases * .01, cases * .01 * 5)

coord = 60000 * OPUS_IN / M + 4000 * OPUS_OUT / M
sub = 40000 * SONNET_IN / M + 3000 * SONNET_OUT / M
cite = 30000 * SONNET_IN / M + 2000 * SONNET_OUT / M
report = coord + 5 * sub + cite
print("L10", coord, 5 * sub, cite, round(report, 2), "month", round(2000 * report),
      "peak", 20 * 5, "50 extra subagents", round(50 * sub, 2))

print("E2", sum(4000 + 800 * i for i in range(12)))
c = 50000 * OPUS_IN / M + 3000 * OPUS_OUT / M
s = 30000 * SONNET_IN / M + 2000 * SONNET_OUT / M
c2 = 50000 * SONNET_IN / M + 3000 * SONNET_OUT / M
print("E5", c, s, round(c + 8 * s, 2), "month", round(500 * (c + 8 * s), 2), "sonnet",
      round(c2 + 8 * s, 2), "time", 8 * 90, 2 * 90)
print("E6 rule of three", 3 / 1000, 30000 * 3 / 1000)

letters = Counter()
for path in sorted(glob.glob("02-modules/06-agents-mcp/notes-*.md")):
    text = open(path, encoding="utf-8").read()
    for question, answer in re.findall(
            r'<div class="q">([^\n]*?Exam style[^\n]*?)</div>\n<div class="a">([^\n]*?)</div>', text):
        for letter in re.findall(r"<b>([A-E])(?: and ([A-E]))?</b>", answer)[:1]:
            for item in letter:
                if item:
                    letters[item] += 1
total = sum(letters.values())
print("answers", dict(sorted(letters.items())), "total", total,
      {k: round(v / total, 2) for k, v in sorted(letters.items())})
