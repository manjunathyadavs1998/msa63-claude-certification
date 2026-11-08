"""Builds three timed mock exams from bank/mock*.txt, with four scenario sets of 15 items each.

Source format matches the 500-question bank, plus one frame line per scenario:
    #S1|Scenario text
It checks answer keys, domain counts, answer-letter balance and overlap with the bank.
"""
import collections
import difflib
import pathlib
import random
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
BANK = ROOT / "02-modules/13-certification/bank"
OUT = ROOT / "02-modules/13-certification"
NAMES = {"S1": "Customer Support Resolution Agent", "S2": "Code Generation with Claude Code",
         "S3": "Multi-Agent Research System", "S4": "Developer Productivity with Claude",
         "S5": "Claude Code for Continuous Integration", "S6": "Structured Data Extraction"}
DOMAINS = {"1": "Agentic Architecture and Orchestration", "2": "Tool Design and MCP Integration",
           "3": "Claude Code Configuration and Workflows",
           "4": "Prompt Engineering and Structured Output",
           "5": "Context Management and Reliability"}
WANT = {"1": 16, "2": 11, "3": 12, "4": 12, "5": 9}


def parse(path):
    frames, questions = {}, []
    text = path.read_text(encoding="utf-8").replace("\r\n", "\n")
    for block in re.split(r"\n\s*\n", text.strip()):
        lines = [l for l in block.split("\n") if l.strip()]
        if lines[0].startswith("#"):
            key, frame = lines[0][1:].split("|", 1)
            frames[key] = frame
            continue
        ts, scenario, level = lines[0][1:].split("|")
        q = dict(ts=ts, scenario=scenario, level=level, stem=[], right=[], wrong=[], why="")
        for line in lines[1:]:
            if line.startswith("+ "):
                q["right"].append(line[2:])
            elif line.startswith("- "):
                q["wrong"].append(line[2:])
            elif line.startswith("= "):
                q["why"] = line[2:]
            else:
                q["stem"].append(line)
        q["stem"] = " ".join(q["stem"])
        assert (len(q["right"]), len(q["wrong"])) in ((1, 3), (2, 3)) and q["why"], q["stem"]
        questions.append(q)
    return frames, questions


def deal(questions, seed):
    random.seed(seed)
    single = [q for q in questions if len(q["right"]) == 1]
    random.shuffle(single)
    deck = []
    for q in single:
        if not deck:
            deck = list("ABCD")
            random.shuffle(deck)
        letter = deck.pop()
        options = list(q["wrong"])
        random.shuffle(options)
        options.insert("ABCD".index(letter), q["right"][0])
        q["options"], q["answer"] = options, letter
    pairs = [(a, b) for a in range(5) for b in range(a + 1, 5)]
    random.shuffle(pairs)
    for q, (a, b) in zip([q for q in questions if len(q["right"]) == 2], pairs):
        options = [None] * 5
        right, wrong = list(q["right"]), list(q["wrong"])
        random.shuffle(right)
        random.shuffle(wrong)
        options[a], options[b] = right
        for k in range(5):
            if options[k] is None:
                options[k] = wrong.pop()
        q["options"], q["answer"] = options, "ABCDE"[a] + " and " + "ABCDE"[b]
    for q in questions:
        keyed = {q["options"]["ABCDE".index(x)] for x in q["answer"].split(" and ")}
        assert keyed == set(q["right"]) and len(set(q["options"])) == len(q["options"]), q["stem"]


def render(number, frames, questions, order):
    out = [f"""<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Part V, Certification</div>

# Certification, Mock Exam {number}

## 60 Items in 120 Minutes

<div class="sub">Scenarios: {', '.join(NAMES[s] for s in order)}. Set a timer, work without notes, and mark your answers on paper.</div>

<table class="meta">
<tr><td>Items</td><td>60, in 4 scenario sets of 15</td></tr>
<tr><td>Time</td><td>120 minutes</td></tr>
<tr><td>Domains</td><td>16, 11, 12, 12 and 9 items, rounded from the guide's weights</td></tr>
<tr><td>Status</td><td>Original practice items, written from the guide. Not real or leaked</td></tr>
</table>
</div>

<div class="lesson">

# Before You Start

::: keyidea
==**These are original practice items, not real or leaked exam questions, and the company names are invented. A score here doesn't predict your exam result.**==
:::

Follow the real conditions: one sitting, no notes, flag and return. Where the guide and current products differ, the answers follow the guide, and an explanation may add a *Build today* note. The real exam's wording may be longer and harder, as candidates report.

**Checkpoints:** 28 minutes after Scenario 1, 56 after Scenario 2, 84 after Scenario 3 and 110 after Scenario 4.

**Targets, which are my own and not official:** 80% overall, and no domain under 70%.

</div>
"""]
    n = 0
    for scenario in order:
        out.append(f'<div class="lesson">\n\n# Scenario: {NAMES[scenario]}\n\n::: key\n{frames[scenario]}\n:::\n')
        for q in [q for q in questions if q["scenario"] == scenario]:
            n += 1
            q["n"] = n
            stem = q["stem"]
            select = "" if len(q["right"]) == 1 or "Select 2" in stem else " **Select 2.**"
            options = "<br>\n".join(f"{'ABCDE'[i]}. {o}" for i, o in enumerate(q["options"]))
            out.append(f"::: qa\n**Q{n}.** {stem}{select}\n\n{options}\n:::\n")
        out.append("</div>\n")
    rows = ["| Q | Ans | Q | Ans | Q | Ans | Q | Ans |", "| ---: | --- | ---: | --- | ---: | --- | ---: | --- |"]
    for i in range(0, 60, 4):
        cells = []
        for q in sorted(questions, key=lambda x: x["n"])[i:i + 4]:
            cells += [str(q["n"]), q["answer"].replace(" and ", ", ")]
        rows.append("| " + " | ".join(cells) + " |")
    out.append('<div class="lesson">\n\n# Answer Key and Score Sheet\n\n' + "\n".join(rows) + "\n")
    out.append("\n| Domain | Items | Your score | Target |\n| --- | ---: | ---: | ---: |")
    for d, count in WANT.items():
        out.append(f"| {d}. {DOMAINS[d]} | {count} | | 70%+ |")
    out.append("| **Total** | **60** | | **80%+** |\n\nThe guide doesn't publish how raw scores map to the scaled 720, so these targets are only study guidance.\n")
    out.append("::: notespace short Your error log: item, what you picked, why it was wrong\n:::\n</div>\n")
    out.append('<div class="lesson">\n\n# Explanations\n')
    for q in sorted(questions, key=lambda x: x["n"]):
        out.append(f"**{q['n']}. {q['answer']}** (TS {q['ts']}). {q['why']}\n")
    out.append("</div>\n")
    return "\n".join(out)


def clean(text):
    return re.sub(r"[^a-z0-9 ]", "", text.lower())


def main():
    bank_text = "\n".join(p.read_text(encoding="utf-8") for p in sorted(BANK.glob("d*.txt")))
    bank_stems = [clean(" ".join(block.split("\n")[1:3])) for block in re.split(r"\n\s*\n", bank_text)]
    bank_stems = [stem for stem in bank_stems if stem]
    plans = {1: ["S1", "S2", "S3", "S6"], 2: ["S1", "S4", "S5", "S6"], 3: ["S2", "S3", "S4", "S5"]}
    seen = []
    ts_all = collections.Counter()
    ok = True
    for number, order in plans.items():
        path = BANK / f"mock{number}.txt"
        if not path.exists():
            print(f"mock {number}: not written yet")
            continue
        frames, questions = parse(path)
        deal(questions, 7000 + number)
        counts = collections.Counter(q["ts"][0] for q in questions)
        scen = collections.Counter(q["scenario"] for q in questions)
        multi = sum(len(q["right"]) == 2 for q in questions)
        single = [q for q in questions if len(q["right"]) == 1]
        letters = collections.Counter(q["answer"] for q in single)
        longest = sum(len(q["right"][0]) >= max(map(len, q["wrong"])) for q in single)
        print(f"mock {number}: {len(questions)} items, domains {dict(sorted(counts.items()))}, "
              f"scenarios {dict(sorted(scen.items()))}, multi {multi}, letters {dict(sorted(letters.items()))}, "
              f"correct-longest {100 * longest / len(single):.0f}%")
        ok &= dict(counts) == WANT and all(v == 15 for v in scen.values()) and len(questions) == 60
        ts_all.update(q["ts"] for q in questions)
        for q in questions:
            stem = clean(q["stem"])
            near = difflib.get_close_matches(stem, bank_stems + seen, n=1, cutoff=0.72)
            if near:
                print(f"  NEAR DUPLICATE: {q['stem'][:80]} <> {near[0][:70]}")
            seen.append(stem)
        if "--write" in sys.argv:
            target = OUT / f"mock-{number}.md"
            target.write_text(render(number, frames, questions, order), encoding="utf-8", newline="\n")
    print("TS coverage:", dict(sorted(ts_all.items())))
    print("all shapes ok" if ok else "SHAPE PROBLEM")


main()
