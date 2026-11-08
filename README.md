# Claude Certified Architect – Foundations (CCAR-F): Study Course and Practice

A complete self-study course for the **Claude Certified Architect – Foundations** exam (code CCAR-F). It has 13 modules of notebook-style lessons, a certification pack ordered by exam domain, a 500-question practice bank, six timed mock exams, worked exercises, and compiled Java samples.

> [!IMPORTANT]
> **This is an independent study resource. It isn't affiliated with or endorsed by Anthropic.** Every question here is original, written from the public exam guide. None is a real or leaked exam question, the company names in scenarios are invented, and a practice score doesn't predict your exam result.

## Contents

- [The exam at a glance](#the-exam-at-a-glance)
- [Start here](#start-here)
- [Suggested study path](#suggested-study-path)
- [Practice and mock exams](#practice-and-mock-exams)
- [Exercises](#exercises)
- [Repository layout](#repository-layout)
- [Building the PDFs](#building-the-pdfs)
- [Running the code](#running-the-code)
- [Accuracy and sources](#accuracy-and-sources)

## The exam at a glance

From the official exam guide, v1.0, effective July 2026:

| Item | Detail |
| --- | --- |
| Questions | 60 multiple-choice and multiple-response items. Each says how many answers to select |
| Time | 120 minutes |
| Structure | 4 scenarios, drawn at random from 6 |
| Pass mark | Scaled score of 720, on a 100–1,000 scale. This isn't the same as 72% correct |
| Score report | Pass or fail, the scaled score, and percent correct by domain |

| Domain | Weight | Where it's taught |
| --- | ---: | --- |
| 1. Agentic Architecture and Orchestration | 27% | AI06, AI13 |
| 2. Tool Design and MCP Integration | 18% | AI06, AI12 |
| 3. Claude Code Configuration and Workflows | 20% | AI12 |
| 4. Prompt Engineering and Structured Output | 20% | AI03, AI13 |
| 5. Context Management and Reliability | 15% | AI03, AI05, AI06, AI10 |

The six scenarios are Customer Support Resolution Agent, Code Generation with Claude Code, Multi-Agent Research System, Developer Productivity with Claude, Claude Code for Continuous Integration, and Structured Data Extraction.

**Download the official guide** from the [certification page](https://anthropic-partners.skilljar.com/claude-certified-architect-foundations-certification). It isn't included here, and you should check there for a newer version before you book.

## Start here

| If you want to… | Open |
| --- | --- |
| Follow a plan from start to finish | `05-pdf-output/certification-pack/00-Start-Here/01-AI13-Study-Plan.pdf` |
| Learn the exam format and how to answer | `05-pdf-output/certification-pack/00-Start-Here/02-AI13L1-Exam-Strategy.pdf` |
| Study one domain | `05-pdf-output/certification-pack/0N-Domain-…/` |
| Practise questions | `05-pdf-output/certification-pack/07-Practice/` |
| Revise in the final days | `05-pdf-output/certification-pack/08-Final-Revision/01-AI13L5-Final-Revision.pdf` |
| See which lesson teaches each exam objective | `01-source-map/EXAM_COVERAGE_MATRIX.md` |
| Read the exam blueprint in plain words | `01-source-map/CERTIFICATION_BLUEPRINT.md` |

The certification pack's own [README](05-pdf-output/certification-pack/README.md) lists every PDF, its page count and the task statements it covers.

## Suggested study path

1. **Read the study plan and the exam strategy** in `00-Start-Here`.
2. **Work through the five domain folders,** in order of weight: Domain 1, then 3 and 4, then 2, then 5.
3. **After each domain, answer that domain's section** of the 500-question bank, one question at a time, and reread the lesson for every miss.
4. **Do the exercises** in `07-Practice`. Each set ends with a "defend a decision" problem.
5. **Sit the mock exams under timed conditions,** a few days apart, so that each one tests understanding rather than memory.
6. **Keep a list of your wrong answers,** each with the rule you missed, and read it on the day before the exam.

A sensible target, which is a study suggestion and not an official figure: **80% or more on the mocks, with no domain below 70%.**

## Practice and mock exams

| Set | Questions | Format | Location |
| --- | ---: | --- | --- |
| Practice bank | 500 | By domain, answers at the end | `02-modules/13-certification/question-bank-500.md`, and as a PDF in `07-Practice` |
| Mock exams 1–3 | 3 × 60 | Timed, answers at the end | `02-modules/13-certification/mock-1.md` to `mock-3.md`, and as PDFs |
| Exam simulations 1–3 | 3 × 60 | **Tick-box files, scored by a script** | `02-modules/13-certification/exam-simulations/` |

### Taking an exam simulation

The exam simulations follow the real format: 4 scenario sets of 15, with 16, 11, 12, 12 and 9 items across Domains 1–5, which is a rounding of the official weights. Their questions are longer and denser than the earlier mocks.

1. Open `Exam-Simulation-1.md` and set a 120-minute timer.
2. Tick each answer by changing `- [ ]` to `- [x]`. Each item says how many to select.
3. **Don't open the `keys/` folder** until you've finished.
4. Score it by domain, scenario and task statement:

```bash
python -I -X utf8 scripts/score_exam_sim.py 1
# or score a copy saved somewhere else
python -I -X utf8 scripts/score_exam_sim.py 1 path/to/my-attempt.md
```

The report lists every miss with its explanation. The full answer key with explanations is in `exam-simulations/keys/`.

## Exercises

Each module has an `exercises.md` with six worked exercises and solutions, ending with a "defend a decision" problem. The certification pack includes the exam-relevant sets as PDFs:

| Exercises | Topic |
| --- | --- |
| `02-modules/06-agents-mcp/exercises.md` | Agents, tool design, MCP |
| `02-modules/12-claude-code/exercises.md` | Claude Code configuration and CI |
| `02-modules/03-claude-api-prompting-context/exercises.md` | Prompting, structured output, context |
| `02-modules/13-certification/exercises.md` | Six exam-style problems: constraints, SDK configuration, failures, batch planning |

The other modules' exercises are in the same place in each module folder.

## Repository layout

```text
00-planning/       course roadmap, question plan, progress and source register
01-source-map/     exam blueprint, objective-to-lesson coverage matrix, playlist analysis
02-modules/        lesson sources in Markdown: the source of truth
  01-…12-          one folder per module: notes-a.md, notes-b.md…, references.md, exercises.md
  13-certification/  exam strategy, practice bank, mocks, exam simulations
04-shared-assets/  notebook-style CSS for the PDFs
05-pdf-output/
  certification-pack/  the lesson PDFs, ordered by exam domain
code/              Java 21 samples, one Maven project per module
scripts/           PDF build, validation, question-bank and mock generators, scoring
07-validation/reports/  quality report for each module
```

| Module | Topic | Exam relevance |
| --- | --- | --- |
| AI01 | AI and ML fundamentals | Background |
| AI02 | Generative AI and LLM fundamentals | Background |
| AI03 | Claude API, prompting and context | **Core: Domains 4 and 5** |
| AI04 | Vector search | Background |
| AI05 | Retrieval-augmented generation | Partly: provenance, Domain 5 |
| AI06 | Agents and MCP | **Core: Domains 1 and 2** |
| AI07 | Frameworks | Background |
| AI08 | Java with Claude | Background |
| AI09 | Security | Supports Domain 1 |
| AI10 | Production | Partly: evaluation and human review, Domain 5 |
| AI11 | Advanced patterns | Background |
| AI12 | Claude Code | **Core: Domain 3** |
| AI13 | Certification preparation | **Core: all domains** |

Background modules are in `certification-pack/09-Optional-Background/`.

## Building the PDFs

The Markdown in `02-modules/` is the source. The PDFs are generated from it with headless Chrome.

**Requirements:** Node.js 18+, Python 3.11+ with `pypdf` and `pypdfium2`, and Google Chrome or Chromium.

```bash
npm install
pip install pypdf pypdfium2

# Build one lesson
node scripts/build_pdf.mjs --in 02-modules/06-agents-mcp/notes-b.md \
     --out 05-pdf-output/modules/AI06L2-Agentic-Loop.pdf \
     --title "The Agentic Loop" --module "AI06 - Agents and MCP" --style notebook
python -I -X utf8 scripts/add_bookmarks.py 05-pdf-output/modules/AI06L2-Agentic-Loop.pdf 02-modules/06-agents-mcp/notes-b.md
python -I -X utf8 scripts/validate_pdf.py 05-pdf-output/modules/AI06L2-Agentic-Loop.pdf 07-validation/ai06/pages

# Rebuild the exam simulations and their keys
python -I -X utf8 scripts/build_exam_sims.py
```

`scripts/build_module.sh` builds a whole module, and `scripts/build_certification_pack.py` reassembles the certification pack from the module PDFs.

## Running the code

Java samples target **Java 21** and use the official `com.anthropic:anthropic-java` SDK, with Spring Boot 3 where noted. Each module in `code/` is a Maven project.

```bash
cd code/ai08-java
mvn -q compile
mvn -q test
```

Samples that call the API need `ANTHROPIC_API_KEY` set in your environment. Never commit a key. Some samples have only been compiled rather than run against a live model, and each lesson says which.

## Accuracy and sources

- **The exam guide decides exam answers.** Where current Anthropic documentation differs, for example the `Task` tool now being called `Agent`, the lessons give the exam's answer and add a *Build today* note.
- **Claims are labeled** as documented and cited, as an engineering recommendation, or as an illustrative figure. `00-planning/SOURCE_REGISTER.md` records what was actually read.
- **The practice questions haven't been trialed** against the real exam, and candidates have reported the real one as harder and wordier.

If you find an error, please open an issue with the file, the question number and a source.
