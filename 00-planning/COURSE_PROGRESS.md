# Course Progress

**Resume point. Read this first in every session.**

Last updated: 2026-11-06.

## Status

| Module | State | Output |
| --- | --- | --- |
| Planning | Done. User approved with "start" | `00-planning/` |
| Format pilot | Superseded by AI05 L3. The source and old PDF remain for reference | `05-pdf-output/prototype/` |
| **AI01 AI and ML Fundamentals** | **Complete** | 4 lesson PDFs (83 pages) + exercises (5 pages) |
| **AI02 Generative AI and LLM Fundamentals** | **Complete** | 7 lesson PDFs (113 pages) + exercises (4 pages) |
| **AI03 Working With Claude** | **Complete** | 6 lesson PDFs (121 pages) + exercises (6 pages) |
| **AI04 Vector Databases and Search** | **Complete** | 5 lesson PDFs (86 pages) + exercises (5 pages) |
| **AI05 RAG** | **Complete** | 7 lesson PDFs (125 pages) + exercises (5 pages) |
| **AI06 Agents and MCP** | **Complete** | 10 lesson PDFs (178 pages) + exercises (6 pages) |
| **AI07 AI Frameworks** | **Complete** | 4 lesson PDFs (65 pages) + exercises (7 pages) |
| **AI08 AI Application Development in Java** | **Complete** | 8 lesson PDFs (118 pages) + exercises (5 pages) |
| **AI09 AI Security** | **Complete** | 5 lesson PDFs (101 pages) + exercises (7 pages) |
| **AI10 AI Production Engineering** | **Complete** | 7 lesson PDFs (129 pages) + exercises (8 pages) |
| **AI11 Advanced AI Topics** | **Complete** | 5 lesson PDFs (83 pages) + exercises (8 pages) |
| **AI12 Claude Code** | **Complete** | 5 lesson PDFs (86 pages) + exercises (8 pages) |
| **AI13 Certification Preparation** | **Complete.** Study plan, 5 lessons, exercises, 500-question bank and 3 mocks | `AI13-*.pdf`. Pack in `05-pdf-output/certification-pack/`. Report: `07-validation/reports/AI13.md` |

## Next action

The course is complete. If the user continues:

1. **Recheck for a newer exam guide on or after 2026-11-17.** A third-party site reports new guides on that date for three other exams, without naming CCAR-F. If CCAR-F's guide changes, rerun `scripts/audit_exam_coverage.py` and update AI13 and the mocks.
2. After editing any lesson, rebuild its PDF, then run `python -X utf8 scripts/build_certification_pack.py plan`, rebuild the study plan, and run `... pack`.
3. New PDF names must be added to the manifest in that script, as its completeness check will otherwise stop the build.

Open questions for the user: API key to run model-calling code, Boot 4, the "©" typo in HLD `M10A-Distributed-Consistency.pdf` p11, exam eligibility, and lesson length.

**AI12 facts AI13 may rely on:** `--bare`, `dontAsk` and the Action aren't in the guide. Exam answers follow the guide. Nothing in AI12 ran through a live model.

## Workflow that worked

1. Write and **run** the Java first, so that lessons quote real numbers.
2. Write the lessons.
3. Recompute every hand-derived number in a `scripts/verify_<module>.py`. This found real errors in both AI01 and AI02.
4. Build using `scripts/build_module.sh`.
5. Make contact sheets using `scripts/contact_sheets.py`, and **look at every figure page**. Then fix and rebuild.
6. Write `references.md`, `exercises.md` and the validation report, then update this file and `lessons.json`.

**Check from AI05:** compare every line of Java in the lessons with the source files. It caught a router snippet that had never been compiled. Write Java containing backslashes with the Write tool, because a shell heredoc here altered them. Maven occasionally hangs, so always use `timeout`, and re-run the lab after any edit to compare its output with what's quoted.

**Check from AI06:** quotation marks are only for text the source actually quotes. A fetch tool's outline paraphrases, so don't quote its summaries. Running a lab, not just compiling it, finds classpath problems.

**Check from AI07:** real libraries can be run with no key, by implementing their model interface with a script and recording requests. It found things no doc states, so use it for all framework code. Print outcomes on every path, not just on exceptions.

**Check from AI08:** run Maven with `< /dev/null`, which stopped the repeated hangs here. Point the real SDK at a local fake server (`FakeAnthropicServer`) to test requests with no key. Automated line wrapping needs a loop guard. Never write Java with backslashes through a shell heredoc.

**Diagram lesson from AI02:** a chain of more than about 6 nodes shrinks to tiny text. Merge nodes or add branches. Never use a back-edge in `TB`.

**Check from AI10:** read the pricing page itself for each module that quotes prices. The bundled reference disagreed with it, and the error reached two earlier lessons. A lab that attacks or stresses this course's own code keeps finding real bugs, so keep doing that.

## Decisions recorded

| Decision | Why |
| --- | --- |
| Modules coded AI01–AI13 | Avoids collision with HLD, LLD, Java and AWS numbering |
| Vector search before RAG | RAG depends on it |
| Added AI03 and AI12 | Required by RAG, agents and 55% of the exam (Domains 3, 4 and 5) |
| Certification last | User's request |
| Forked the LLD pipeline | Has Java highlighting. HLD pipeline stays untouched |
| `style`, not `classDef`, for node colors | `classDef` produced unreadable nodes |
| `h2 { display: table }` and `break-after: avoid` | Stops stranded headings |
| Figure `max-height` 120mm | Stops small diagrams pushing content to later pages |
| Post-render `©` back to `(c)` | The typographer corrupted lettered exercise parts |
| Don't pad lessons to their page budget | AI01 was 83 of 100 pages, and AI02 113 of 180. Budgets are estimates, and the real total will likely be lower than 2,432 |
| Exercises are also built to PDF | The earlier courses only kept them as Markdown. `<details>` isn't used, so that solutions print |
| Theory lessons may use a consequences table instead of a three-stage architecture | AI02 L2 and L3 would have had a forced design otherwise |
| Code ligatures off in `course-notebook.css` | They drew `->` as an arrow and `(?<=` as `(?≤`, and broke copying. AI01–AI05 rebuilt |
| Scripted model answers test harness logic offline | Lets `Extraction` run with no key, and the lesson says so |

## Open items

- User to confirm exam eligibility with their employer.
- Optional: set `ANTHROPIC_API_KEY`, so API-calling samples can be run as well as compiled.
- Before AI12 and AI13: check the certification page for a newer guide.
- Spring Boot 3.5's open-source support ended on 30 June 2026. The course stays on Boot 3 by default, as the user didn't choose otherwise. Tell the user if this should change.
- Confirmed in AI12: `--continue` and `--fork-session`. The hooks guide was read, though the full hooks reference and the MCP page beyond AI06's reading weren't.
- **HLD issue found, not fixed:** `system-design-course` `M10A-Distributed-Consistency.pdf`, page 11, prints "Consistency ©" rather than "(C)". Awaiting the user's decision.
- Should lessons be longer? AI03 was written deeper (121 of 156 pages). Awaiting the user's view.

## Extra notes (outside AI01–AI13)

- 2026-11-07: `02-modules/extras/claude-chat-at-scale.md` → `05-pdf-output/extras/Claude-Chat-At-Scale.pdf`, a reference design for chat context at scale, in the notebook style. Not part of the certification pack. Java demo: `code/extras/ContextAssembler.java`, compiled and run without a model. It's labeled as not being Anthropic's real system.

## Exam simulations (added 2026-11-08)

Three new tick-box mocks of 60 items each are in `02-modules/13-certification/exam-simulations/`, built from `bank/sim1.txt`–`sim3.txt` by `scripts/build_exam_sims.py`. To evaluate one, run `python -I -X utf8 scripts/score_exam_sim.py <1|2|3>`, then explain the misses from `keys/`. Checks passed: domain counts 16/11/12/12/9, 15 per scenario, answer letters balanced within 1, correct answer longest in 18–28% of single-answer items, and no stem overlap with the bank, older mocks or guide samples at the set cutoffs. Known limits: untrialed, no independent review of the keys, stem medians of 36, 30 and 28 words against 46 in the guide's samples, and multiple-response counts of 4, 4 and 6 are my choice.
