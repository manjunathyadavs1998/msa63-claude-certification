"""Recomputes every hand-derived number in AI01."""
from math import exp, log


def sigmoid(z):
    return 1 / (1 + exp(-z))


print("L1 0.1% of a million =", 0.001 * 1_000_000)
print("L2 accuracy-only tie: 1-412/20000 =", round(1 - 412 / 20000, 4))
print("L2 gini 8/2 =", round(1 - 0.8**2 - 0.2**2, 2), " 50/50 =", 1 - 0.5**2 - 0.5**2)
print("L2 threshold 5/200 =", 5 / 200)
print("L2 Q2 acc/prec/rec =", (40 + 890) / 1000, 40 / 50, 40 / 100)

p = sigmoid(1)
g = (p - 1) * 2
w = 0.5 - 0.1 * g
print("L3 worked: p", round(p, 4), "loss", round(-log(p), 4), "grad", round(g, 4), "w", round(w, 4))
p2 = sigmoid(w * 2)
print("L3 worked: new p", round(p2, 4), "new loss", round(-log(p2), 4))
print("L3 cross-entropy 0.9, 0.01 =", round(-log(0.9), 3), round(-log(0.01), 3))
print("L3 params 4*3+5 =", 4 * 3 + 5)
print("L3 Q1 =", round(1 - 0.5 * (sigmoid(1) - 1), 4))

print("L4 idf refund, the =", round(log(12 / 3), 3), round(log(12 / 10), 3),
      "ratio", round(log(4) / log(1.2), 2))
print("L4 Q2 =", round(log(100), 3), round(log(2), 3), round(log(100) / log(2), 2))

# Exercise 1
total = 1_000_000
fraud = 5_000
tp = int(fraud * 0.8)
fn = fraud - tp
fp = int((total - fraud) * 0.01)
tn = total - fraud - fp
print("E1 tp fp fn tn", tp, fp, fn, tn)
print("E1 precision", round(tp / (tp + fp), 4), "accuracy", round((tp + tn) / total, 5))
print("E1 review", (tp + fp) * 5, "missed", fn * 200, "total", (tp + fp) * 5 + fn * 200)
print("E1 no model", fraud * 200, "review all", total * 5)

# Exercise 2
print("E2 threshold 8/400 =", 8 / 400)
print("E2 overall recall", (900 * 0.8 + 100 * 0.05) / 1000)

# Exercise 3
print("E3 weighted gini", round(0.5 * 0.32 + 0.5 * 0.32, 2), "reduction", round(0.5 - 0.32, 2))

# Exercise 4
x, w, b, y, lr = 1.5, 0.4, 0.1, 0, 0.5
z = w * x + b
p = sigmoid(z)
loss = -log(1 - p)
gw = (p - y) * x
gb = p - y
w2 = w - lr * gw
b2 = b - lr * gb
z2 = w2 * x + b2
p2 = sigmoid(z2)
print("E4 z", round(z, 4), "p", round(p, 4), "loss", round(loss, 4), "gw", round(gw, 4),
      "gb", round(gb, 4), "w", round(w2, 4), "b", round(b2, 4), "z2", round(z2, 4),
      "p2", round(p2, 4), "loss2", round(-log(1 - p2), 4))

# Exercise 5 (illustrative prices)
print("E5 all LLM", 100_000 * 0.002 + 2_000 * 1.5, "tiered", 18_000 * 0.002 + 2_000 * 1.5,
      "tiered 1% human", 18_000 * 0.002 + 1_000 * 1.5)
