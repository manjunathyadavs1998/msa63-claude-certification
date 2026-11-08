"""Scores a ticked exam simulation against its key, by domain, task statement and scenario.

Usage: python -I -X utf8 scripts/score_exam_sim.py <1|2|3> [ticked file]
"""
import collections
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SIMS = ROOT / "02-modules/13-certification/exam-simulations"
DOMAINS = {"1": "Agentic Architecture and Orchestration", "2": "Tool Design and MCP Integration",
           "3": "Claude Code Configuration and Workflows", "4": "Prompt Engineering and Structured Output",
           "5": "Context Management and Reliability"}


def ticked(path):
    answers, current = {}, None
    for line in path.read_text(encoding="utf-8").splitlines():
        m = re.match(r"### Question (\d+)", line)
        if m:
            current = int(m.group(1))
            answers[current] = []
        m = re.match(r"\s*[-*]\s*\[[xX]\]\s*([A-E])\.", line)
        if m and current:
            answers[current].append(m.group(1))
    return answers


def table(rows, title):
    print(f"\n{title}")
    for name in sorted(rows):
        right, total = rows[name]
        print(f"  {name}: {right}/{total} ({100 * right / total:.0f}%)")


def main():
    number = sys.argv[1]
    source = pathlib.Path(sys.argv[2]) if len(sys.argv) > 2 else SIMS / f"Exam-Simulation-{number}.md"
    key = json.loads((SIMS / "keys" / f"exam-simulation-{number}.json").read_text(encoding="utf-8"))
    answers = ticked(source)
    domain, task, scenario = (collections.defaultdict(lambda: [0, 0]) for _ in range(3))
    right, blank, missed = 0, [], []
    for q in key:
        got = sorted(answers.get(q["n"], []))
        ok = got == sorted(q["answer"])
        right += ok
        if not got:
            blank.append(q["n"])
        if not ok:
            missed.append((q, got))
        for rows, name in ((domain, f"{q['ts'][0]}. {DOMAINS[q['ts'][0]]}"), (task, f"TS {q['ts']}"), (scenario, q["scenario"])):
            rows[name][0] += ok
            rows[name][1] += 1
    print(f"Simulation {number}: {right}/{len(key)} ({100 * right / len(key):.0f}%), unanswered: {blank or 'none'}")
    table(domain, "By domain")
    table(scenario, "By scenario")
    table(task, "By task statement")
    print("\nMissed")
    for q, got in missed:
        print(f"  Q{q['n']} TS {q['ts']} ({q['level']}): you {got or 'blank'}, answer {q['answer']}. {q['why']}")


if __name__ == "__main__":
    main()
