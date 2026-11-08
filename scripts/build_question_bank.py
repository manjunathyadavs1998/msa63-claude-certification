"""Builds the 500-question self-evaluation bank from bank/d*.txt, with balanced answer letters.

Source format, one block per question:
    @TS|Scenario|Difficulty
    Stem
    + correct option (two for a select-2 item)
    - wrong option
    = explanation
"""
import collections
import pathlib
import random
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
BANK = ROOT / "02-modules/13-certification/bank"
OUT = ROOT / "02-modules/13-certification/question-bank-500.md"
SCENARIOS = {
    "S1": "Customer Support", "S2": "Code Generation", "S3": "Multi-Agent Research",
    "S4": "Developer Productivity", "S5": "CI/CD", "S6": "Data Extraction",
}
LEVEL = {"E": "Easy", "M": "Medium", "H": "Hard"}
DOMAINS = {
    "1": "Agentic Architecture and Orchestration", "2": "Tool Design and MCP Integration",
    "3": "Claude Code Configuration and Workflows", "4": "Prompt Engineering and Structured Output",
    "5": "Context Management and Reliability",
}
TARGET = {
    "1.1": 20, "1.2": 20, "1.3": 22, "1.4": 21, "1.5": 18, "1.6": 16, "1.7": 18,
    "2.1": 20, "2.2": 21, "2.3": 17, "2.4": 18, "2.5": 14,
    "3.1": 18, "3.2": 18, "3.3": 14, "3.4": 16, "3.5": 16, "3.6": 18,
    "4.1": 17, "4.2": 17, "4.3": 20, "4.4": 17, "4.5": 16, "4.6": 13,
    "5.1": 14, "5.2": 16, "5.3": 13, "5.4": 12, "5.5": 11, "5.6": 9,
}


def parse():
    questions = []
    errors = []
    for path in sorted(BANK.glob("d*.txt")):
        text = path.read_text(encoding="utf-8").replace("\r\n", "\n")
        for block in re.split(r"\n\s*\n", text.strip()):
            lines = [line for line in block.split("\n") if line.strip()]
            head = lines[0]
            if not head.startswith("@"):
                errors.append(f"{path.name}: bad head: {head[:60]}")
                continue
            ts, scenario, level = head[1:].split("|")
            stem = []
            right, wrong, why = [], [], ""
            for line in lines[1:]:
                if line.startswith("+ "):
                    right.append(line[2:])
                elif line.startswith("- "):
                    wrong.append(line[2:])
                elif line.startswith("= "):
                    why = line[2:]
                else:
                    stem.append(line)
            q = dict(ts=ts, scenario=scenario, level=level, stem=" ".join(stem),
                     right=right, wrong=wrong, why=why)
            shape = (len(right), len(wrong))
            if shape not in ((1, 3), (2, 3)) or not why or scenario not in SCENARIOS:
                errors.append(f"{ts} shape {shape}: {q['stem'][:60]}")
            questions.append(q)
    return questions, errors


def assign_positions(questions):
    """Deals answer positions from shuffled decks, so that letters stay close to equal."""
    random.seed(20261106)
    random.shuffle(order := list(range(len(questions))))
    single = [i for i in order if len(questions[i]["right"]) == 1]
    for domain in "12345":
        deck = []
        for i in [k for k in single if questions[k]["ts"].startswith(domain)]:
            if not deck:
                deck = list("ABCD")
                random.shuffle(deck)
            questions[i]["single_pos"] = "ABCD".index(deck.pop())
    pairs = [(a, b) for a in range(5) for b in range(a + 1, 5)]
    multi = [i for i in order if len(questions[i]["right"]) == 2]
    pair_deck = []
    for i in multi:
        if not pair_deck:
            pair_deck = list(pairs)
            random.shuffle(pair_deck)
        questions[i]["multi_pos"] = pair_deck.pop()
    for q in questions:
        wrong = list(q["wrong"])
        random.shuffle(wrong)
        if len(q["right"]) == 1:
            options = list(wrong)
            options.insert(q["single_pos"], q["right"][0])
            q["answer"] = "ABCD"[q["single_pos"]]
        else:
            a, b = q["multi_pos"]
            right = list(q["right"])
            random.shuffle(right)
            options = [None] * 5
            options[a], options[b] = right
            for k in range(5):
                if options[k] is None:
                    options[k] = wrong.pop()
            q["answer"] = "ABCDE"[a] + " and " + "ABCDE"[b]
        q["options"] = options


def order_questions(questions):
    """Keeps domains together, and mixes task statements and difficulty inside each one."""
    random.seed(500)
    out = []
    for domain in "12345":
        group = [q for q in questions if q["ts"].startswith(domain)]
        random.shuffle(group)
        out.extend(group)
    return out


def report(questions):
    print("total", len(questions))
    counts = collections.Counter(q["ts"] for q in questions)
    for ts, target in TARGET.items():
        mark = "" if counts[ts] == target else f"  TARGET {target}"
        print(f"  {ts}: {counts[ts]}{mark}")
    print("domains", {d: sum(q["ts"].startswith(d) for q in questions) for d in "12345"})
    print("scenario", dict(sorted(collections.Counter(q["scenario"] for q in questions).items())))
    print("level", dict(collections.Counter(q["level"] for q in questions)))
    single = [q for q in questions if len(q["right"]) == 1]
    print("multi", len(questions) - len(single))
    letters = collections.Counter(q["answer"] for q in single)
    print("letters", {k: f"{letters[k]} ({100 * letters[k] / len(single):.1f}%)" for k in "ABCD"})
    for d in "12345":
        c = collections.Counter(q["answer"] for q in single if q["ts"].startswith(d))
        n = max(1, sum(c.values()))
        print(f"  domain {d}", {k: f"{100 * c[k] / n:.0f}%" for k in "ABCD"})
    longest = sum(len(q["right"][0]) >= max(map(len, q["wrong"])) for q in single)
    print(f"correct is longest: {100 * longest / len(single):.1f}% (chance is about 25%)")
    if "--tells" in sys.argv:
        for q in single:
            longest_wrong = max(map(len, q["wrong"]))
            if len(q["right"][0]) > longest_wrong + 8:
                print("TELL", q["ts"], len(q["right"][0]), longest_wrong, q["right"][0][:70])
    stems = collections.Counter(q["stem"] for q in questions)
    print("duplicate stems", [s[:60] for s, n in stems.items() if n > 1])


def inline(text):
    return text.replace("$", "\\$") if False else text


def render(questions):
    out = []
    out.append("""<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Part V, Certification</div>

# Certification Self-Evaluation

## 500 Questions, With Answers at the End

<div class="sub">Practice for Claude Certified Architect – Foundations (CCAR-F), written from the official exam guide's 30 task statements and weighted the way the guide weights its domains.</div>

<table class="meta">
<tr><td>Exam</td><td>CCAR-F, guide v1.0, effective July 2026</td></tr>
<tr><td>Questions</td><td>500, and all original</td></tr>
<tr><td>Answers</td><td>Answer key and explanations at the end</td></tr>
<tr><td>Time</td><td>About 2 minutes per question, as on the exam</td></tr>
</table>
</div>

<div class="lesson">

# How to Use This Bank

::: keyidea
==**These are original practice questions, written from the public exam guide. None is a real or leaked exam question, and no score here predicts your exam result.**==
:::

**What the real exam looks like.** According to the guide: 60 multiple-choice and multiple-response items in 120 minutes, across 4 scenarios drawn from 6, with a pass mark of a scaled 720 out of 1,000. The guide doesn't publish a raw-score equivalent, so a scaled 720 doesn't mean 72% correct.

**How this bank differs.** Items are grouped by domain and aren't presented as scenario sets. Each names its scenario, so you can practise reading in context. Exam items may be longer.

**Where the guide and current products differ, the answer follows the guide,** because that's what the exam is written against. An answer marked *Build today* tells you what current documentation says. Check for a newer guide before you sit the exam.

**Suggested method:**

1. Do one domain at a time, without notes, marking your answers on paper.
2. Check them against the answer key, then read the explanation for each one you missed or guessed.
3. Record your percentage per domain below. Note the task statement of each miss, as that shows what to restudy.
4. Retake your misses a few days later.

## Score sheet

These targets are my own study suggestions, and not official figures.

| Domain | Questions | Exam weight | Your score | Target |
| --- | ---: | ---: | ---: | ---: |""")
    weights = {"1": 27, "2": 18, "3": 20, "4": 20, "5": 15}
    number = 0
    ranges = {}
    for d in "12345":
        n = sum(q["ts"].startswith(d) for q in questions)
        ranges[d] = (number + 1, number + n)
        number += n
        out.append(f"| {d}. {DOMAINS[d]} (Q{ranges[d][0]}–{ranges[d][1]}) | {n} | {weights[d]}% | | 80% |")
    out.append("\nAim for 80% overall, with no domain below 70%.\n\n::: notespace short Your notes\n:::\n</div>\n")
    number = 0
    for d in "12345":
        out.append(f'<div class="lesson">\n\n# Domain {d}: {DOMAINS[d]}\n')
        for q in [x for x in questions if x["ts"].startswith(d)]:
            number += 1
            q["n"] = number
            select = " **Select 2.**" if len(q["right"]) == 2 else ""
            letters = "ABCDE"
            opts = "<br>\n".join(f"{letters[i]}. {o}" for i, o in enumerate(q["options"]))
            out.append(f"::: qa\n**Q{number}.** *{SCENARIOS[q['scenario']]} · {LEVEL[q['level']]}.* "
                       f"{q['stem']}{select}\n\n{opts}\n:::\n")
        out.append("</div>\n")
    out.append('<div class="lesson">\n\n# Answer Key\n\nUse this for quick marking. Explanations follow.\n')
    per_row = 5
    # Tables can't split across pages here, so each one must fit on a page.
    for start in range(0, len(questions), 85):
        chunk = questions[start:start + 85]
        rows = ["| " + " | ".join(["Q", "Ans"] * per_row) + " |",
                "| " + " | ".join(["---:", "---"] * per_row) + " |"]
        for i in range(0, len(chunk), per_row):
            cells = []
            for q in chunk[i:i + per_row]:
                cells += [str(q["n"]), q["answer"].replace(" and ", ", ")]
            while len(cells) < per_row * 2:
                cells.append("")
            rows.append("| " + " | ".join(cells) + " |")
        out.append("\n".join(rows) + "\n")
    out.append("</div>\n")
    for d in "12345":
        out.append(f'<div class="lesson">\n\n# Answers: Domain {d}\n')
        for q in [x for x in questions if x["ts"].startswith(d)]:
            out.append(f"**{q['n']}. {q['answer']}** (TS {q['ts']}). {q['why']}\n")
        out.append("</div>\n")
    return "\n".join(out)


def main():
    questions, errors = parse()
    for e in errors:
        print("ERROR", e)
    assign_positions(questions)
    questions = order_questions(questions)
    # The key must point at the options marked correct, and options must be distinct.
    for q in questions:
        keyed = {q["options"]["ABCDE".index(x)] for x in q["answer"].split(" and ")}
        assert keyed == set(q["right"]), q["stem"]
        assert len(set(q["options"])) == len(q["options"]), q["stem"]
    print("key check passed for", len(questions))
    report(questions)
    if len(sys.argv) > 1 and sys.argv[1] == "--write":
        OUT.write_text(render(questions), encoding="utf-8")
        print("wrote", OUT)


main()
