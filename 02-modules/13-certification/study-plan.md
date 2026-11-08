<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Part V, Certification</div>

# CCAR-F Study Plan

## The Exam-First Route Through the Course

<div class="sub">What to read for the exam, in what order, what to skip, and how to know when you're ready.</div>

<table class="meta">
<tr><td>Exam</td><td>Claude Certified Architect – Foundations, guide v1.0, July 2026</td></tr>
<tr><td>Required reading</td><td>487 pages</td></tr>
<tr><td>Practice</td><td>287 pages: 500 questions, 3 mocks and worked exercises</td></tr>
<tr><td>Optional background</td><td>934 pages, in <code>09-Optional-Background</code></td></tr>
</table>
</div>

<div class="lesson">

# The Exam-First Route

::: keyidea
==**Read by domain, test by domain, then take timed mocks.**== Spend your time in proportion to the guide's weights, and let your error log decide what to reread. Everything in the optional folder can wait until after the exam.
:::

**Study order.** The pack is arranged by the guide's five domains, so its numbered folders run in a different order from the course's modules. Read Domain 4's AI03 L1 first if the Messages API is new to you, since Domains 1 and 2 use its terms.

## What's required

**Domain 1, 27%** · `01-Domain-1-Agentic-Architecture-27pct`, 105 pages

| PDF | Pages | Covers |
| --- | ---: | --- |
| AI06L1-Workflows-or-Agents | 16 | TS 1.6 |
| AI06L2-Agentic-Loop | 18 | TS 1.1 |
| AI06L6-Multi-Agent | 20 | TS 1.2, 1.3, 5.3 |
| AI06L7-Sessions-Harnesses | 18 | TS 1.7, 5.4 |
| AI06L8-Hooks-Escalation | 18 | TS 1.4, 1.5, 5.2 |
| AI13L2-SDK-and-CLI | 15 | SDK reading level, and the CLI flag sheet |

**Domain 2, 18%** · `02-Domain-2-Tools-and-MCP-18pct`, 68 pages

| PDF | Pages | Covers |
| --- | ---: | --- |
| AI06L3-Tool-Design | 17 | TS 2.1, 2.3 |
| AI06L4-MCP-Architecture | 16 | TS 2.4 |
| AI06L5-MCP-Integration-Errors | 19 | TS 2.2, 2.4, 5.3 |
| AI12L1-How-Claude-Code-Works | 16 | TS 2.5, built-in tools |

**Domain 3, 20%** · `03-Domain-3-Claude-Code-20pct`, 70 pages

| PDF | Pages | Covers |
| --- | ---: | --- |
| AI12L2-CLAUDE-MD-Rules | 16 | TS 3.1, 3.3 |
| AI12L3-Skills-Subagents-Hooks | 17 | TS 3.2 |
| AI12L4-Plan-Mode-Refinement | 16 | TS 3.4, 3.5 |
| AI12L5-CI-CD | 21 | TS 3.6, 4.6, 1.6 |

**Domain 4, 20%** · `04-Domain-4-Prompts-and-Output-20pct`, 98 pages

| PDF | Pages | Covers |
| --- | ---: | --- |
| AI03L1-Messages-API | 19 | Foundation: tool_use, stop_reason, tool_choice, history |
| AI03L2-Prompt-Engineering | 22 | TS 4.1, 4.2 |
| AI03L3-Structured-Output | 23 | TS 4.3, 4.4 |
| AI03L5-Caching-and-Batches | 21 | TS 4.5. For caching, know only that it exists |
| AI13L3-Pydantic-Resources | 13 | Pydantic, retries, MCP resources |

**Domain 5, 15%** · `05-Domain-5-Context-and-Reliability-15pct`, 59 pages

| PDF | Pages | Covers |
| --- | ---: | --- |
| AI03L6-Context-Engineering | 21 | TS 5.1 |
| AI05L4-Query-Context-Citations | 18 | TS 5.6. Citations and provenance sections |
| AI10L3-Evals | 20 | TS 5.5. Sampling, calibration and segments sections |

**Scenarios** · `06-Scenarios-and-Design-Problems`, 56 pages

| PDF | Pages | Covers |
| --- | ---: | --- |
| AI13L4-Scenarios-Exercises | 19 | All six scenarios, and the guide's four exercises |
| AI06L9-DP-Support-Agent | 17 | S1 worked in depth |
| AI06L10-DP-Research-System | 20 | S3 worked in depth |

**Final revision** · `08-Final-Revision`, 17 pages

| PDF | Pages | Covers |
| --- | ---: | --- |
| AI13L5-Final-Revision | 17 | All 30 task statements, and guide vs current behavior |


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
