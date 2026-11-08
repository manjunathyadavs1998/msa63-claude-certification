# Source Register

Records what was really read. **"Link OK" means only that the URL responded on 4 November 2026, not that its content has been checked.** Read each source before citing it, then update this file.

## Exam and playlist

| Source | Status |
| --- | --- |
| [CCAR-F Exam Guide v1.0](../01-source-map/CCAR-F-Exam-Guide-v1.0.pdf) | **Read in full** |
| [Agent SDK subagents](https://code.claude.com/docs/en/agent-sdk/subagents.md), [hooks](https://code.claude.com/docs/en/agent-sdk/hooks.md), [sessions](https://code.claude.com/docs/en/agent-sdk/sessions.md) | **Read in full on 2026-11-06** for AI13 |
| [CLI reference](https://code.claude.com/docs/en/cli-reference.md) | Saved in full, and searched for listed flags, for AI13 L2 |
| [MCP resources](https://modelcontextprotocol.io/docs/concepts/resources) | **Read in full on 2026-11-06** |
| Third-party exam posts | Read for study advice only. See `02-modules/13-certification/references.md`. Anecdotal or unverified |
| Playlist transcripts (23, auto-captions), in `01-source-map/transcripts/` | Downloaded and keyword-mapped, **not read in full**. See `PLAYLIST_ANALYSIS.md`. Read the relevant ones while authoring each module |

## How Anthropic solved it: planned sources

The "problem" column is my expectation from prior knowledge and must be confirmed at authoring, except where the status says it was read.

| Lesson | Problem | Anthropic source | Status |
| --- | --- | --- | --- |
| AI05 L3 | Chunks lose context | [Contextual Retrieval](https://www.anthropic.com/engineering/contextual-retrieval) | **Read; used in pilot** |
| AI02 L4 | Scaling human feedback for harmlessness | [Constitutional AI paper](https://arxiv.org/abs/2212.08073) | **Abstract read; used in AI02 L4** |
| AI02 L4 | Stating intended model behavior | [Claude's constitution](https://www.anthropic.com/constitution) | **First half read; used in AI02 L4** |
| AI02 L6, AI11 L3 | Models are opaque | [Mapping the Mind](https://www.anthropic.com/research/mapping-mind-language-model), [Tracing Thoughts](https://www.anthropic.com/research/tracing-thoughts-language-model) | **Read; used in AI02 L6.** The full [Scaling Monosemanticity](https://transformer-circuits.pub/2024/scaling-monosemanticity/) paper was too large to fetch |
| AI03 L5 | Re-sending long prefixes is costly | [Prompt caching docs](https://platform.claude.com/docs/en/build-with-claude/prompt-caching) | **Read in part; used in AI03 L5** |
| AI03 L6 | Attention is finite in long agent runs | [Effective context engineering](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents) | **Read; used in AI03 L2 and L6** |
| AI06 L1 | Teams over-build agents | [Building effective agents](https://www.anthropic.com/engineering/building-effective-agents) | **Read via outline; used in AI06** |
| AI06 L3 | Tools written for developers confuse agents | [Writing tools for agents](https://www.anthropic.com/engineering/writing-tools-for-agents) | **Read via outline; used in AI06 L3** |
| AI06 L3, L4 | Tool definitions consume context | [Advanced tool use](https://www.anthropic.com/engineering/advanced-tool-use), [Code execution with MCP](https://www.anthropic.com/engineering/code-execution-with-mcp) | **Read via outline; used in AI06 L3** |
| AI06 L4 | N×M integration problem | [Introducing MCP](https://www.anthropic.com/news/model-context-protocol) | **Read via outline; used in AI06 L4** |
| AI06 L6 | Coordinating agents on research | [How we built our multi-agent research system](https://www.anthropic.com/engineering/multi-agent-research-system) | **Read via outline; used in AI06 L6.** One of the few published looks at a real Anthropic production system |
| AI06 L7 | Work spanning many context windows | [Effective harnesses for long-running agents](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents) | **Read via outline; used in AI06 L7** |
| AI09 L2 | Injection through content | [Prompt injection defenses](https://www.anthropic.com/research/prompt-injection-defenses) | Link OK |
| AI09 L3 | Permission fatigue and blast radius | [Claude Code sandboxing](https://www.anthropic.com/engineering/claude-code-sandboxing) | Link OK |
| AI09 L5 | Universal jailbreaks | [Constitutional Classifiers](https://www.anthropic.com/research/constitutional-classifiers), [Many-shot jailbreaking](https://www.anthropic.com/research/many-shot-jailbreaking) | Link OK |
| AI11 L3 | Deceptive training behavior | [Alignment faking](https://www.anthropic.com/research/alignment-faking) | Link OK |
| AI12 | Effective use of Claude Code | [Claude Code best practices](https://www.anthropic.com/engineering/claude-code-best-practices) | Link OK |
| AI03, AI06, AI12 | Current product behavior | [Subagents](https://code.claude.com/docs/en/sub-agents), [Skills](https://code.claude.com/docs/en/skills) | **Read in part, for the blueprint's differences table** |

## Real production systems

**This is a shortlist, not findings.** Each still has to be read before use, and I'll add more per module. Use only architecture that a company has published. Everything else is labeled as a recommended reference design.

| Lesson | System | Link check |
| --- | --- | --- |
| AI10, AI05 | [LinkedIn: building a generative AI product](https://www.linkedin.com/blog/engineering/generative-ai/musings-on-building-a-generative-ai-product) | **Read; used in AI05** |
| AI05 | [Uber QueryGPT](https://www.uber.com/blog/query-gpt/) | **Read; used in AI05 L5.** No accuracy figure is published |
| AI05, AI06 | [DoorDash RAG support chatbot](https://careersatdoordash.com/blog/large-scale-rag-llm-chatbot/) | **Blocked (403). Not read, and not cited** |
| AI06 L6 | Anthropic's multi-agent research system (above) | Link OK |

## Papers and standards

| Lesson | Source | Status |
| --- | --- | --- |
| AI02 L2–L3 | [Attention Is All You Need](https://arxiv.org/abs/1706.03762) | **Key parts read** |
| AI05 L1 | [RAG paper](https://arxiv.org/abs/2005.11401) | **Abstract read** |
| AI05 L4 | [HyDE](https://arxiv.org/abs/2212.10496) | **Abstract read** |
| AI05 L5 | [GraphRAG](https://arxiv.org/abs/2404.16130) | **Abstract read** |
| AI05 L5 | [Self-RAG](https://arxiv.org/abs/2310.11511) | **Abstract read** |
| AI05 L6 | [Ragas](https://arxiv.org/abs/2309.15217) | **Abstract read** |
| AI05 L4 | [Citations docs](https://platform.claude.com/docs/en/build-with-claude/citations) | **Read** |
| AI04 L2 | [HNSW paper](https://arxiv.org/abs/1603.09320) | Link OK |
| AI06 L2 | [ReAct paper](https://arxiv.org/abs/2210.03629) | Link OK |
| AI02 L6, AI03 L6 | [Lost in the Middle](https://arxiv.org/abs/2307.03172) | **Abstract read** |
| AI09 L1 | [OWASP Top 10 for LLM Applications](https://genai.owasp.org/llm-top-10/) | Link OK |
| AI07 L4 | [Spring AI](https://docs.spring.io/spring-ai/reference/), [LangChain4j](https://docs.langchain4j.dev/) | Link OK |
| AI08 L4 | [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) | Link OK |
| AI04 L1 | [Claude embeddings guide](https://platform.claude.com/docs/en/build-with-claude/embeddings) | **Read.** Anthropic offers no embedding model of its own; the guide covers Voyage AI |

## Your PDFs in `D:\MSA63\Notes\`

Read-only topic checklists, not sources of fact.

| File | Status |
| --- | --- |
| `MCP From Scratch to Advance _ Developer Handbook.pdf` (177 pages, text) | **Table of contents read.** Its 16 sections inform AI06 L4–L5 and AI08 L4 |
| `Master_RAG_Architectures_DeepDive.pdf` (5 pages, text) | **Its 9 RAG types listed.** All covered by AI05 L5 and AI04 L3. Its text refers to source images that aren't included |
| AI Roadmap, AI engineering, AI Interview cheat sheet, AI Interview handbook, 2 Deep Learning, LLM complete handbook, LangChain Handbook, ML Algorithms, Prompt engineering, Pytorch handbook, Rag and LLM, System Design for AI_LLM Applications, 2 Transformer | **Image-only, not read.** The PDF page renderer wasn't available to the Read tool in this session. To gap-check them, render pages using the course pipeline's tools at the start of the matching module |

## AI09, read 2026-11-05

Depth for each is in `02-modules/09-security/references.md`.

| Source | Status |
| --- | --- |
| Anthropic: prompt injection defenses, Claude Code sandboxing, Constitutional Classifiers, many-shot jailbreaking | Read as fetched outlines. Quotes limited to marked text |
| Mitigate jailbreaks and prompt injections, Claude Code security, MCP security best practices 2026-07-28 | Read in full |
| Claude Code permissions, API and data retention, prompt caching | Relevant sections read |
| OWASP LLM Top 10 2025 | **List page only** |
| CaMeL, Design Patterns for Securing LLM Agents | **Abstracts only** |
| Simon Willison, The lethal trifecta | Read as a fetched outline |

## AI10, read 2026-11-05

Depth for each is in `02-modules/10-production/references.md`.

| Source | Status |
| --- | --- |
| Rate limits, errors, service tiers, model deprecations, Other LLM gateways | Read in full |
| Pricing, gateway compatibility guide, bundled API reference files | Relevant sections read |
| Demystifying evals, evaluation guide | Read as fetched outlines |
| OpenTelemetry GenAI conventions repository | **README only** |

## AI11, read 2026-11-05

Depth for each is in `02-modules/11-advanced/references.md`.

| Source | Status |
| --- | --- |
| Alignment faking, Sleeper Agents, Agentic Misalignment, small-samples poisoning, Mapping the Mind, Tracing Thoughts, Measuring agent autonomy | Read as fetched outlines |
| Computer use tool, code execution tool | Relevant sections read |
| LoRA, QLoRA, vLLM papers | **Abstracts only** |
| Hugging Face quantization overview | Read in full |
| Temporal: understanding Temporal | Read as a fetched outline |
| Claude 3 Haiku fine-tuning post | Read as a fetched outline. Current availability not confirmed |

## AI12, read 2026-11-06

Depth for each is in `02-modules/12-claude-code/references.md`. Claude Code version 2.1.318.

| Source | Status |
| --- | --- |
| Sessions, headless, GitHub Actions, Agent SDK overview | Read in full |
| Memory, hooks guide, best practices, tools reference | Relevant sections read |
| Skills, subagents, permission modes | Outlines of the first 100,000 characters |
