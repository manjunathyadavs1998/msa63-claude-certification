# Gap Analysis

What existing material covers, and what this course must add. Based on what's been checked so far. Items not yet read are marked.

## Overlap with your other courses

Rule: deepen and cross-reference rather than restate. Each course's content was checked by its lesson headings only, so re-read the relevant lesson before authoring.

| Existing | Covers | This course |
| --- | --- | --- |
| HLD M21A | Embeddings, IVF, PQ, HNSW, vector databases | AI04 recaps briefly, then adds filtering, operations, product choice and multi-tenant design |
| HLD M21B | Building LLM applications | AI03 and AI05 go deeper, and specific to Claude |
| HLD M21C, M21E | ML systems, recommendation | Cross-referenced. Not retaught |
| HLD M21D | Design ChatGPT (LLM chat serving) | AI02 L5 cross-references it for serving internals. AI10 L7 designs a different system, an LLM gateway |
| HLD M08, M11 | Kafka, reliability, rate limiting | AI08 L6 and AI10 L1 apply them to LLM traffic |
| LLD M23 | Design patterns | Strategy and Chain of Responsibility referenced in AI07 and AI10 |
| Java 15, 20, 21, 22 | Idempotency, resilience, Kafka, sagas | AI08 and AI11 L5 reference them |
| Java 29 | Brief survey of AI-assisted development, RAG, Spring AI | Superseded in depth by AI03, AI05, AI07 and AI08 |
| AWS notes | Cloud infrastructure | AI10 L6 references them rather than reteaching them |

## Exam coverage gaps in the playlist

From the keyword analysis in `01-source-map/PLAYLIST_ANALYSIS.md`. These were found by term search, so the videos may still show them on slides.

| Topic | Evidence | Home |
| --- | --- | --- |
| `detected_pattern` field (TS 4.4) | No transcript mentions it | AI03 L3, AI13 L5 |
| Attention dilution (TS 1.6, 4.6) | Term not found | AI02 L6, AI06, AI13 |
| Stratified sampling, calibration (TS 5.5) | Only video 21 | AI10 L3, AI13 L6 |
| Path-specific rules (TS 3.3) | Mostly video 11 | AI12 L2 |
| Agent SDK by name | 6 mentions | AI06, AI07 L1 |
| Java | 6 passing mentions | All of AI08 |

## Topics not on the exam at all

Excluded by the guide's out-of-scope list, yet required for your production goal. Each lesson's Certification lens says so plainly.

| Topic | Module |
| --- | --- |
| ML fundamentals, transformer internals, tokenization, training, RLHF, Constitutional AI | AI01, AI02 |
| Embedding models, vector database details | AI04, parts of AI05 |
| Streaming, API auth, rate limits, cloud deployment | AI08, AI10 |
| Fine-tuning, model hosting, computer use, vision | AI11 |

Together these make up a large share of the course. If your only goal were passing the exam, far fewer pages would be needed. You asked for production depth as well, so they stay in.

## Risks to this plan

| Risk | Effect | Mitigation |
| --- | --- | --- |
| **Size:** planned at over 2,000 pages | High token cost, across many sessions | One module at a time, and each is a safe stopping point |
| **The exam guide changes.** It's from July 2026 | AI13 could go stale | Re-check the certification page before writing AI12 and AI13 |
| **Products change quickly,** as with the 2026 API changes | Version-specific notes age | Record version-sensitive claims in each `references.md`, with a date |
| **Few published production architectures** | "Real production" content is thinner than desired | Use what's published, and label the rest as recommended designs. Never fill the gap with invented detail |
| **Code calling the API can't be run** without a key | Only compile-verified | Label it in the lesson. Setting `ANTHROPIC_API_KEY` would allow running it |
| **Eligibility unconfirmed** | You may be unable to sit the exam | Check with your employer. See the blueprint |
| **Reference-design opinions** | Could be mistaken for fact | Rule 3 in `CLAUDE.md` |

## Files from the first pass

Moved rather than deleted. They don't follow this format, and will be rebuilt.

| File | Now at | Becomes |
| --- | --- | --- |
| `01_Certification_Blueprint.md` | `01-source-map/CERTIFICATION_BLUEPRINT.md` | Kept. It's the exam reference |
| `02_Playlist_Analysis.md` | `01-source-map/PLAYLIST_ANALYSIS.md` | Kept |
| `03_Learning_Roadmap.md` | `00-planning/archive/` | Replaced by `COURSE_ROADMAP.md` |
| `04_Exam_Question_Bank.md` | `00-planning/archive/` | Replaced by AI13. Its answer letters were unbalanced |
| `modules/module-01/` | `00-planning/archive/modules/` | Replaced by AI01 and AI02. The Java demo can be reused |
