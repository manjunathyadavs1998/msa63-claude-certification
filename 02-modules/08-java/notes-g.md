<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Part IV, Java</div>

# AI in Java, Lesson 7

## Testing AI Code

<div class="sub">The team said AI code couldn't be tested, because the outputs vary. So they had no tests. A refactor dropped the refund limit check, and nobody noticed for a week. That check had nothing probabilistic about it.</div>

<table class="meta">
<tr><td>Module</td><td>AI08 of 13</td></tr>
<tr><td>Lesson</td><td>7 of 8</td></tr>
<tr><td>Prerequisites</td><td>AI05 L6, AI08 L1–L6</td></tr>
<tr><td>Exam relevance</td><td>Mostly out of scope. Stratified measurement supports TS 5.5</td></tr>
<tr><td>Reading time</td><td>~25 minutes</td></tr>
</table>
</div>

<div class="toc">

## Contents

1. [Testing AI Code](#testing-ai-code)

</div>

<div class="lesson">
<div class="lesson-meta">Lesson 7 of 8 · AI in Java</div>

# Testing AI Code

::: keyidea
==**Most code in an AI application is ordinary code, and it should have ordinary tests that run with no key.**== Separate two questions. *Does our code behave correctly, given any model reply?* That's testing, and it should be deterministic. *Does the model give good replies?* That's evaluation, and it's statistical. ==**Mixing them gives you flaky tests that prove little.**==
:::

<div class="prereq"><b>Before:</b> this module's code, and evaluating RAG (AI05 L6). <b>Teaches:</b> layers of tests, what each one can and can't catch, prompt snapshots, and eval gates. <b>Used by:</b> AI10 L3.</div>

## The hook: what 26 tests can do with no key

`code/ai08-java` has 26 tests. Real output: `Tests run: 26, Failures: 0, Errors: 0`, in about 43 seconds, with no API key and no external services. Each one exists to catch a specific failure:

| Test class | Tests | Uses | Catches |
| --- | ---: | --- | --- |
| `AgentServiceTest` | 6 | Script, real H2 | Skipped gates, repeated refunds, leaks, runaway loops |
| `ClaudeModelTest` | 5 | Real SDK, fake API | Forbidden fields, wrong retries, lost blocks |
| `ChatApiTest` | 5 | Real Tomcat | SSE, validation, error bodies, disconnects |
| `AiTestingPatternsTest` | 4 | Script | PII, identity, prompt changes, eval gates |
| `RagServiceTest` | 3 | Script, in-memory search | Permissions, fail closed, prompt shape |
| `McpServerTest` | 1 | Real SDK client and server | Protocol, errors |
| `ReviewWorkerTest` | 1 | Real embedded Kafka | Duplicates, retries, dead letters |
| `BatchJobTest` | 1 | Real SDK, fake API | Ordering, resubmission |

**What none of them show:** whether Claude gives good answers, or whether the real API matches my fake.

## Intuition: testing a car and its driver

You can test a car's brakes on a rig, with no driver. They either work or they don't, every time. Whether a driver makes good decisions is a different question, answered by many supervised miles, in statistics. You wouldn't accept "brakes work 94% of the time", and you wouldn't test a driver with one fixed route.

## Mechanism: the layers

```mermaid caption="Layers of testing, from fast and deterministic at the top to slow and statistical at the bottom. Run the top ones on every commit, and the bottom ones less often."
flowchart TB
  U[Scripted model: logic, on every commit] --> F[Fake API: real SDK and JSON, on every commit]
  F --> I[Real infrastructure: Kafka, HTTP, in CI]
  I --> L[Live smoke test: real API, nightly]
  L --> E[Evals: real model, on prompt and model changes]
  style U fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
  style E fill:#FFF3C4,stroke:#8A6D00,stroke-width:2px
```

| Layer | Answers | Key? | Deterministic? | Cost |
| --- | --- | --- | --- | --- |
| Scripted model | Does our logic handle this reply? | No | Yes | Free |
| Fake API | Does the SDK send the right JSON? | No | Yes | Free |
| Real infrastructure | Do Kafka and HTTP behave? | No | Mostly | Free, slower |
| Live smoke | Does the real API accept it? | Yes | Mostly | Cents |
| Evals | Are the answers good? | Yes | **No** | Dollars |

### Layer 1: a script for logic

A scripted model lets you produce replies that are hard to get on demand: a truncation, a refusal, a tool call in the wrong order, or a hallucinated tool.

```java
ScriptedModel model = ScriptedModel.of(
        ScriptedModel.tools(refund("7781", "40.00")),
        ScriptedModel.tools(verify()),
```

**Assert on effects, not on words.** `refundsIssued` and the ledger are facts. Don't assert on generated text here, since that text is yours.

### Layer 2: a fake API for the contract

From `ClaudeModelTest`, which pins down what must never be sent:

```java
assertThat(body.has("temperature")).isFalse();
assertThat(body.has("tool_choice")).isFalse();
assertThat(body.has("thinking")).isFalse();
```

### Secrets and security are testable too

| Risk | Test | Real output |
| --- | --- | --- |
| Tool exception reaches the model | Throw a marker, and search the next request | `The tool failed.` |
| Key in an API error | Assert it's not in the body | Passes |
| PII reaches the model | Assert the request lacks it | See below |

```text
sent to model: Dispute on account <ACCOUNT_1> from <EMAIL_2>, and again <ACCOUNT_1>.
model replied: Open a dispute for <ACCOUNT_1>, and email <EMAIL_2>.
user sees: Open a dispute for 123456789012, and email asha@example.com.
```

### Prompt snapshots

A prompt is code. A one-word change can move behavior, and a refactor can change it by accident. So keep prompts in files, and make any change a visible, reviewed diff that triggers evals. Real output, in a small demonstration:

```text
snapshot unchanged: true
snapshot after an edit: false
```

### Evals: gates by segment

The harness scores by segment, and blocks on any failing gate. **In this test, the "model" is a script, with a deliberately bad injection answer,** so it tests the harness only. Real output:

```text
lookup     2/2 gate 0.90 pass
refund     2/2 gate 0.90 pass
injection  0/1 gate 1.00 FAIL
no-answer  1/1 gate 0.90 pass
overall 5/6 = 83%, blocked by: [injection]
```

```mermaid caption="How a change gets through. Logic tests always run. Changes to prompts, tools or models also run evals, and any failed segment gate blocks the release, whatever the overall score."
flowchart TB
  C[Change] --> T[Tests with no key]
  T --> P{Prompt changed?}
  P -->|no| S[Ship]
  P -->|yes| E[Evals by segment]
  E --> B[Any failed gate blocks]
  style B fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
```

**83% overall, and still blocked,** because safety segments carry a zero-tolerance gate (TS 5.5). With so few cases, these rates are only an illustration: see AI05 L6 for sample sizes.

| Eval practice | Why |
| --- | --- |
| Don't compare exact text | Replies vary |
| Check properties: tool chosen, facts present, forbidden content absent | Stable |
| Use a judge for fuzzy qualities, and calibrate it (AI05 L6) | Scale |
| Same cases before and after | Reduces noise |
| Track cost and latency too | A cheaper reply that needs more turns isn't cheaper |
| Pin the model version | Otherwise nothing's comparable |

### A live smoke test

A few cheap real calls, nightly, on your exact model, with your exact parameters. **It catches what fakes can't:** a retired model, a rejected parameter, or a changed response. Allow for variation by checking shape rather than wording. This module doesn't have one, because there's no key here.

## Why the obvious fixes fall short

| Tempting move | Why it fails | Better |
| --- | --- | --- |
| "AI code can't be tested" | Most of it is ordinary logic | Script the model |
| Call the real API in unit tests | Slow, costly, flaky | Layers |
| Assert on exact model text | Breaks with any variation | Assert on properties |
| Mock the SDK client | Skips serialization | Fake HTTP server |
| Retry flaky tests until green | Hides real regressions | Separate evals from tests |
| One overall accuracy number | Hides failing segments | Gate by segment |
| Only test the happy path | Failures are where the money goes | Script failures on purpose |

## How Anthropic and the field approach it

| Point | Source |
| --- | --- |
| Evals with about 20 real queries can show big effects early | Anthropic's research post (AI06 L6) |
| Use held-out test sets for tools | Writing Tools for Agents |
| Judge outcomes rather than the path | Anthropic's research post |
| Stratified measurement | CCAR-F TS 5.5 |

## The production architecture

==**This is a recommended reference design, not a specific company's system.**== AI10 L3 goes further into evaluation.

**Baseline: manual checks.**

```mermaid caption="Baseline. People try a few prompts by hand, so logic regressions get through unnoticed."
flowchart TB
  C[Change] --> M[Try it by hand]
  M --> S[Ship]
  S --> X[Regressions found by users]
  style X fill:#FBD5E0,stroke:#B3261E,stroke-width:2px
```

**Improved: tests with a real key in CI.** New problem: ==**they're slow, costly and flaky, so people start ignoring red builds.**==

```mermaid caption="Improved. Real API tests catch more, but flakiness teaches the team to ignore failures."
flowchart TB
  C[Change] --> T[All tests call the real API]
  T --> X[New problem: flaky, slow, costly]
  style X fill:#FFF3C4,stroke:#8A6D00,stroke-width:2px
```

**Production: layered gates.**

```mermaid caption="Production. Deterministic tests guard every commit. Nightly smoke tests watch the real API, evals guard prompt changes, and production samples feed new cases."
flowchart TB
  C[Commit] --> T[Scripted and fake API tests]
  T --> E[Evals, when prompts or models change]
  E --> K[Canary]
  N[Nightly live smoke test] --> A[Alert]
  K --> P[(Production samples become cases)]
  style T fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
```

### Failures

| Failure | Containment |
| --- | --- |
| Fake drifts from the real API | Nightly smoke test, and record real responses occasionally |
| Eval set goes stale | Add production failures |
| Flaky eval blocks releases | Set thresholds using margins (AI05 L6) |
| Judge drifts | Recalibrate |
| Tests pass, and production fails on real data | Canary, and watch by segment |

## Trade-offs

| Decision | Gain | Cost |
| --- | --- | --- |
| Scripted model | Fast, deterministic, any scenario | Says nothing about the model |
| Fake API | Tests the real SDK | My JSON may be wrong |
| Recorded responses | More realistic | Go stale, and may hold sensitive data |
| Evals in CI | Catch quality regressions | Cost and time |
| Embedded Kafka | Real behavior | Slower, about 8 seconds here |

**When to go lighter:** a prototype needs a few scripted tests. Add evals once the prompt matters.

::: mistake
1. **Saying AI code can't be tested.** Most of it can.
2. **Mixing tests and evals.** One's deterministic, the other's statistical.
3. **Asserting on exact generated text.** Check properties.
4. **Mocking the SDK client.** You skip the JSON.
5. **Only testing success.** Script truncation, refusal, and bad order.
6. **No live test at all.** Fakes can drift.
7. **Accepting prompt changes with no eval.** Treat them as code changes.
8. **Reading a small eval as precise.** Remember the margins.
:::

::: tip What to say in an interview
*"I separate testing from evaluation. Most of the code is deterministic given a model reply, so I test it with a scripted model and with the real SDK against a fake API, with no key and no flakiness. Whether replies are good is statistical, so that's evals by segment, run on prompt or model changes, with zero tolerance on safety segments. A small nightly live test catches API changes."*
:::

## Certification lens

::: key How this lesson maps to CCAR-F
==**Not directly tested,**== apart from the idea in **TS 5.5**: aggregate accuracy can hide weak segments.
:::

These are original practice questions for understanding, not real or leaked exam questions.

::: qa
<div class="q">Q1 · Component selection · Medium. Which layer catches each? (a) A refund runs before verification. (b) The request includes <code>temperature</code>. (c) A new model ID is rejected. (d) Answers got vaguer after a prompt edit. (e) A duplicate event pays twice.</div>
<div class="a">(a) Scripted model. (b) Fake API. (c) Live smoke test. (d) Evals. (e) Real or embedded Kafka, or a scripted consumer test.</div>
:::

::: qa
<div class="q">Q2 · Code analysis · Medium. A test asserts <code>assertEquals("Your refund of $40 is on its way.", agent.run(...).text())</code> with a live model. What's wrong? Rewrite it.</div>
<div class="a">It's flaky, since the wording varies, and it's slow and costly. It also doesn't check the effect. Use a scripted model, and assert <code>refundsIssued</code> and the ledger. For quality, put it in evals, and check properties: the right tool was called, and the amount appears.</div>
:::

::: qa
<div class="q">Q3 · Best decision · Hard. Overall eval accuracy is 96%, and the release is blocked as the injection segment is 19 of 20. A manager says 95% is good enough. What do you say?</div>
<div class="a">That segment has a zero-tolerance gate for a reason: one failure may mean acting on an attacker's instructions. Read the failing case, and fix it, for example by moving that protection into code, so it no longer depends on the model. With 20 cases, 95% is also imprecise. Don't lower the gate to ship.</div>
:::

::: revision Revision — Lesson 7
- Most AI application code is ordinary, testable code.
- Testing: is our code right, given any reply? Evaluation: are the replies good?
- Scripted model for logic, with failures on purpose.
- Fake API with the real SDK for JSON, retries and errors.
- Assert effects, not exact words.
- Prompts are code, so snapshot them, and review changes.
- Evals by segment, with zero tolerance on safety segments.
- 83% overall can still be blocked.
- A nightly live smoke test catches what fakes can't.
- Production failures become new cases.
:::

::: notespace short Your notes
:::
</div>
