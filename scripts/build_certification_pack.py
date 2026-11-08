"""Organizes the course's PDFs into an exam-first pack, and writes its study plan and index.

Usage: build_certification_pack.py plan   writes the study plan's markdown, to be built as a PDF
       build_certification_pack.py pack   copies PDFs into the pack and merges required reading
PDFs are copied and never moved, because the build pipeline owns 05-pdf-output/modules/.
"""
import pathlib
import shutil
import sys

from pypdf import PdfReader, PdfWriter

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "05-pdf-output/modules"
PACK = ROOT / "05-pdf-output/certification-pack"
GUIDE = ROOT / "01-source-map/CCAR-F-Exam-Guide-v1.0.pdf"

REQUIRED = [
    ("00-Start-Here", "Start here", [
        ("AI13-Study-Plan", "The route through this pack"),
        ("AI13L1-Exam-Strategy", "Format, method, distractor families and pacing"),
    ]),
    ("01-Domain-1-Agentic-Architecture-27pct", "Domain 1, 27%", [
        ("AI06L1-Workflows-or-Agents", "TS 1.6"),
        ("AI06L2-Agentic-Loop", "TS 1.1"),
        ("AI06L6-Multi-Agent", "TS 1.2, 1.3, 5.3"),
        ("AI06L7-Sessions-Harnesses", "TS 1.7, 5.4"),
        ("AI06L8-Hooks-Escalation", "TS 1.4, 1.5, 5.2"),
        ("AI13L2-SDK-and-CLI", "SDK reading level, and the CLI flag sheet"),
    ]),
    ("02-Domain-2-Tools-and-MCP-18pct", "Domain 2, 18%", [
        ("AI06L3-Tool-Design", "TS 2.1, 2.3"),
        ("AI06L4-MCP-Architecture", "TS 2.4"),
        ("AI06L5-MCP-Integration-Errors", "TS 2.2, 2.4, 5.3"),
        ("AI12L1-How-Claude-Code-Works", "TS 2.5, built-in tools"),
    ]),
    ("03-Domain-3-Claude-Code-20pct", "Domain 3, 20%", [
        ("AI12L2-CLAUDE-MD-Rules", "TS 3.1, 3.3"),
        ("AI12L3-Skills-Subagents-Hooks", "TS 3.2"),
        ("AI12L4-Plan-Mode-Refinement", "TS 3.4, 3.5"),
        ("AI12L5-CI-CD", "TS 3.6, 4.6, 1.6"),
    ]),
    ("04-Domain-4-Prompts-and-Output-20pct", "Domain 4, 20%", [
        ("AI03L1-Messages-API", "Foundation: tool_use, stop_reason, tool_choice, history"),
        ("AI03L2-Prompt-Engineering", "TS 4.1, 4.2"),
        ("AI03L3-Structured-Output", "TS 4.3, 4.4"),
        ("AI03L5-Caching-and-Batches", "TS 4.5. For caching, know only that it exists"),
        ("AI13L3-Pydantic-Resources", "Pydantic, retries, MCP resources"),
    ]),
    ("05-Domain-5-Context-and-Reliability-15pct", "Domain 5, 15%", [
        ("AI03L6-Context-Engineering", "TS 5.1"),
        ("AI05L4-Query-Context-Citations", "TS 5.6. Citations and provenance sections"),
        ("AI10L3-Evals", "TS 5.5. Sampling, calibration and segments sections"),
    ]),
    ("06-Scenarios-and-Design-Problems", "Scenarios", [
        ("AI13L4-Scenarios-Exercises", "All six scenarios, and the guide's four exercises"),
        ("AI06L9-DP-Support-Agent", "S1 worked in depth"),
        ("AI06L10-DP-Research-System", "S3 worked in depth"),
    ]),
    ("08-Final-Revision", "Final revision", [
        ("AI13L5-Final-Revision", "All 30 task statements, and guide vs current behavior"),
    ]),
]
PRACTICE = ("07-Practice", [
    ("AI13-Question-Bank-500", "500 by domain: Q1–135, Q136–225, Q226–325, Q326–425, Q426–500"),
    ("AI13-Mock-Exam-1", "S1, S2, S3, S6, in 60 items"),
    ("AI13-Mock-Exam-2", "S1, S4, S5, S6, in 60 items"),
    ("AI13-Mock-Exam-3", "S2, S3, S4, S5, in 60 items"),
    ("AI06-Exercises", "Agents and MCP, worked"),
    ("AI12-Exercises", "Claude Code, worked"),
    ("AI03-Exercises", "Prompts, output and context, worked"),
    ("AI13-Exercises", "Final-stretch reasoning, worked"),
])
OPTIONAL = {
    "1-Helpful-If-New-To-LLMs": ["AI02L1-Tokens", "AI02L6-Why-LLMs-Fail", "AI03L4-Thinking-Effort"],
    "2-Java-Implementation": ["AI08L1-Claude-in-Spring-Boot", "AI08L2-Streaming", "AI08L3-Production-Agent-Loop",
                              "AI08L4-MCP-Server-Java", "AI08L5-RAG-Service", "AI08L6-Async-AI",
                              "AI08L7-Testing-AI-Code", "AI08L8-DP-Banking-Assistant", "AI08-Exercises"],
    "3-AI-and-LLM-Fundamentals": ["AI01L1-Learning-From-Data", "AI01L2-Classical-ML-and-Metrics",
                                  "AI01L3-Neural-Networks", "AI01L4-Words-to-Vectors", "AI01-Exercises",
                                  "AI02L2-Attention", "AI02L3-Transformer", "AI02L4-How-LLMs-Train",
                                  "AI02L5-Inference-and-Serving", "AI02L7-Beyond-Text-Choosing-Models",
                                  "AI02-Exercises"],
    "4-Search-and-RAG": ["AI04L1-Embedding-Models", "AI04L2-ANN-In-Production", "AI04L3-Hybrid-and-Rerank",
                         "AI04L4-Choosing-a-Store", "AI04L5-DP-MultiTenant-Search", "AI04-Exercises",
                         "AI05L1-Why-RAG", "AI05L2-Ingestion-Chunking", "AI05L3-Contextual-Retrieval",
                         "AI05L5-RAG-Family", "AI05L6-Evaluating-RAG", "AI05L7-DP-Knowledge-Assistant",
                         "AI05-Exercises"],
    "5-Frameworks": ["AI07L1-Do-You-Need-a-Framework", "AI07L2-LangChain-LangGraph", "AI07L3-LlamaIndex-Others",
                     "AI07L4-Spring-AI-LangChain4j", "AI07-Exercises"],
    "6-Security-and-Production": ["AI09L1-Threat-Modelling", "AI09L2-Prompt-Injection", "AI09L3-Tools-MCP-Sandboxes",
                                  "AI09L4-PII-Tenancy-Audit", "AI09L5-Guardrails", "AI09-Exercises",
                                  "AI10L1-Reliability", "AI10L2-Gateways-Routing", "AI10L4-Observability",
                                  "AI10L5-Cost-Semantic-Cache", "AI10L6-Deployment-DR", "AI10L7-DP-LLM-Gateway",
                                  "AI10-Exercises"],
    "7-Advanced": ["AI11L1-Fine-Tuning-LoRA", "AI11L2-Self-Hosting", "AI11L3-Interpretability-Alignment",
                   "AI11L4-Autonomy", "AI11L5-Event-Driven-Sagas", "AI11-Exercises"],
}


def pages(name):
    path = SRC / f"{name}.pdf"
    return len(PdfReader(path).pages) if path.exists() else 0


def check_complete():
    listed = {n for _, _, items in REQUIRED for n, _ in items}
    listed |= {n for n, _ in PRACTICE[1]}
    listed |= {n for names in OPTIONAL.values() for n in names}
    actual = {p.stem for p in SRC.glob("*.pdf")}
    missing, extra = actual - listed, listed - actual - {"AI13-Study-Plan"}
    assert not missing and not extra, f"not placed: {sorted(missing)}; no such PDF: {sorted(extra)}"
    return len(actual)


def table(items):
    rows = ["| PDF | Pages | Covers |", "| --- | ---: | --- |"]
    rows += [f"| {n} | {pages(n)} | {why} |" for n, why in items]
    return "\n".join(rows)


def write_plan():
    required = sum(pages(n) for _, _, items in REQUIRED for n, _ in items if n != "AI13-Study-Plan")
    parts = []
    for folder, label, items in REQUIRED:
        if folder.startswith("00"):
            continue
        parts.append(f"**{label}** · `{folder}`, {sum(pages(n) for n, _ in items)} pages\n\n{table(items)}\n")
    optional = sum(pages(n) for names in OPTIONAL.values() for n in names)
    practice = sum(pages(n) for n, _ in PRACTICE[1])
    text = f"""<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Part V, Certification</div>

# CCAR-F Study Plan

## The Exam-First Route Through the Course

<div class="sub">What to read for the exam, in what order, what to skip, and how to know when you're ready.</div>

<table class="meta">
<tr><td>Exam</td><td>Claude Certified Architect – Foundations, guide v1.0, July 2026</td></tr>
<tr><td>Required reading</td><td>{required} pages</td></tr>
<tr><td>Practice</td><td>{practice} pages: 500 questions, 3 mocks and worked exercises</td></tr>
<tr><td>Optional background</td><td>{optional} pages, in <code>09-Optional-Background</code></td></tr>
</table>
</div>

<div class="lesson">

# The Exam-First Route

::: keyidea
==**Read by domain, test by domain, then take timed mocks.**== Spend your time in proportion to the guide's weights, and let your error log decide what to reread. Everything in the optional folder can wait until after the exam.
:::

**Study order.** The pack is arranged by the guide's five domains, so its numbered folders run in a different order from the course's modules. Read Domain 4's AI03 L1 first if the Messages API is new to you, since Domains 1 and 2 use its terms.

## What's required

{chr(10).join(parts)}

**Skim or skip inside required lessons.** The guide excludes detailed language implementation, so treat each lesson's **Java** section as optional for the exam. AI03 L5's caching detail, AI05 L4's retrieval mechanics and AI10 L3's general eval tooling go beyond the objectives, too. Those task-statement sections are what count. Certification lens sections, at the end of each lesson, tell you what's in scope.

## A four-week plan

This is my suggestion, at about 1.5–2 hours a day. Adjust it to your experience. The page counts show the load, and they aren't a promise about how long you'll take.

| Week | Days | Read | Practice |
| --- | ---: | --- | --- |
| 1 | 1–2 | `00-Start-Here`, the guide's sections 1–6 | Guide's 12 samples, untimed |
| 1 | 3–7 | Domain 1 | Bank Q1–135, in sets of about 30 |
| 2 | 8–10 | Domain 2 | Bank Q136–225 |
| 2 | 11–14 | Domain 3, and build guide Exercise 2 | Bank Q226–325 |
| 3 | 15–17 | Domain 4, and try guide Exercise 3 | Bank Q326–425 |
| 3 | 18–19 | Domain 5 | Bank Q426–500 |
| 3 | 20–21 | `06-Scenarios-and-Design-Problems` | Module exercises |
| 4 | 22 | Review the error log | **Mock 1, timed** |
| 4 | 23–24 | Restudy weak task statements | Official practice exam, if you have access |
| 4 | 25 | Review | **Mock 2, timed** |
| 4 | 26–27 | Restudy, `AI13-Exercises` | Retake bank misses |
| 4 | 28 | Final revision | **Mock 3, timed** |
| 4 | 29–30 | Handbook and guide, twice | Nothing new on the last day |

**Short on time?** A two-week version: read the Certification lens and revision box of each required lesson first, along with the handbook, then use bank results to choose which full lessons to read, and keep all three mocks.

## The guide's own preparation

The guide's sample questions and four exercises are the official advice. Do them, even if you do nothing else in `06`. If you can access the official practice exam, use it to find weak spots. One candidate says it reuses a pool, so a repeat score overstates readiness.

## How you know you're ready

These are my own suggested signals, and the guide publishes no raw-score cutoff. No practice score can predict the result.

| Signal | Target |
| --- | --- |
| First-attempt mock score | 80% or better |
| Lowest domain on a mock | 70% or better |
| Time on a mock | 110 minutes or less |
| Error log | No distractor family keeps recurring |
| Flag sheet and file locations | Recalled without notes |

## Before you book

1. Recheck the certification page for a newer guide, on or after **November 17, 2026.** A third-party prep site reports updates on that schedule for three other exams, without naming CCAR-F. I couldn't confirm that at the source.
2. Confirm the retake policy, eligibility and delivery rules on the official pages.
3. Follow the guide if a practice item here ever disagrees with it.

## What's not in this pack

- **Real exam questions.** None was used or sought, and all the practice items here are original.
- **Pass guarantees.** This is preparation material. It isn't affiliated with, or endorsed by, Anthropic.
- **Runs against a live model.** Without an API key, the code that calls models has been compiled but not run. The labs that don't need a model have been run, and each module's references say which is which.

::: notespace short Your plan: exam date, and weak domains
:::

</div>
"""
    target = ROOT / "02-modules/13-certification/study-plan.md"
    target.write_text(text, encoding="utf-8", newline="\n")
    print("wrote", target, "required", required, "practice", practice, "optional", optional)


def clean_target():
    """Removes only this script's own output folder, after checking where it is."""
    assert PACK.parent == ROOT / "05-pdf-output" and PACK.name == "certification-pack"
    if PACK.exists():
        shutil.rmtree(PACK)
    PACK.mkdir()


def build_pack():
    count = check_complete()
    clean_target()
    (PACK / "00-Start-Here").mkdir()
    shutil.copy2(GUIDE, PACK / "00-Start-Here/00-CCAR-F-Exam-Guide-v1.0.pdf")
    writer = PdfWriter()
    index = ["# CCAR-F Certification Pack\n",
             "Generated by `scripts/build_certification_pack.py`. Copies of PDFs from `05-pdf-output/modules/`, "
             "which stay the build output. **Start with `00-Start-Here/01-AI13-Study-Plan.pdf`.**\n",
             f"`CCAR-F-Required-Reading-All.pdf` joins all required reading in one bookmarked file.\n"]
    page_total = 0
    for folder, label, items in REQUIRED:
        out = PACK / folder
        out.mkdir(exist_ok=True)
        parent = writer.add_outline_item(label, len(writer.pages))
        index.append(f"\n## {folder}\n\n| File | Pages | Covers |\n| --- | ---: | --- |")
        for i, (name, why) in enumerate(items, start=1):
            target = out / f"{i:02d}-{name}.pdf"
            shutil.copy2(SRC / f"{name}.pdf", target)
            n = pages(name)
            page_total += n
            index.append(f"| {target.name} | {n} | {why} |")
            writer.append(str(SRC / f"{name}.pdf"), outline_item=None, import_outline=False)
            writer.add_outline_item(name, len(writer.pages) - n, parent=parent)
    writer.write(PACK / "CCAR-F-Required-Reading-All.pdf")
    folder, items = PRACTICE
    (PACK / folder).mkdir()
    index.append(f"\n## {folder}\n\n| File | Pages | Covers |\n| --- | ---: | --- |")
    for i, (name, why) in enumerate(items, start=1):
        shutil.copy2(SRC / f"{name}.pdf", PACK / folder / f"{i:02d}-{name}.pdf")
        index.append(f"| {i:02d}-{name}.pdf | {pages(name)} | {why} |")
    base = PACK / "09-Optional-Background"
    index.append("\n## 09-Optional-Background\n\nNot needed for the exam's objectives. Useful for real work.\n\n"
                 "| Folder | PDFs | Pages |\n| --- | ---: | ---: |")
    for sub, names in OPTIONAL.items():
        (base / sub).mkdir(parents=True)
        for name in names:
            shutil.copy2(SRC / f"{name}.pdf", base / sub / f"{name}.pdf")
        index.append(f"| {sub} | {len(names)} | {sum(map(pages, names))} |")
    (PACK / "README.md").write_text("\n".join(index) + "\n", encoding="utf-8", newline="\n")
    placed = len(list(PACK.rglob("*.pdf")))
    size = sum(p.stat().st_size for p in PACK.rglob("*.pdf")) / 1e6
    print(f"sources {count}, pack PDFs {placed}, required pages {page_total}, merged "
          f"{len(PdfReader(PACK / 'CCAR-F-Required-Reading-All.pdf').pages)}, {size:.0f} MB")


if sys.argv[1] == "plan":
    write_plan()
else:
    build_pack()
