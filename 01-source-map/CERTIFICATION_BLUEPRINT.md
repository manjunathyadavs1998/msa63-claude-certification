# Phase 1: Certification Blueprint

**Claude Certified Architect – Foundations (exam code CCAR-F)**

Source: [official Exam Guide v1.0, effective July 2026](https://everpath-course-content.s3-accelerate.amazonaws.com/instructor%2F6nizmqk8tpzpfjvt6qmmav7rh%2Fpublic%2F1783542750%2FClaude+Certified+Architect+%E2%80%93+Foundations+Exam+Guide.pdf), read in full on 4 November 2026 from the [certification page](https://anthropic-partners.skilljar.com/claude-certified-architect-foundations-certification).

---

## 1. Exam facts

| Item | Official value |
|---|---|
| Questions | 60 |
| Format | Multiple-choice and multiple-response. Each item says how many answers to select |
| Structure | 4 scenarios, picked at random from a bank of 6 |
| Time | 120 minutes (2 minutes per question) |
| Pass mark | Scaled 720 on a 100–1,000 scale |
| Fee | $125 USD per attempt |
| Delivery | Proctored by Pearson VUE, online or at a test center |
| Validity | 12 months. On-time renewal is a free, non-proctored assessment |
| Retakes | Wait 14, then 30, then 90 days. Maximum 4 attempts in a rolling 12 months |
| Score report | Pass/fail, scaled score, and percent correct by domain |
| Suggested experience | 6+ months building with Claude APIs, Agent SDK, Claude Code and MCP |

**A scaled 720 does not mean 72% correct.** The guide does not publish a raw-score cut. Treat any "72%" claim as unverified. This program uses 80%+ on mocks as its own safety margin, which is a study target and not an official number.

### Check this first: eligibility

The official page links to a "Who can take an exam" page that I could not read. The guide refers to a "Partner Academy" and "partner tier" discounts. Videos 01 and 23 of the playlist and several third-party sites say registration is limited to [Claude Partner Network](https://vanja.io/claude-certification/) organizations, using a recognized company email domain.

**Action for you:** confirm that your employer is a partner before you plan around an exam date. If it isn't, the knowledge still applies, but you may not be able to sit the exam yet.

---

## 2. Domains and weights

| # | Domain | Weight | Items in a 60-question mock* |
|---|---|---|---|
| 1 | Agentic Architecture & Orchestration | 27% | 16 |
| 2 | Tool Design & MCP Integration | 18% | 11 |
| 3 | Claude Code Configuration & Workflows | 20% | 12 |
| 4 | Prompt Engineering & Structured Output | 20% | 12 |
| 5 | Context Management & Reliability | 15% | 9 |

*The item counts are my rounding of the official percentages. Because scenarios are drawn at random, your real distribution may differ slightly.

## 3. The six scenarios

You will see 4 of these, so prepare for all 6.

| # | Scenario | Primary domains |
|---|---|---|
| S1 | Customer Support Resolution Agent (`get_customer`, `lookup_order`, `process_refund`, `escalate_to_human`; 80%+ first-contact resolution) | D1, D2, D5 |
| S2 | Code Generation with Claude Code | D3, D5 |
| S3 | Multi-Agent Research System (coordinator with search, analysis, synthesis and report subagents) | D1, D2, D5 |
| S4 | Developer Productivity with the Agent SDK (built-in tools plus MCP) | D2, D3, D1 |
| S5 | Claude Code for CI/CD (reviews, test generation, PR feedback) | D3, D4 |
| S6 | Structured Data Extraction (JSON schema validation, edge cases) | D4, D5 |

---

## 4. All 30 task statements: what the exam wants

The wording below is my summary. The guide lists the full knowledge and skills under each statement.

### Domain 1: Agentic Architecture & Orchestration (27%)

| TS | Topic | The rule to remember | Common wrong answer |
|---|---|---|---|
| 1.1 | Agentic loops | Continue on `stop_reason: "tool_use"`, finish on `"end_turn"`, and append tool results to the history each turn | Parsing the text for "done", or using an iteration cap as the *primary* stop |
| 1.2 | Coordinator and subagents | Hub-and-spoke: all communication goes through the coordinator, which selects subagents dynamically | Always running the full pipeline; decomposing the task too narrowly |
| 1.3 | Spawning and context passing | Subagents inherit nothing, so put the complete findings in the prompt. The coordinator's `allowedTools` must include the spawning tool. Parallel means several calls in one response | Assuming shared memory; spawning across separate turns to get parallelism |
| 1.4 | Enforcement and handoff | When compliance must be guaranteed, use programmatic gates. Handoffs carry customer ID, root cause, amount and recommended action | "Make the system prompt stricter" |
| 1.5 | Hooks | `PostToolUse` normalizes data before the model sees it. Tool-call interception blocks policy violations | Relying on prompts for a guaranteed rule |
| 1.6 | Task decomposition | Prompt chaining for predictable work; dynamic decomposition for open-ended investigation | One giant pass; a fixed plan for an unknown problem |
| 1.7 | Sessions | `--resume <name>` when context is still valid; `fork_session` for branches; a fresh session with a summary when tool results are stale | Resuming with stale results and not mentioning file changes |

### Domain 2: Tool Design & MCP Integration (18%)

| TS | Topic | The rule to remember | Common wrong answer |
|---|---|---|---|
| 2.1 | Tool interfaces | Descriptions are the main selection signal. Include inputs, examples, edge cases and boundaries. Rename or split overlapping tools | Adding a routing layer or few-shot examples *before* fixing descriptions |
| 2.2 | Structured MCP errors | `isError` plus `errorCategory`, `isRetryable` and a readable message. An empty result is not a failure | Generic "Operation failed"; retrying a business error |
| 2.3 | Tool distribution and `tool_choice` | Give each agent 4–5 role-specific tools. `auto` may return text, `any` must call a tool, forced calls a named tool | Giving 18 tools to every agent; granting all search tools to synthesis |
| 2.4 | MCP integration | `.mcp.json` for the team, `~/.claude.json` for personal, `${VAR}` expansion for secrets. Resources expose catalogs. Prefer community servers for standard integrations | Committing tokens; building a custom Jira server |
| 2.5 | Built-in tools | Grep searches contents, Glob matches paths, Edit needs unique text, and Read + Write is the fallback. Explore incrementally | Reading every file upfront; using Glob to search contents |

### Domain 3: Claude Code Configuration & Workflows (20%)

| TS | Topic | The rule to remember | Common wrong answer |
|---|---|---|---|
| 3.1 | CLAUDE.md | User, project and directory levels. User level is not shared. Use `@import` and `.claude/rules/` to stay modular, and `/memory` to diagnose | Putting team rules in `~/.claude/CLAUDE.md` |
| 3.2 | Commands and skills | `.claude/commands/` and `.claude/skills/` are shared; `~/.claude/...` is personal. Know `context: fork`, `allowed-tools`, `argument-hint` | Inventing a `.claude/config.json` commands array |
| 3.3 | Path-specific rules | `paths:` globs load rules only for matching files. Best for conventions spread across directories, such as tests | A CLAUDE.md in every subdirectory |
| 3.4 | Plan vs direct | Plan for architecture, many files or several valid approaches. Direct for clear, small fixes. Explore isolates noisy discovery | Starting direct on a stated large migration |
| 3.5 | Iterative refinement | 2–3 input/output examples, tests first, the interview pattern. One message for interacting issues; one at a time for independent issues | "Make it better" |
| 3.6 | CI/CD | `-p` for non-interactive runs, `--output-format json` with `--json-schema`, an independent review session, and prior findings passed in to avoid duplicates | `CLAUDE_HEADLESS`, `--batch` (neither exists); self-review in the same session |

### Domain 4: Prompt Engineering & Structured Output (20%)

| TS | Topic | The rule to remember | Common wrong answer |
|---|---|---|---|
| 4.1 | Explicit criteria | Categorical report and skip criteria, with examples per severity. Temporarily disable noisy categories to restore trust | "Be conservative", "only high confidence" |
| 4.2 | Few-shot | 2–4 targeted examples showing the *reasoning* on ambiguous cases | Many examples that only show the format, or that cause pattern matching |
| 4.3 | Structured output | Tool use with a schema removes *syntax* errors, not *semantic* ones. Use nullable fields to prevent fabrication, and `"other"` + detail or `"unclear"` enums | Marking everything required; assuming a valid schema means correct data |
| 4.4 | Validation and retry | Send back the document, the failed output and the specific error. Retrying can't recover information that isn't in the source | Retrying missing data; a generic "try again" |
| 4.5 | Batch processing | 50% cheaper, up to 24 hours, no latency SLA, no multi-turn tools. Correlate with `custom_id`, and resubmit only failures | Using batch for blocking pre-merge checks |
| 4.6 | Multi-pass review | An independent instance beats self-review. Per-file passes plus a cross-file integration pass | A bigger context window; requiring consensus across runs |

### Domain 5: Context Management & Reliability (15%)

| TS | Topic | The rule to remember | Common wrong answer |
|---|---|---|---|
| 5.1 | Long conversations | Keep a persistent "case facts" block outside summarized history. Trim tool outputs. Put key findings first to counter "lost in the middle" | Summarizing away amounts, dates and IDs |
| 5.2 | Escalation | Triggers: the customer asks for a human, a policy gap, or no progress. Multiple matches mean ask for another identifier | Sentiment analysis, self-reported confidence, heuristic matching |
| 5.3 | Error propagation | Recover locally from transient errors. Propagate type, query, partial results and alternatives. An access failure is not an empty result | Returning empty as success; killing the workflow on one failure |
| 5.4 | Large codebases | Scratchpad files, subagents for verbose work, `/compact`, and manifests for crash recovery | One ever-growing session |
| 5.5 | Human review | Aggregate accuracy can hide weak segments. Measure by document type and field; use stratified sampling and calibrated field-level confidence | "97% overall, so automate" |
| 5.6 | Provenance | Structured claim-source mappings, dates on data, conflicts annotated rather than resolved at random, and coverage gaps reported | Dropping sources in summaries; silently choosing one number |

**Three decision habits appear throughout the guide's sample questions:**

1. **Fix the root cause with the proportionate first step.** Answers that add a classifier, routing layer or new infrastructure before fixing a description or prompt are often traps.
2. **Guaranteed rule means code. Judgment means prompt.** Anything financial or compliance-related needs hooks or gates.
3. **Keep the error context.** Hiding, flattening or summarizing away failure details is nearly always wrong.

---

## 5. Out of scope for the exam

The guide explicitly says these **will not appear**:

- Fine-tuning or training custom models
- API authentication, billing and account management, OAuth, API key rotation
- Detailed language or framework implementation
- Deploying or hosting MCP servers (infrastructure, networking, containers)
- Claude's internal architecture, training process and model weights
- Constitutional AI, RLHF and safety training methods
- Embedding models and vector database implementation details
- Computer use, vision and image analysis
- Streaming API implementation and server-sent events
- Rate limits, quotas and pricing calculations
- Specific cloud provider configurations
- Performance benchmarks and model comparisons
- Prompt caching details (beyond knowing it exists)
- Token counting algorithms and tokenization specifics

---

## 6. Your 15 modules mapped to the exam

Your Idea.md goes well beyond this exam, which suits your goal of becoming a production architect. It does mean a lot of it won't earn exam marks, so I've labeled each module.

| Module | Exam relevance | Task statements | What is excluded or must be added |
|---|---|---|---|
| 1 AI & LLM Fundamentals | **Mostly out of scope** | Supports 1.1, 4.3, 5.1 | Fine-tuning, RLHF, tokenization and internals are explicitly excluded |
| 2 Claude Architecture | Partial | 4.5, 1.1 | Vision, streaming, caching details, model comparisons are excluded |
| 3 Prompt Engineering | **Core** | 4.1–4.4 | Add multi-pass review (4.6) and batch strategy (4.5) |
| 4 Claude API with Java | Partial | 1.1, 2.3, 4.3 | Auth, rate limits, streaming and Java specifics are excluded |
| 5 Tool Use | **Core** | 2.1, 2.2, 2.3, 2.5 | Add built-in tools (2.5) |
| 6 MCP | **Core** | 2.2, 2.4 | Deployment is excluded; `.mcp.json` scoping is in |
| 7 RAG | **Mostly out of scope** | 5.6, part of 5.1 | Embedding and vector DB details are excluded; provenance is in |
| 8 Agentic AI | **Core, highest weight** | 1.1–1.7 | Add the Agent SDK, hooks, sessions |
| 9 Context & Reliability | **Core** | 5.1–5.6 | Add human review and provenance |
| 10 Claude Code | **Core** | 3.1–3.6 | Add path rules and CI flags |
| 11 Security & Governance | Indirect | 1.4, 1.5, 2.3 | No dedicated domain; least privilege and hooks are the tested parts |
| 12 Evaluation | Partial | 5.5, 4.4 | Stratified sampling and calibration are in; tracing tools are not |
| 13 Enterprise Architecture | **Out of scope** | None | Cloud, K8s and deployment are excluded. Career value only |
| 14 Advanced Patterns | Partial | 1.2, 1.6 | AI gateways and distributed transactions are not on the exam |
| 15 Projects | Practice | All | Add the guide's 4 official exercises (section 8 of the guide) |

### Java and the exam

The exam isn't language-specific, but you do need to read non-Java material. The appendix names **Pydantic**, and the Claude Agent SDK is published for [Python and TypeScript](https://code.claude.com/docs/en/agent-sdk). So:

- **Java:** Messages API, manual agentic loops, tool definitions, MCP servers and production services.
- **Python or TypeScript (reading level):** Agent SDK examples, hooks and subagents, because the official exercises assume them.

---

## 7. Where the guide and current products differ

The guide is dated July 2026 and today is November 2026. Exams usually follow their published guide, so **answer the exam from the guide and build real systems from current docs**. If Anthropic publishes a newer guide, it replaces this table.

| Topic | Exam guide says | Current behavior | Source |
|---|---|---|---|
| Spawning subagents | The **Task** tool | Renamed **Agent** in Claude Code 2.1.63. `Task(...)` references still work as aliases | [Subagents docs](https://code.claude.com/docs/en/sub-agents) |
| Commands vs skills | Separate concepts | Commands are merged into skills. `.claude/commands/x.md` and `.claude/skills/x/SKILL.md` both create `/x` | [Skills docs](https://code.claude.com/docs/en/skills) |
| `allowed-tools` | Restricts tool access during a skill | Docs describe it as tools Claude can use *without asking permission* during that turn | [Skills docs](https://code.claude.com/docs/en/skills) |
| Forced `tool_choice` | `any` and `{"type":"tool"}` guarantee a tool call | Returns **400** on Opus 5.5, Sonnet 5.5, Fable 5.1 and Mythos 5.1. Use `auto` with an instruction and `strict: true`, or `output_config.format` | Claude API reference bundled with Claude Code |
| Structured output | Tool use is the most reliable approach | A dedicated option now exists too: `output_config.format` and `client.messages.parse()` | Same |
| Tool errors | `isError` with `errorCategory` and `isRetryable` | Only `isError` is in the MCP protocol. The other two are metadata you design | [MCP tools spec](https://modelcontextprotocol.io/specification/2026-07-28/server/tools) |
| MCP protocol | Not detailed | Version `2026-07-28` is stateless, with `server/discover`. Sampling and logging are deprecated | [MCP architecture](https://modelcontextprotocol.io/docs/learn/architecture) |
| `stop_reason` | `tool_use`, `end_turn` | Production code must also handle `max_tokens`, `pause_turn` and `refusal` | Same |
| Few-shot / temperature | Video 16 mentions lowering temperature | `temperature` is rejected on current models (non-default values on Sonnet 5.5 and Haiku 5.5). Use `effort` instead | Same |

**The guide's wording on `allowed-tools` and `tool_choice` will likely still be the exam's wording.** Knowing both versions also protects you in real projects and interviews.

---

## 8. Official preparation resources

- [Certification page, with the guide, terms and policy PDFs](https://anthropic-partners.skilljar.com/claude-certified-architect-foundations-certification)
- [Claude API docs](https://platform.claude.com/docs)
- [Claude Code docs](https://code.claude.com/docs)
- [Agent SDK docs](https://code.claude.com/docs/en/agent-sdk)
- [Model Context Protocol](https://modelcontextprotocol.io)
- The guide's 12 sample questions (section 9) and 4 hands-on exercises (section 8). **Do these yourself. They are the best available signal of exam style.**

Third-party figures differ on some details, including one listing a $99 fee. Where they differ, this file follows the official guide.
