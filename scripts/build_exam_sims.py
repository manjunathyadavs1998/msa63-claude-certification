"""Builds the tick-box exam simulations from bank/sim*.txt, and writes their keys separately.

Source format, with blocks separated by a blank line:
    #S1|Scenario frame
    @1.4|H
    Stem
    + correct option
    - wrong option
    ? explanation

Checks: 60 items, domain counts 16/11/12/12/9, 15 per scenario, letter balance, correct-answer
length, stem length, and overlap with the bank, the earlier mocks and the guide's samples.
"""
import collections
import difflib
import json
import pathlib
import random
import re
import statistics
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
MOD = ROOT / "02-modules/13-certification"
OUT = MOD / "exam-simulations"
NAMES = {"S1": "Customer Support Resolution Agent", "S2": "Code Generation with Claude Code",
         "S3": "Multi-Agent Research System", "S4": "Developer Productivity with Claude",
         "S5": "Claude Code for Continuous Integration", "S6": "Structured Data Extraction"}
WANT = {"1": 16, "2": 11, "3": 12, "4": 12, "5": 9}


def parse(path):
    sets, current = [], None
    for block in re.split(r"\n\s*\n", path.read_text(encoding="utf-8").replace("\r\n", "\n").strip()):
        lines = [l for l in block.split("\n") if l.strip()]
        if lines[0].startswith("#"):
            key, frame = lines[0][1:].split("|", 1)
            current = dict(key=key, frame=frame, items=[])
            sets.append(current)
            continue
        ts, level = lines[0][1:].split("|")
        q = dict(ts=ts, level=level, stem="", right=[], wrong=[], why="")
        for line in lines[1:]:
            if line.startswith("+ "):
                q["right"].append(line[2:])
            elif line.startswith("- "):
                q["wrong"].append(line[2:])
            elif line.startswith("? "):
                q["why"] = line[2:]
            else:
                q["stem"] = (q["stem"] + " " + line).strip()
        assert q["why"], q["stem"]
        assert (len(q["right"]), len(q["wrong"])) in ((1, 3), (2, 3)), q["stem"]
        current["items"].append(q)
    return sets


def arrange(items, seed):
    """Places the correct option so that letters stay balanced within 1, for single-answer items."""
    rng = random.Random(seed)
    singles = [q for q in items if len(q["right"]) == 1]
    letters = list("ABCD") * (len(singles) // 4 + 1)
    letters = letters[:len(singles)]
    rng.shuffle(letters)
    for q, letter in zip(singles, letters):
        wrong = q["wrong"][:]
        rng.shuffle(wrong)
        index = "ABCD".index(letter)
        q["options"] = wrong[:index] + q["right"] + wrong[index:]
        q["answer"] = [letter]
    for q in items:
        if len(q["right"]) == 2:
            options = q["right"] + q["wrong"]
            rng.shuffle(options)
            q["options"] = options
            q["answer"] = ["ABCDE"[options.index(r)] for r in q["right"]]
            q["answer"].sort()


def check(name, sets):
    items = [q for s in sets for q in s["items"]]
    problems = []
    if len(items) != 60:
        problems.append(f"{len(items)} items")
    counts = collections.Counter(q["ts"][0] for q in items)
    if dict(counts) != WANT:
        problems.append(f"domains {sorted(counts.items())}")
    for s in sets:
        if len(s["items"]) != 15:
            problems.append(f"{s['key']} has {len(s['items'])}")
    singles = [q for q in items if len(q["answer"]) == 1]
    letters = collections.Counter(q["answer"][0] for q in singles)
    if max(letters.values()) - min(letters.values()) > 1:
        problems.append(f"letters {letters}")
    longest = sum(len(q["right"][0]) >= max(len(o) for o in q["options"]) for q in singles)
    stems = [len(q["stem"].split()) for q in items]
    print(f"{name}: items {len(items)}, multi {len(items) - len(singles)}, letters {dict(sorted(letters.items()))}, "
          f"correct longest {100 * longest / len(singles):.0f}%, stem median {statistics.median(stems)} "
          f"max {max(stems)}, TS {len({q['ts'] for q in items})}, levels "
          f"{dict(collections.Counter(q['level'] for q in items))}")
    if longest / len(singles) > 0.40:
        problems.append("correct answer too often the longest")
    return problems


def older_stems():
    text = ""
    for f in ["question-bank-500.md", "mock-1.md", "mock-2.md", "mock-3.md"]:
        text += (MOD / f).read_text(encoding="utf-8")
    return [s.lower() for s in re.findall(r"\*\*Q\d+\.\*\*\s(?:\*[^*]+\*\s)?(.*?)\n\n", text, re.S)]


def guide_stems(path):
    text = pathlib.Path(path).read_text(encoding="utf-8", errors="replace")
    return [re.sub(r"\s+", " ", s).lower() for s in re.findall(r"Question \d+: (.*?)\n\n", text, re.S)]


def overlap(all_sets, older, guide):
    stems = [(n, q["stem"].lower()) for n, sets in all_sets.items() for s in sets for q in s["items"]]
    issues = []
    for i, (name, stem) in enumerate(stems):
        for other_name, other in stems[i + 1:]:
            if difflib.SequenceMatcher(None, stem, other).ratio() >= 0.72:
                issues.append(f"within sims: {name}/{other_name}: {stem[:70]}")
        for label, pool, cutoff in (("older", older, 0.72), ("guide", guide, 0.6)):
            for other in pool:
                m = difflib.SequenceMatcher(None, stem, other)
                if m.real_quick_ratio() >= cutoff and m.quick_ratio() >= cutoff and m.ratio() >= cutoff:
                    issues.append(f"{label}: {name}: {stem[:70]}")
    return issues


def write(number, sets):
    OUT.mkdir(exist_ok=True)
    (OUT / "keys").mkdir(exist_ok=True)
    names = ", ".join(NAMES[s["key"]] for s in sets)
    lines = [f"# Exam Simulation {number}", "",
             f"**Candidate:** ________  **Start time:** ________  **End time:** ________", "",
             "| | |", "| --- | --- |", "| Items | 60, in 4 scenario sets of 15 |", "| Time | 120 minutes, about 2 minutes per item |",
             f"| Scenarios | {names} |", "", "## Rules", "",
             "- Work in one sitting, with no notes, tools or AI help, and set a 120-minute timer.",
             "- **To tick an answer,** change `[ ]` to `[x]`. Editors with Markdown checkboxes let you click instead.",
             "- Each item says how many answers to select. A multiple-response item only counts if every choice is right.",
             "- Don't open the `keys` folder until you've finished.",
             "- When you've finished, save this file and ask Claude to evaluate it.", "",
             "> These are original practice items, written from the public exam guide. They aren't real or leaked exam "
             "questions, the company names are invented, and a score here doesn't predict your exam result.", ""]
    n = 0
    key = [f"# Exam Simulation {number}: Answer Key", "", "Don't read this until you've finished the simulation.", ""]
    data = []
    checkpoints = {0: 0, 1: 30, 2: 60, 3: 90}
    for i, s in enumerate(sets):
        lines += ["---", "", f"## Scenario {i + 1}: {NAMES[s['key']]}", "",
                  f"*Suggested checkpoint: start by minute {checkpoints[i]}.*", "", s["frame"], ""]
        key += [f"## Scenario {i + 1}: {NAMES[s['key']]}", ""]
        for q in s["items"]:
            n += 1
            count = len(q["answer"])
            ask = "Select 1." if count == 1 else f"Select {count}."
            lines += [f"### Question {n}", "", f"{q['stem']} **{ask}**", ""]
            for letter, option in zip("ABCDE", q["options"]):
                lines.append(f"- [ ] {letter}. {option}")
            lines.append("")
            key += [f"**{n}. {' and '.join(q['answer'])}** (TS {q['ts']}, {q['level']}). {q['why']}", ""]
            data.append(dict(n=n, answer=q["answer"], ts=q["ts"], scenario=NAMES[s["key"]], level=q["level"], why=q["why"]))
    lines += ["---", "", "**End of simulation.** Save this file and ask Claude to evaluate it.", ""]
    (OUT / f"Exam-Simulation-{number}.md").write_text("\n".join(lines), encoding="utf-8", newline="\n")
    (OUT / "keys" / f"Exam-Simulation-{number}-Key.md").write_text("\n".join(key), encoding="utf-8", newline="\n")
    (OUT / "keys" / f"exam-simulation-{number}.json").write_text(json.dumps(data, indent=1), encoding="utf-8", newline="\n")


def main():
    all_sets, failed = {}, []
    for number in (1, 2, 3):
        path = MOD / "bank" / f"sim{number}.txt"
        if not path.exists():
            continue
        sets = parse(path)
        rng = random.Random(1000 + number)
        for s in sets:
            rng.shuffle(s["items"])
        arrange([q for s in sets for q in s["items"]], number * 7)
        failed += [f"sim{number}: {p}" for p in check(f"sim{number}", sets)]
        all_sets[f"sim{number}"] = sets
        write(number, sets)
    ts = collections.Counter(q["ts"] for sets in all_sets.values() for s in sets for q in s["items"])
    print("TS coverage:", sorted(ts.items()))
    if len(sys.argv) > 1:
        issues = overlap(all_sets, older_stems(), guide_stems(sys.argv[1]))
        print("overlap issues:", len(issues))
        for issue in issues:
            print(" ", issue)
    print("PROBLEMS:", failed if failed else "none")


if __name__ == "__main__":
    main()
