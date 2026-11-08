<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Part V, Certification</div>

# Certification, Lesson 1

## How the Exam Works, and How to Take It

<div class="sub">Knowing the material isn't enough on a 60-item scenario exam. You also need a method for choosing between two answers that both seem to work, and a pace that survives dense reading.</div>

<table class="meta">
<tr><td>Module</td><td>AI13 of 13</td></tr>
<tr><td>Lesson</td><td>1 of 5</td></tr>
<tr><td>Prerequisites</td><td>None. Read this first, then again the week before the exam</td></tr>
<tr><td>Exam relevance</td><td><b>Every item</b></td></tr>
<tr><td>Reading time</td><td>~25 minutes</td></tr>
</table>
</div>

<div class="toc">

## Contents

1. [How the Exam Works, and How to Take It](#how-the-exam-works-and-how-to-take-it)

</div>

<div class="lesson">
<div class="lesson-meta">Lesson 1 of 5 · Certification</div>

# How the Exam Works, and How to Take It

::: keyidea
==**The exam asks which option is best in this situation, not which option is true.**== Find the constraint in the scenario, reject what breaks it, then prefer the fix that addresses the cause at the right layer. Keep two minutes per item, and flag what's slow.
:::

<div class="prereq"><b>Before:</b> nothing. <b>Teaches:</b> the exam's format, how items are built, an elimination method, the distractor families, pacing, and how to use practice results. <b>Used by:</b> the mocks and the final revision.</div>

## The hook: two right answers

Here's the kind of situation candidates describe. An item offers a prompt instruction and a hook. Both would reduce the problem. You've used both in real work, so both feel right, and you spend four minutes deciding. Multiply that by fifteen, and the last scenario gets rushed.

The way out is to stop asking "would this work?" and ask "what does this scenario require?" If it says a rule must **always** hold, a prompt can't be the answer, because prompts have a non-zero failure rate. That one word settles the item.

## Intuition: a building inspection

An inspector doesn't ask whether a repair could work. They ask whether it meets the code for this building. A bucket under a leak "works". It still fails the inspection, since it treats the symptom and not the cause. Most exam distractors are buckets: plausible, partly effective and at the wrong layer.

## The facts, from the exam guide

| Fact | Exam guide v1.0 |
| --- | --- |
| Code and name | CCAR-F, Claude Certified Architect – Foundations |
| Items | 60, multiple-choice and multiple-response. Each states how many to select |
| Structure | 4 scenarios, picked at random from 6 |
| Time | 120 minutes, so 2 minutes per item |
| Passing | Scaled 720 on a 100–1,000 scale |
| Fee | \$125 USD |
| Validity | 12 months from award |
| Results | Pass or fail with a scaled score, plus percent correct by domain |

| Domain | Weight | About how many of 60 |
| --- | --- | --- |
| 1 Agentic Architecture and Orchestration | 27% | 16 |
| 2 Tool Design and MCP Integration | 18% | 11 |
| 3 Claude Code Configuration and Workflows | 20% | 12 |
| 4 Prompt Engineering and Structured Output | 20% | 12 |
| 5 Context Management and Reliability | 15% | 9 |

**The item counts are my rounding of the weights.** They aren't official, and the mix you get depends on your four scenarios.

==**The guide says the exam is criterion-referenced, and the domain percentages on your report don't determine the result.**== So don't try to pass by writing off a domain. With 4 scenarios drawn from 6, you can't know which you'll get, so study all six.

### Check these before you book

The guide is the authority, but a few things change. Confirm them on the official certification page before relying on them:

- **A newer guide.** A prep site relaying Pearson VUE's program page reports updated content from December 7, 2026, for three other exams (CCAO-F, CCDV-F, CCAR-P), with guides due November 17, 2026. It doesn't name CCAR-F. *This comes from a third party, and I couldn't confirm it at the source.* Recheck on or after November 17, 2026.
- **Retake and validity rules.** One candidate's blog states a six-month wait after failing and six-month validity. That conflicts with the guide's 12 months and the retake rules recorded in this course's blueprint, so trust the official policy.
- **Eligibility and delivery.** Follow the program's own instructions.

## Mechanism: how an item is built

An item usually has four parts, and the guide's own samples follow this shape:

```mermaid caption="Anatomy of a scenario item. The constraint is what separates the best answer from the merely workable ones."
flowchart TB
  A[Scenario frame and observed problem] --> C[Constraint: always, first, minimal change]
  C --> D[Best answer fixes the cause, at the right layer]
  C --> E[Distractors: partial, wrong layer, or overbuilt]
  style D fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
  style E fill:#FBD5E0,stroke:#B3261E,stroke-width:2px
  style C fill:#FFF3C4,stroke:#8A6D00,stroke-width:2px
```

### Reading the constraint

| Words in the item | What they point to |
| --- | --- |
| "Must always", "guaranteed", "compliance", "financial" | Code: a hook, a gate or backend enforcement, and not a prompt |
| "First", "most effective first step", "most likely cause" | The simplest fix for the cause. Usually better descriptions, examples or a config correction, before new infrastructure |
| "Minimal", "least effort", "low overhead" | Rule out new services, new agents and rewrites |
| "All team members", "new hire doesn't get it" | Project scope and version control, not `~/.claude/` |
| "Overnight", "weekly", "can wait" | Batch API |
| "Blocking", "pre-merge", "user is waiting" | Synchronous API |
| "Spread across the codebase" | A path-scoped rule with a glob, not a directory `CLAUDE.md` |
| "Might not be in the document" | A nullable field, not a required one |
| "CI", "pipeline", "hangs" | `-p`, with `--output-format json` and `--json-schema` for parsing |

## A four-step method for every item

```mermaid caption="The method, in order. Most items are settled at step 3, before you compare the survivors."
flowchart TB
  A[1. Read the final question first] --> B[2. Mark the constraint in the scenario]
  B --> C[3. Eliminate what breaks it, or is at the wrong layer]
  C --> D[4. Between survivors, pick the root-cause fix]
  style C fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
```

Step 1 matters because the stems are long. Knowing what's being asked tells you which details matter as you read.

## Why the obvious habits fall short

| Habit | Why it fails here | Do this instead |
| --- | --- | --- |
| Pick what you do at work | Your setup may differ from what the guide teaches | Answer from the guide's reasoning |
| Pick the most sophisticated design | The exam often rewards the simplest effective fix | Check for a simpler root-cause fix first |
| Pick the longest answer | No reliable rule, and this course's own items are checked for that tell | Judge the content |
| Answer from memory of a similar question | A changed word changes the answer | Reread the constraint |
| Spend as long as it takes | Later scenarios get rushed | Flag at about 2.5 minutes and move on |

## The distractor families

These are the recurring wrong answers, and each has a test question. This list is this course's own analysis of the guide's objectives and its 12 samples. It isn't an official list.

| Family | Looks like | Test | Task statements |
| --- | --- | --- | --- |
| **Prompt for a guarantee** | "Add a strongly worded instruction" | Must it always hold? Then code | 1.4, 1.5 |
| **Wrong signal** | Parse "done" in text, escalate on sentiment, filter on self-reported confidence | Is it the real signal, or a proxy? | 1.1, 4.1, 5.2 |
| **Overbuilding** | A routing classifier or a new agent, when the fix is a description | Is the simple fix tried yet? | 2.1, 1.6 |
| **Under-scoping** | Personal config for a team need | Who needs this, and how do they get it? | 2.4, 3.1, 3.2 |
| **Wrong API tempo** | Batch for pre-merge, or synchronous for an overnight job | Is something blocked waiting? | 4.5 |
| **Retry the unfixable** | Keep retrying when the data isn't in the document | Could a retry change the outcome? | 2.2, 4.4 |
| **Silent failure** | Empty result returned as success | Can the caller tell an outage from no matches? | 2.2, 5.3 |
| **All-or-nothing failure** | Kill the workflow on one error | Can partial results continue? | 5.3 |
| **Assumed inheritance** | "The subagent can see what the coordinator saw" | Was it passed in the prompt? | 1.3 |
| **Self-review** | Same session reviews its own code, or "think harder" | Is the reviewer independent? | 4.6, 3.6 |
| **Lossy summary** | Summarize numbers or drop sources | What must survive verbatim? | 5.1, 5.6 |
| **Aggregate comfort** | "97% overall, so automate" | How does each segment do? | 5.5 |

::: mistake
==**Don't treat this table as a shortcut for answering without reading.**== Each family has exceptions. A prompt *is* the right answer when the guide wants guidance and judgment rather than a guarantee, such as how to phrase a reply. And a hard item may offer two code-level options.
:::

### When both answers still work

One candidate reports that when two options both work, the exam tends to favor the one that lets Claude handle more, and that some items test when Claude isn't the right tool, so read the constraints first. *That's one person's impression, not a documented rule.* It's consistent with the guide's preference for model-driven decisions in open-ended work, and with deterministic code where a guarantee is required, so use it only as a tie-breaker:

- **Judgment work** (which tool next, how to investigate): let the model decide.
- **Guarantees** (limits, order of operations, permissions): code decides.

## Multiple-response items

The item states how many to select. Evaluate each option on its own, as true or false for this scenario, instead of looking for a matching pair. The guide doesn't describe partial credit, so assume you need all of them, and don't count on any.

## Pacing

These checkpoints are my recommendation, based on the guide's 120 minutes and 60 items.

| After | Target time used | If behind |
| --- | --- | --- |
| Scenario 1 (about 15 items) | 28 min | Flag sooner |
| Scenario 2 (about 30 items) | 56 min | Stop rereading stems |
| Scenario 3 (about 45 items) | 84 min | Answer, flag and move on |
| Scenario 4 (60 items) | 110 min | Use the last 10 for flagged items |

The target gives you about 1.85 minutes per item and a 10-minute reserve. Candidates describe the pacing as tight and the wording as tiring, and they report using flag-and-return and the review screen. Always record an answer before flagging. *Check what your delivery allows in the exam tutorial,* because the guide doesn't describe the interface.

```mermaid caption="What to do when an item stalls. A guess you can come back to beats a blank, and a blank beats a lost scenario."
flowchart TB
  A[Item past 2.5 minutes?] -->|No| B[Answer and move on]
  A -->|Yes| C[Pick your best survivor]
  C --> D[Flag it, and move on]
  D --> E[Return in the final 10 minutes]
  style D fill:#FFF3C4,stroke:#8A6D00,stroke-width:2px
```

## The Build today trap

==**The guide was written in July 2026, and the products keep moving.**== For example, the guide says the **Task** tool spawns subagents and `allowedTools` must include `"Task"`, while current docs call it the **Agent** tool. On the exam, answer as the guide teaches. At work, follow the current docs. L5 lists every known difference, so you don't need to wonder mid-exam.

Candidates also report differences among the guide, the official practice exam and the real exam. If an item seems to allow either form, choose on the reasoning, not the name.

## How to use practice results

| Source | Use it for | Caution |
| --- | --- | --- |
| Guide's 12 samples | Reading the real style | Candidates call them the closest to the real thing |
| Official practice exam | Finding weak spots, with explanations | Per one candidate's account, it reuses a pool, so scores rise from memory. It shares no items with the real exam, which felt harder |
| This course's 500 questions | Learning by task statement | Original, not real items, and no score predicts your result |
| This course's 3 mocks | Timing and scenario stamina | Take each one only once under exam conditions |

**Targets (mine, not official):** 80% or better on each first attempt at a mock, and no domain under 70%. Since the mapping from raw score to scaled score isn't published, ==**no practice score can tell you the exam result.**==

A candidate's report that their weakest domain was Claude Code configuration, despite daily use, and another's that CLI flags were hardest, are worth noting. Daily users rarely think about scope, hierarchy or frontmatter. L2 has the flag sheet.

## The last week

1. Take Mock 3 early in the week, and fix what it shows.
2. Reread the final revision handbook, and the guide's Domain sections.
3. Review your error log, not whole lessons.
4. The day before: no new material. Check your ID, room and system requirements. Candidates describe strict proctoring.

## Trade-offs

Exam technique can't replace knowledge. The method only separates options when you know what each one does. It also has a cost: reading for constraints is a habit that needs practice, which is why the mocks exist.

::: tip
Keep an error log with four columns: item, what you picked, why it was wrong, and which distractor family it fell into. Repeated families show you what to study next much better than a percentage does.
:::

## Certification lens

::: key
Supports **every task statement**, and it teaches no new exam content. What's documented comes from the exam guide. The distractor families and pacing are this course's recommendations, and the candidate reports are anecdotal. No real exam content was used.
:::

These are original practice questions, not real or leaked.

::: qa
<div class="q">Q1 · Technique · Medium. An item says "the rule must hold on every refund". The options are a stronger system prompt, more few-shot examples, a hook that blocks the call, and a lower temperature. Which word settles it, and which answer follows?</div>
<div class="a"><b>Answer:</b> "Every" makes it a guarantee, so the hook. The other options all stay probabilistic.</div>
:::

::: qa
<div class="q">Q2 · Technique · Medium. You've spent three minutes on an item and you're down to two options. What should you do?</div>
<div class="a"><b>Answer:</b> Pick the better one, flag it and move on. A recorded answer is safer than an unanswered item, and the time protects later scenarios.</div>
:::

::: qa
<div class="q">Q3 · Technique · Hard. You score 92% on the official practice exam on your third attempt. What does that show?</div>
<div class="a"><b>Answer:</b> Little. One candidate reports that it reuses a pool, so a third attempt partly measures memory, and no raw score maps to the scaled score. Fresh items under timed conditions, by domain, tell you more.</div>
:::

::: revision
- 60 items, 120 minutes, 4 of 6 scenarios, scaled pass mark 720.
- Study all six scenarios and all five domains.
- Read the question first, find the constraint, eliminate, then pick the root-cause fix.
- Guarantees go in code. Judgment stays with the model.
- Prefer the simplest effective fix when the item says "first" or "minimal".
- Know the distractor families, and keep an error log.
- Answer and flag at about 2.5 minutes, with a 10-minute reserve.
- Answer as the guide teaches, and build as the current docs teach.
- Practice scores don't predict the exam result.
:::

::: notespace short Your notes
:::

</div>
