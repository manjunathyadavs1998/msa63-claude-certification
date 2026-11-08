"""Recomputes every hand-derived number in AI03."""
print("L2 table totals", 4 + 6 + 12 + 18, 3 + 4 + 1 + 2, 10 / 40)
print("L2 disabled", 4 + 6, 3 + 4, (3 + 4) / (4 + 6))
print("L5 hook per request", 50_000 * 4 / 1e6, "per hour", 1000 * 50_000 * 4 / 1e6)
print("L5 nightly", (5000 * 4 + 800 * 20) / 1e6 * 2e6)
print("L5 Q1 max gap", 28 - 24)
w, r = 100_000 * 4 / 1e6 * 1.25, 100_000 * 4 / 1e6 * 0.05
print("L5 Q4 without", 50 * 100_000 * 4 / 1e6, "with", round(w + 49 * r, 2),
      "saves", round(1 - (w + 49 * r) / 20, 3))
print("L6 Q5", (900 - 120) * 30)
print("E1 precision", 16 / 20, 21 / 30, 5 / 50, 8 / 100, "overall", 50 / 200)
print("E1 after", 37 / 50, "lost", 5 + 8)
print("E4 batches", 300_000 / 100_000, "MB allowance per request KB", 256 * 1024 / 100_000)
print("E4 cost", (4000 * 4 + 600 * 20) / 1e6, (4000 * 4 + 600 * 20) / 1e6 * 300_000,
      (4000 * 4 + 600 * 20) / 1e6 * 300_000 / 2)
print("E4 failures", 0.02 * 300_000)
print("E5", 45 * 800, 45 * 100, 45 * 700)
