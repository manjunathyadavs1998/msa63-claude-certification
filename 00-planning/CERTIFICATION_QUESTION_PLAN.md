# Certification Question Plan

All questions are original practice material written from the public exam guide. None are real or leaked exam questions, and no score here predicts your exam result.

## What the real exam asks

Per the official guide: 60 items in 120 minutes, across 4 scenarios drawn from 6. The formats are **only** multiple-choice and multiple-response, with each item stating how many to select.

The other formats you asked for in `Idea.md` won't appear on the exam. They're still useful, since writing out a sequence or debugging code builds the judgment that scenario questions test. So they're used for practice and never in mocks.

## Question volume

| Where | Format | Count |
| --- | --- | ---: |
| Domain 1 bank (AI13 L2) | Exam style | 66 |
| Domain 2 bank (AI13 L3) | Exam style | 44 |
| Domain 3 bank (AI13 L4) | Exam style | 49 |
| Domain 4 bank (AI13 L5) | Exam style | 49 |
| Domain 5 bank (AI13 L6) | Exam style | 37 |
| Three mock exams (AI13 L9–L11) | Exam style | 180 |
| Question Types Lab (AI13 L8) | 5 each of the 16 practice-only formats | 80 |
| | **Total in AI13** | **505** |

Banks total 245, split by official weight. That's at least 8 per task statement (30 × 8 = 240).

**In addition, every lesson has a Certification lens:** 3 questions in a lesson that only supports the exam, and 5 in a lesson that teaches an in-scope task statement. Module exercises add 6 worked problems each.

## The 18 formats

| # | Format | Used in |
| ---: | --- | --- |
| 1 | Single-answer MCQ | **Exam**, mocks, banks |
| 2 | Multiple-select | **Exam**, mocks, banks |
| 3 | Scenario-based architecture | **Exam style**, as the framing for 1 and 2 |
| 4 | Real-world problem solving | Lab, exercises |
| 5 | Architecture design and component selection | Lab, design problems |
| 6 | Code analysis and output prediction | Lab |
| 7 | Java implementation and debugging | Lab, AI08 |
| 8 | Sequence-flow and execution order | Lab |
| 9 | Configuration-based (`.mcp.json`, frontmatter, CLI flags) | Lab, AI12 |
| 10 | Error identification and troubleshooting | Lab |
| 11 | True/False | Lab |
| 12 | Assertion and reasoning | Lab |
| 13 | Match the following | Lab |
| 14 | Case study | Lab, design problems |
| 15 | Best architectural decision | Lab, "defend a decision" exercises |
| 16 | Performance and cost optimization | Lab |
| 17 | Security and failure handling | Lab, AI09 |
| 18 | Complex multi-agent design | Lab, AI06 |

## Mock exam blueprint

Each mock has 60 items at 2 minutes each. Domain counts are 16 / 11 / 12 / 12 / 9, which are my rounding of the official weights. Every scenario appears in exactly two mocks:

| Mock | Scenarios |
| --- | --- |
| 1 | S1 Customer Support, S2 Code Generation, S3 Research System, S6 Data Extraction |
| 2 | S1 Customer Support, S4 Developer Productivity, S5 CI/CD, S6 Data Extraction |
| 3 | S2 Code Generation, S3 Research System, S4 Developer Productivity, S5 CI/CD |

After each mock you record percent correct by domain, matching the real score report. **Targets:** 80%+ overall and no domain below 70%. These are my targets, not official figures. The guide doesn't publish a raw-score equivalent of its scaled 720.

## How wrong answers are written

The guide's samples reuse a small set of tempting wrong answers. Each question draws distractors from different types, so you learn to recognize them:

| Distractor type | Looks like | Why it's wrong |
| --- | --- | --- |
| Probabilistic fix | "Strengthen the system prompt" for a guaranteed rule | Prompts can't guarantee compliance |
| Over-engineering | Add a classifier or routing layer as a first step | Not proportionate to the root cause |
| Wrong layer | Blame a downstream agent for bad decomposition | Treats a symptom |
| Invented feature | `CLAUDE_HEADLESS`, `--batch` | Doesn't exist |
| Solves a different problem | Sentiment analysis for case complexity | Measures the wrong thing |
| Bigger hammer | A larger context window for a quality problem | Capacity isn't attention |
| Hiding failure | Return empty results as success | Removes recovery options |

## Quality rules

1. **Balance answer letters in every set:** each letter between 20% and 30%. The 30-question starter bank from the first pass came out at A 7, B 12, C 9, D 2, so you could game it. It will be rebuilt, not reused. A script checks this.
2. **Length must not give it away.** The correct option shouldn't routinely be the longest.
3. **Every answer explains all the options:** why the right one is right, and why each other one is wrong.
4. **Each question records:** task statement, scenario, difficulty, format and distractor types.
5. **Where the guide and current products differ,** the exam answer follows the guide, and the explanation notes what you'd build today.
6. **Coverage is checked by script,** so no task statement falls below 8 bank questions.
7. **Don't copy or lightly reword** the guide's 12 samples. Do those separately, as your best signal of real style.
