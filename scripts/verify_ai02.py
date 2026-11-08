"""Recomputes every hand-derived number in AI02."""
from math import log

PRICES = {"Fable 5.1": (10, 50), "Opus 5.5": (4, 20), "Sonnet 5.5": (2, 10), "Haiku 5.5": (0.10, 0.50)}


def cost_per_million(price, input_tokens, output_tokens):
    return (input_tokens * price[0] + output_tokens * price[1])


print("L2 Q2", 1024 / 16, 4000 ** 2)
print("L3 FF per block, d=512", (512 * 2048 + 2048) + (2048 * 512 + 512))
print("L3 ratios", round(1600 / 768, 2), 48 / 12, round(1_557_611_200 / 124_439_808, 2))
print("L3 Q3 70B at 16-bit GB", 70e9 * 2 / 1e9)
print("L4 ln(28)", round(log(28), 3), "175/1.3", round(175 / 1.3))
print("L5 Q3", 0.8 + 500 / 50)
print("L5 no-cache 2000,500", sum(2000 + i for i in range(500)), "with", 2500)
print("L7 prices per 1M requests (2000 in, 500 out):")
for name, price in PRICES.items():
    print(" ", name, cost_per_million(price, 2000, 500))
print("L7 top/bottom", cost_per_million(PRICES["Fable 5.1"], 2000, 500)
      / cost_per_million(PRICES["Haiku 5.5"], 2000, 500))
for name, price, error in [("Opus", PRICES["Opus 5.5"], .01), ("Sonnet", PRICES["Sonnet 5.5"], .02),
                           ("Haiku15", PRICES["Haiku 5.5"], .15), ("Haiku1.5", PRICES["Haiku 5.5"], .015)]:
    model = cost_per_million(price, 2000, 500) / 1e6
    print(" ", name, round(model, 5), round(error * 2, 4), round(model + error * 2, 5))
print("L7 haiku15/opus", round(.30045 / .038, 1))
print("L7 Q2", 10_000 * 100_000 / 1e6 * 2 + 1_000 * 100_000 / 1e6 * 10)
# Exercises
print("E1 merges prose vs json: 6/25, 21/20", round(6 / 25, 2), round(21 / 20, 2))
print("E2 budget", 800_000 * 1.3, 800_000 * 1.3 + 16_000 <= 1_000_000)
print("E3 pairs ratio", (20_000 ** 2) / (2_000 ** 2))
print("E4 total", 1.0 + 1200 / 80, 1.0 + 300 / 80)
print("E5 cost", (5_000 * 4 + 800 * 20) / 1e6, "x 2M", (5_000 * 4 + 800 * 20) * 2,
      "batch half", (5_000 * 4 + 800 * 20) * 2 / 2)
print("E6 prompts", 0.4 * 200, 0.6 * 200)
