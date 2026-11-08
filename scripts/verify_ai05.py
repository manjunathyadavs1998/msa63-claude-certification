"""Recomputes every hand-derived number in AI05."""
from math import ceil, sqrt

M = 1e6
OPUS_IN, SONNET_IN, SONNET_OUT, HAIKU_IN, HAIKU_OUT = 4.0, 2.0, 10.0, 0.10, 0.50

print("L1 cached read", 150_000 * OPUS_IN * .05 / M, "RAG", 20 * 800 * OPUS_IN / M,
      "write", 150_000 * OPUS_IN * 1.25 / M)
print("L1 Q2 write", 100_000 * OPUS_IN * 1.25 / M, "reads", round(999 * 100_000 * OPUS_IN * .05 / M, 2),
      "uncached", 1000 * 100_000 * OPUS_IN / M)
print("L2 Q2 chunks", [s for s in range(0, 1000, 150) if s < 1000 and (s == 0 or s + 50 < 1000)][:8],
      "stored", 6 * 200 + 100)
print("L3 reductions", round(1 - 3.7 / 5.7, 3), round(1 - 2.9 / 5.7, 3), round(1 - 1.9 / 5.7, 3))

print("L6 ALL", (37 + 10 + 16) / 100, (36 + 10 + 11 + 12) / 100, (35 + 10 + 10 + 12) / 100,
      "recall without no-answer", (37 + 10 + 16) / 80)
for n, p in [(100, .90), (20, .50), (40, .90), (385, .50)]:
    print(f"L6 margin n={n} p={p}: +/-{1.96 * sqrt(p * (1 - p) / n):.3f}")
print("L6 judge precision", round(24 / (24 + 10), 3), "recall", 24 / 30,
      "human unsupported", 30 / 200, "judge unsupported", (24 + 10) / 200)
print("L6 Q2 recall", 7 / 10, "context precision", round(3 / 8, 3))
print("L6 cost per run", 300 * (4000 * SONNET_IN + 400 * SONNET_OUT) / M, "judge",
      300 * 3 * (1500 * HAIKU_IN + 100 * HAIKU_OUT) / M)

EMP, ACTIVE, PER_DAY = 50_000, .40, 4
dau = EMP * ACTIVE
q_day = dau * PER_DAY
avg = q_day / (8 * 3600)
print("L7 DAU", dau, "q/day", q_day, "avg workday qps", round(avg, 2), "peak x3", round(avg * 3, 1))
docs, per_doc, tokens = 4e6, 12, 400
chunks = docs * per_doc
print("L7 chunks", chunks, "float32 GB", chunks * 1024 * 4 / 1e9, "int8 GB", chunks * 1024 / 1e9,
      "graph GB", round(chunks * 264 / 1e9, 2), "text GB", chunks * 1600 / 1e9)
int8_total = chunks * (1024 + 264) / 1e9
print("L7 int8+graph GB", round(int8_total, 1), "64GB nodes", ceil(int8_total / 64))
corpus_tok = chunks * tokens
print("L7 corpus tokens", corpus_tok / 1e9, "B; contextualize $", corpus_tok / M * 1.02)
print("L7 monthly changed 5% tokens B", corpus_tok * .05 / 1e9, "$", corpus_tok * .05 / M * 1.02)

rewrite = (600 * HAIKU_IN + 60 * HAIKU_OUT) / M
sys_write = 2000 * SONNET_IN * 1.25 / M
sys_read = 2000 * SONNET_IN * .05 / M
ctx = 8 * tokens * SONNET_IN / M
out = 450 * SONNET_OUT / M
answer = sys_read + ctx + out
print("L7 rewrite", round(rewrite, 6), "sys read", sys_read, "ctx", ctx, "out", out,
      "answer", round(answer, 5), "total", round(answer + rewrite, 5))
month = q_day * 22
print("L7 month q", month, "$", round(month * (answer + rewrite)), "per employee",
      round(month * (answer + rewrite) / EMP, 2))
print("L7 cache writes/day upper (one per 5 min, 8h)", 8 * 12, "$", round(8 * 12 * sys_write, 3))
print("L7 semantic cache 20% hit saves $", round(.2 * month * answer))

print("E1 tokens", 900 * 400 / 1000, "K")
print("E3 chunks", 2e6 * 10, "GB int8+graph", 2e6 * 10 * 1288 / 1e9)
print("E4 ALL recall", (72 + 45 + 18) / 150, "by seg", 72 / 80, 45 / 50, 18 / 20)
print("E4b faithful", (70 + 25 + 17) / 150, 70 / 80, 25 / 50, 17 / 20)
print("E5 month $", 30_000 * 22 * (6000 * SONNET_IN + 500 * SONNET_OUT) / M,
      "with 3000 tok ctx", 30_000 * 22 * (3000 * SONNET_IN + 500 * SONNET_OUT) / M)
print("E4d margin n=50 p=.5", round(1.96 * sqrt(.25 / 50), 3))
print("L7 ITPM", round(8.3 * 60 * 3200), "OTPM", round(8.3 * 60 * 450), "20 chunks ctx", 20 * 400 * SONNET_IN / M)
