# Concept Dependency Map

Each concept has **one canonical home**. Teach it there, and cross-reference it elsewhere. Check here before writing a definition.

## Canonical homes

| Concept | Taught in | Used by |
| --- | --- | --- |
| Precision, recall, F1, overfitting | AI01 L2 | AI04, AI05 L6, AI10 L3, AI13 |
| Loss, gradient descent, backpropagation | AI01 L3 | AI02 L4, AI11 L1 |
| Word vectors | AI01 L4 | AI02 L2, AI04 L1 |
| Tokens and tokenization | AI02 L1 | AI03 L5, AI10 L5 |
| Attention, multi-head, positions | AI02 L2 | AI02 L3, AI02 L6 |
| RLHF, Constitutional AI | AI02 L4 | AI09 L5, AI11 L3 |
| Sampling, KV cache, continuous batching | AI02 L5 | AI10, AI11 L2 |
| Why output is probabilistic | AI02 L5 | AI06 L8, AI09, AI13 |
| Hallucination | AI02 L6 | AI03 L3, AI05, AI10 L3 |
| "Lost in the middle", attention dilution | AI02 L6 | AI03 L6, AI06, AI13 |
| `stop_reason`, statelessness | AI03 L1 | AI06 L2, AI08 L1, AI13 |
| Few-shot, explicit criteria | AI03 L2 | AI06 L3, AI12, AI13 |
| JSON schema output, nullable fields, retry with feedback | AI03 L3 | AI06, AI08, AI13 |
| Thinking, effort, model selection | AI03 L4 | AI10 L2 |
| Prompt caching, Message Batches | AI03 L5 | AI05 L3, AI08 L6, AI10 L5 |
| Case facts, trimming, compaction | AI03 L6 | AI06 L7, AI13 |
| Embedding model choice | AI04 L1 | AI05 |
| ANN recap (home: HLD M21A), filtering, quantization | AI04 L2 | AI04 L5, AI05 L7 |
| BM25 (home: HLD M09), RRF, reranking | AI04 L3 | AI05 L3, AI08 L5 |
| Chunking | AI05 L2 | AI05 L3, AI08 L5 |
| Citations, provenance, claim-source mappings | AI05 L4 | AI06 L6, AI06 L10, AI13 |
| Retrieval metrics | AI05 L6 | AI10 L3 |
| Workflows vs agents, prompt chaining | AI06 L1 | AI06 L6, AI07, AI13 |
| Agentic loop | AI06 L2 | AI08 L3, AI12, AI13 |
| Tool design, `tool_choice`, structured errors | AI06 L3 | AI06 L5, AI08 L3, AI13 |
| MCP primitives and transports | AI06 L4 | AI06 L5, AI08 L4, AI09 L3 |
| Coordinator-subagent, context passing | AI06 L6 | AI06 L10, AI12 L3, AI13 |
| Sessions, forks, scratchpads, manifests | AI06 L7 | AI12, AI13 |
| Hooks, gates, escalation, handoff | AI06 L8 | AI06 L9, AI09, AI12 L3, AI13 |
| Prompt injection | AI09 L2 | AI05, AI06, AI08, AI10 |
| Least privilege for tools | AI09 L3 | AI06 L3, AI13 |
| PII, tenancy, audit | AI09 L4 | AI08 L8, AI10 L6 |
| Retries, fallbacks, rate limits (home: HLD M11) | AI10 L1 | AI08 L1, AI10 L7 |
| Evals, stratified sampling, calibration | AI10 L3 | AI05 L6, AI13 |
| Traces and token metrics | AI10 L4 | AI08 |
| CLAUDE.md, rules, skills, plan mode | AI12 | AI13 |
| CI/CD flags, independent review | AI12 L5 | AI13 |

**Note on forward references:** AI05 L6 uses evals before AI10 L3 teaches them in full. AI05 L6 covers only retrieval metrics and points forward. AI06 mentions injection and points to AI09.

## Task statements to teaching homes

Every exam objective has a lesson that teaches it. AI13 reviews and tests them.

| Task statement | Taught in | | Task statement | Taught in |
| --- | --- | --- | --- | --- |
| 1.1 Agentic loops | AI06 L2 | | 3.4 Plan vs direct | AI12 L4 |
| 1.2 Coordinator pattern | AI06 L6 | | 3.5 Iterative refinement | AI12 L4 |
| 1.3 Context passing | AI06 L6 | | 3.6 CI/CD | AI12 L5 |
| 1.4 Enforcement, handoff | AI06 L8 | | 4.1 Explicit criteria | AI03 L2 |
| 1.5 Hooks | AI06 L8 | | 4.2 Few-shot | AI03 L2 |
| 1.6 Decomposition | AI06 L1 | | 4.3 Structured output | AI03 L3 |
| 1.7 Sessions | AI06 L7 | | 4.4 Validation, retry | AI03 L3 |
| 2.1 Tool interfaces | AI06 L3 | | 4.5 Batches | AI03 L5 |
| 2.2 Structured errors | AI06 L5 | | 4.6 Multi-pass review | AI12 L5 |
| 2.3 Tool distribution | AI06 L3 | | 5.1 Long conversations | AI03 L6 |
| 2.4 MCP integration | AI06 L5 | | 5.2 Escalation | AI06 L8 |
| 2.5 Built-in tools | AI12 L1 | | 5.3 Error propagation | AI06 L6 |
| 3.1 CLAUDE.md | AI12 L2 | | 5.4 Large codebases | AI06 L7 |
| 3.2 Commands, skills | AI12 L3 | | 5.5 Human review | AI10 L3 |
| 3.3 Path rules | AI12 L2 | | 5.6 Provenance | AI05 L4 |

**All 30 task statements have a home.** AI10 L3 and AI05 L4 hold exam content in otherwise non-core modules, so those lessons mustn't be skipped.

## AI13 additions

AI13 L2 teaches Agent SDK reading and CLI flags, and AI13 L3 teaches Pydantic and MCP resources in depth. Reference them from other lessons, and don't restate them.
