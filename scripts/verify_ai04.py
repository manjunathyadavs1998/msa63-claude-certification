"""Recomputes every hand-derived number in AI04."""
from math import ceil

print("L1 Q2", 40e6 * 1024 * 4 / 1e9, 40e6 * 512 / 1e9)
print("L2 .9^40", round(.9 ** 40, 4), ".92^40", round(.92 ** 40, 4))
print("L3 MRR", round((1 + .5 + .25) / 3, 3), "Q1", round((1 + 1 / 3 + 0 + .5) / 4, 3))
print("L3 raw sums", 4.80 + 0.10, 1.00 + 0.87)
print("L4 3M", 3e6 * 1024 * 4 / 1e9)
print("L5 ratio", round(21_991_292 / 8_793), "int8 saving", round(1 - 6 / 20, 2))
print("L5 largest tenant int8+graph GB", round(21_991_292 * (1024 + 264) / 1e9, 1))
print("L5 replicas", 2000 / 400, "shard searches", 2000 * 3, "capacity", 6 * 400)
print("E1 float", 60e6 * 1536 * 4 / 1e9, "graph", 60e6 * 264 / 1e9)
for name, gb in [("float", 368.64 + 15.84), ("half", 184.32 + 15.84), ("int8", 92.16 + 15.84),
                 ("768", 60e6 * 768 * 4 / 1e9 + 15.84)]:
    print(" ", name, round(gb, 2), ceil(gb / 64))
print("E3", round(1 / 61, 5), round(1 / 64 + 1 / 62, 5), round(1 / 62 + 1 / 63, 5))
chunks = 30e6 + 4 * 5e6 + 50 * 200e3 + 5000 * 4e3
print("E5 chunks", chunks, "GB", chunks * 1288 / 1e9, "dedicated", 50e6 * 1288 / 1e9,
      "shared", (chunks - 50e6) * 1288 / 1e9, "nodes", ceil(chunks * 1288 / 1e9 / 90))
