# AI05: References and Verification

Numbers are recomputed by `scripts/verify_ai05.py`, or come from running `code/ai05-rag/`. Last checked 2026-11-04.

## Code status

| Lesson | File | Status |
| --- | --- | --- |
| L1 | `QuestionRouter.java` | Run, and output quoted exactly. Four invented questions, one deliberately misrouted |
| L2 | `ChunkLab.java` | Run, and output quoted exactly. One invented 804-character policy with 9 facts. Sizes are in characters, not tokens |
| L3 | `ChunkContextualizer.java` | **Compiled, not run**. No API key |
| L3 | `RankFusion.java` | Run, and output quoted exactly |
| L4 | `CitedAnswers.java` | **Compiled, not run**. No API key. `anthropic-java` 3.2.0 |
| L4 | `Provenance.java` | Run, and output quoted exactly. Invented data |
| L5 | `SqlGuard.java` | Run, and output condensed to one line per query. Regex sketch, and the lesson says that it isn't a security boundary |
| L6 | `RagEval.java` | Run. **Results are scripted.** It tests the harness, not a model or retriever |
| L7 | `KnowledgeAssistant.java` | Run with fake services. No search engine, directory or model is involved |
| L7 | SQL schema | **Not run** |

**No lesson in AI05 has run against the Claude API, a real embedding model or a real search engine.**

## Findings worth remembering, with their limits

| Finding | Limit |
| --- | --- |
| Structure-aware chunking kept all 9 facts with 836 characters. Fixed-size needed 50% overlap and 1,504 | One tiny, clean, invented document with obvious headings. Real documents will be messier |
| Overall eval row pointed at retrieval, and one segment needed a generation fix | Scripted numbers, chosen to show the method |
| Cached 150K prefix cheaper per question than 16K uncached | Input only, assumes a warm cache, and ignores dilution and infrastructure |
| $0.39 per employee per month in L7 | Every input is an assumption, and infrastructure, embedding and reranking aren't included |

## Hand-derived figures

All match `verify_ai05.py`: L1's cache arithmetic and Q2, L2 Q2, L3's reductions, L6's rates, margins, judge calibration and run cost, L7's estimation tables, rate-limit figures and semantic cache saving, and Exercises 1, 3, 4 and 5.

**An error found in my own draft:** L2 Q2 first gave two stored-token answers, and it now gives 1,300, which the script confirms.

## Sources read

| Claim | Source | How it was checked |
| --- | --- | --- |
| 200,000 tokens guidance, 5.7% to 1.9%, top 150 to 20, $1.02 per million | [Contextual Retrieval](https://www.anthropic.com/engineering/contextual-retrieval) | Read. The $1.02 is a 2024 figure for Haiku 3 |
| Parametric and non-parametric memory | [RAG paper](https://arxiv.org/abs/2005.11401) | Abstract read |
| All citation behaviors, incompatibility with structured outputs | [Citations docs](https://platform.claude.com/docs/en/build-with-claude/citations) | Read |
| Routing, retrieval and generation, 80% then four months, linguists, 10% to 0.01% | [LinkedIn](https://www.linkedin.com/blog/engineering/generative-ai/musings-on-building-a-generative-ai-product) | Read |
| Workspaces, agents, 1.2M queries, 10 to 3 minutes, 300 users, 78%, unsolved hallucination | [Uber QueryGPT](https://www.uber.com/blog/query-gpt/) | Read. No accuracy figure is published, and the lesson says so |
| Global questions, entity graph, community summaries | [GraphRAG](https://arxiv.org/abs/2404.16130) | Abstract read |
| Reference-free metrics | [Ragas](https://arxiv.org/abs/2309.15217) | Abstract read |
| Reflection tokens, adaptive retrieval | [Self-RAG](https://arxiv.org/abs/2310.11511) | Abstract read |
| Hypothetical document embedding | [HyDE](https://arxiv.org/abs/2212.10496) | Abstract read |
| Position effects | [Lost in the Middle](https://arxiv.org/abs/2307.03172) | Abstract read, in AI02 |
| `glob`, `grep` and `CLAUDE.md` in Claude Code | [Context engineering post](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents) | Read, in AI03 |
| TS 5.1, 5.3, 5.5, 5.6, 1.3, 4.1, 4.4, 4.5, 4.6 | Exam guide v1.0 | Read |
| Pipeline, chunk sizes, RAG vs fine-tuning, 2×2 diagnosis | HLD M21B | Read. Cross-referenced, not re-taught |

## Not used

- **DoorDash's RAG chatbot post.** The site blocked automated reading with a 403, so nothing in this module cites it.
- `Master_RAG_Architectures_DeepDive.pdf` was used only as a checklist of 9 types. All 9 appear in L5.
- Anthropic's multi-agent research post is **not read yet**. L4 only points to AI06 L6, which must read it.

## Stated from general knowledge

Table-of-contents navigation in L5, labeled there as such. Multimodal retrieval approaches. Chunking strategy descriptions: semantic, recursive, parent-child. Parser pitfalls. The golden-set guidance and online signals. Margin of error, using the normal approximation. OpenSearch as an example of a hybrid engine.

## Engineering recommendations, not facts

Every production architecture and the L7 design, all targets and thresholds including 0.40, 15 minutes and 95%, the router rules, the choice of 8 chunks, the per-segment gates and the zero-failure rule. **None describes a specific company's system.**

## Depth note

HLD M21B already teaches the pipeline, so lessons came in at 125 of 186 budgeted pages. The budget wasn't a target, and the page count reflects restating nothing.
