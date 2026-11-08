# Phase 2: Playlist Analysis

[YouTube playlist](https://www.youtube.com/watch?v=rcpNFm_poQs&list=PLviC8AFqAj5A9MHkRIn2fU5Ac2lEdJxNf): 23 videos, about 14.5 hours, about 137,000 transcript words. This is a supplement to the official guide, not a replacement.

## How this was analysed, and its limits

To keep token use low, I did **not** read all 137,000 words.

| Step | What was done |
|---|---|
| Video list | Retrieved all 23 titles and durations |
| Transcripts | Downloaded all 23. All are **auto-generated English captions** |
| Topic mapping | A script counted terms for each of the 30 task statements in every transcript |
| Exact checks | A second script searched for 32 exact exam terms, such as `custom_id` and `stratified`, per video |
| Sampling | I read 3 short excerpts per video (about 135 words each), plus targeted context for a few claims |

**What this can and can't tell you:**

- It reliably shows *which topics each video discusses* and *which exam terms are absent*.
- It does **not** assess teaching quality in depth. "Depth" below reflects how much a video talks about a topic, not whether every statement is accurate.
- Slides, diagrams, on-screen code and demos are **not** in transcripts. A term marked as missing may still appear on a slide.
- Captions garble words (Claude becomes "cloud", "plain mode" for plan mode), so counts are approximate.

A full read of every transcript would cost about 180,000 tokens. If you want it, I'd do it only for the videos you find confusing.

---

## Video-by-video map

Task statements are the strongest matches from the term counts. "Ep" is the creator's numbering, which is one behind the playlist position.

| # | Video | Min | Maps to | Depth | Module | Watch? |
|---|---|---|---|---|---|---|
| 01 | Full exam breakdown | 9 | Overview, partner-only rule, exam tips | Overview | Blueprint | Optional |
| 02 | Ep 01 Agentic loops & `stop_reason` | 49 | **1.1** | High | 8 | **Must** |
| 03 | Ep 02 Multi-agent & coordinator | 33 | **1.2, 1.3** | High | 8 | **Must** |
| 04 | Ep 03 Context passing & sessions | 34 | **1.7**, 1.2, 1.3, 5.6 | High | 8, 9 | **Must** |
| 05 | Ep 04 Multi-agent hands-on (Python) | 13 | 1.1–1.3 | Demo | 8 | Must |
| 06 | Ep 05 Hooks & task decomposition | 45 | **1.5, 1.4, 1.6** | High | 8 | **Must** |
| 07 | Ep 06 Tool descriptions & misrouting | 25 | **2.1**, 4.2 | Medium | 5 | **Must** |
| 08 | Ep 07 Error handling & `tool_choice` | 38 | **2.2, 2.3**, 1.2 | High | 5, 6 | **Must** |
| 09 | Ep 08 MCP servers & config | 89 | **2.4**, 2.5, 2.1 | Very high, partly beyond exam | 6 | Selective |
| 10 | Ep 09 Built-in tools | 36 | **2.5** | High | 5, 10 | Must |
| 11 | Ep 10 CLAUDE.md hierarchy | 32 | **3.1, 3.3** | High | 10 | **Must** |
| 12 | Ep 11 Slash commands & skills | 33 | **3.2** | High | 10 | **Must** |
| 13 | Ep 12 Plan mode vs execute | 52 | **3.4, 3.5** | High | 10 | **Must** |
| 14 | Ep 13 CI/CD pipelines | 31 | **3.6** | High | 10 | **Must** |
| 15 | Ep 14 Explicit criteria | 34 | **4.1**, 3.6, 4.2 | High | 3 | **Must** |
| 16 | Ep 15 Few-shot prompting | 28 | **4.2** | High | 3 | Must |
| 17 | Ep 16 Structured output & JSON schema | 37 | **4.3, 4.4**, 2.3 | High | 3 | **Must** |
| 18 | Ep 17 Batch API & multi-pass review | 34 | **4.5, 4.6**, 3.6 | High | 2, 3 | **Must** |
| 19 | Ep 18 Context engineering | 43 | **5.1, 5.2** | High | 9 | **Must** |
| 20 | Ep 19 Error propagation & large codebases | 42 | **5.3, 5.4**, 2.2 | High | 9 | **Must** |
| 21 | Ep 20 When AI needs a human | 51 | **5.5, 5.6** | High | 9, 12 | **Must** |
| 22 | Bonus: exam questions & traps | 61 | Mixed, all domains | Practice | Exam prep | **Must, near the end** |
| 23 | Architect vs Developer exams | 25 | Career choice, partner gating | Overview | Blueprint | Optional |

**Video 01 and 23 note:** both state the exam is gated to partners. See the eligibility check in the blueprint.

---

## Coverage by task statement

All 30 task statements have at least some coverage. Those below deserve more attention:

### Weak or missing in the transcripts: study these from the guide

| TS | Gap | Evidence | What to do |
|---|---|---|---|
| 4.4 | **`detected_pattern`** field | No transcript mentions it | Read the guide's text on 4.4 |
| 1.6, 4.6 | **"Attention dilution"** as a term | No mention, though multi-pass review is taught in video 18 | Learn the term; sample Q12 uses the idea |
| 5.5 | Field-level calibration and stratified sampling | Only video 21 | Rewatch video 21; use guide exercise 3 |
| 3.3 | Path-specific rules | Essentially only video 11 | Do guide exercise 2 by hand |
| 3.5 | Interview pattern, test-driven iteration | Only video 13 | Short but explicitly tested |
| 3.2 | `argument-hint` | 3 mentions, only video 12 | Create one real skill |
| 5.1 | "Lost in the middle" | 2 mentions, only video 19 | Memorize the mitigation |
| 2.4 | MCP resources as catalogs | 6 mentions, only video 9 | Compare resources with tools |
| 1.3, Appendix | **Agent SDK** by name | Only 6 explicit mentions, mostly videos 05 and 23 | Read the Agent SDK docs yourself |
| Appendix | Pydantic | 3 passing mentions | Read a short validation-retry example |

### Covered well

The largest term counts are for 1.1, 1.2, 1.3, 1.5, 2.5, 3.1, 3.2, 3.4, 3.6, 4.5 and 5.2. Each has at least one dedicated video.

---

## Outside the playlist: topics from your Idea.md

| Topic | Evidence in transcripts | Where it goes |
|---|---|---|
| Java / Spring Boot | 6 passing mentions across the playlist; demos appear to be Python or TypeScript | Modules 4, 5, 6 and 15 must supply all Java |
| RAG, embeddings, vector DBs | **0 mentions** | Module 7 |
| Prompt caching | **0 mentions** | Module 2 |
| Streaming | 2 mentions | Modules 2, 4 |
| Evaluation and observability | 1 mention | Module 12 |
| Prompt injection, PII, RBAC | 4 mentions | Module 11 |
| Kafka, K8s, AWS, Docker | 14 passing mentions | Module 13 |
| MCP transports | 12 mentions, video 09 | Module 6 |

Most of these gaps are not a flaw in the playlist, because the exam excludes many of them. They do confirm the playlist won't cover your wider production goal.

---

## Points to double-check while watching

| Video | Statement | What to know |
|---|---|---|
| 03 | "99.99%" of subagent problems come from the coordinator not injecting context | It's a useful instinct, but read each question's evidence. Guide sample Q7 is about decomposition instead |
| 08, 17 | `tool_choice: "any"` for guaranteed output | Correct for the exam per the guide, but current top models reject it with a 400 (see blueprint section 7) |
| 16 | Lowering temperature | The caption says it doesn't help judgment, which is reasonable. Current models also reject the parameter |
| 03, 05 | Task tool payload | Correct for the guide. The tool is now named Agent in Claude Code |
| 12 | `allowed-tools` | The exam treats it as a restriction. Current docs define it as a pre-approval |

These are flagged from short excerpts and exact-term matches, so they are prompts to check rather than proof of errors.

## Fastest watch order

Skip videos 01 and 23 and be selective in video 09. That leaves about 13 hours. Following exam weight:

**D1:** 02, 03, 04, 05, 06 → **D3:** 11, 12, 13, 14 → **D4:** 15, 16, 17, 18 → **D2:** 07, 08, 10, 09 → **D5:** 19, 20, 21 → **Final:** 22
