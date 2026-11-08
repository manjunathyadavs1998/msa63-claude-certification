# AI02: References and Verification

Numbers are recomputed by `scripts/verify_ai02.py`, or come from running `code/ai02-llm/`. Last checked 2026-11-04.

## Demo output

All demos are teaching code using invented data, unless a public configuration is named. Each uses fixed seeds where randomness is involved.

| Lesson | File | Status |
| --- | --- | --- |
| L1 | `Bpe.java` | Run, and output quoted exactly. A 30-merge toy, so its counts don't represent real tokenizers |
| L1 | `sdk/.../TokenBudget.java` | **Compiled against `anthropic-java` 3.2.0. Not run**, since no API key is available |
| L2 | `Attention.java` | Run, and output quoted exactly. Vectors are hand-written, and Q, K and V share one vector |
| L3 | `TransformerSize.java` | Run, and output quoted exactly |
| L4, L6 | `TinyLm.java` | Run, and output quoted exactly. A bigram counter, not an LLM |
| L5 | `Serving.java` | Run, and output quoted exactly. The batching figures are a simulation with invented lengths |
| L6, L7 | `Snippets.java` | Run. Compiles and exercises `withinPolicy` and `ModelOption` |

## Hand-derived figures

| Claim | Recomputed | Verdict |
| --- | --- | --- |
| L2 Q2: d_k and pairs | 64, 16,000,000 | ✓ |
| L3: GPT-2 small parameters | 124,439,808 from the formula | ✓ Matches the widely reported count (recalled, not looked up in a primary source) |
| L3: GPT-2 XL parameters | 1,557,611,200 from the formula | ✓ Same note |
| L3: scale ratios | 2.08×, 4×, 12.52× | ✓ |
| L3 Q1: feed-forward at d = 512 | 2,099,712 | ✓ |
| L3 Q3: 70B at 16-bit | 140 GB, consistent with HLD M21D | ✓ |
| L4: ln(28) | 3.332 | ✓ |
| L5: KV cache count | 1,124,750 vs 2,500 | ✓ |
| L5 Q3 | 10.8 s | ✓ |
| L7: cost per 1M requests | $45,000, $18,000, $9,000, $450 | ✓ |
| L7: cost per completed task | $0.0380, $0.0490, $0.3005, $0.0305 | ✓ Error rates are **invented** |
| L7 Q2 | $3,000 | ✓ |
| Exercises 2–5 | All match the script | ✓ |
| Exercise 1 | Pair counts checked by script | **Corrected**: a three-way tie, not two |

**Errors found in my own drafts:** the Exercise 1 tie above. Also, L4 called InstructGPT's gap "100 times". The real ratio is about 135×, and the paper's own phrase is "100x fewer", so L4 now quotes the paper.

## Sources read

| Claim | Source | How it was checked |
| --- | --- | --- |
| ~30% more tokens on Claude 4.7+, estimate, free, separate limit, rejected inputs | [Token counting docs](https://platform.claude.com/docs/en/build-with-claude/token-counting) | Read in full |
| Attention formula, scaling reason, 6 layers, 512, 8 heads, 2,048, sinusoids, Adam, warm-up, dropout, label smoothing | [Vaswani et al. 2017](https://arxiv.org/abs/1706.03762) | Key parts of the paper read |
| RLHF results at Anthropic | [Bai et al. 2022, HH](https://arxiv.org/abs/2204.05862) | Abstract read |
| Constitutional AI's phases and goals | [Bai et al. 2022, CAI](https://arxiv.org/abs/2212.08073) | Abstract read |
| Claude's constitution: role and priority order | [Constitution](https://www.anthropic.com/constitution) | First half of the page read |
| Sycophancy findings | [Sharma et al. 2023](https://arxiv.org/abs/2310.13548) | Abstract read |
| 1.3B vs 175B preference result | [Ouyang et al. 2022](https://arxiv.org/abs/2203.02155) | Abstract read |
| Power-law scaling | [Kaplan et al. 2020](https://arxiv.org/abs/2001.08361) | Abstract read |
| KV cache description, PagedAttention, 2–4× | [Kwon et al. 2023](https://arxiv.org/abs/2309.06180) | Abstract read |
| Default-decline circuit, mental math, unfaithful reasoning, limits | [Tracing Thoughts](https://www.anthropic.com/research/tracing-thoughts-language-model) | Page read |
| Features, Golden Gate Bridge, steering, limits | [Mapping the Mind](https://www.anthropic.com/research/mapping-mind-language-model) | Page read. The full Scaling Monosemanticity paper was too large to fetch |
| Lost in the Middle | [Liu et al. 2023](https://arxiv.org/abs/2307.03172) | Abstract read |
| Calibration, weak on new tasks | [Kadavath et al. 2022](https://arxiv.org/abs/2207.05221) | Abstract read |
| Exam scope and task statements | Official guide | Read in full |

## Title confirmed only

BPE for language modeling ([Sennrich](https://arxiv.org/abs/1508.07909)), [RoPE](https://arxiv.org/abs/2104.09864), [speculative decoding](https://arxiv.org/abs/2211.17192), [T5](https://arxiv.org/abs/1910.10683), [GAN](https://arxiv.org/abs/1406.2661), [VAE](https://arxiv.org/abs/1312.6114), [DDPM](https://arxiv.org/abs/2006.11239). Each is cited only for its basic, well-known idea.

## From the API reference bundled with Claude Code (`claude-api` skill, cached 2026-10-06)

**Version-sensitive. Check before relying on these.**

- Model IDs, prices, 1M context windows, and Haiku 5.5's price condition.
- Sampling parameters rejected at API version `2026-10-21`.
- The list of `stop_reason` values, `refusal` handling and server-side fallbacks.
- Forced `tool_choice` returning 400 on Opus 5.5, Sonnet 5.5 and Fable 5.1, and thinking being non-disableable on Opus 5.5.
- Fable 5.1's 30-day retention requirement.
- Fast mode's 2.5× figure and research-preview status.
- Advice to measure lower effort before building a cascade, and caches being model-scoped.
- SDKs requiring streaming for very large `max_tokens`.

The linked pricing, models, batch and citations pages responded, but **I read the prices from that reference, not those pages**.

## Inference, not documented fact

These are labeled as inference or analogy in the lessons:

- Output prices being higher because generation is sequential.
- Batch discounts existing because the work can wait for capacity.
- Prompt caching being related to prefix reuse. Anthropic's internals aren't public.
- The "good starting point" column of the tier table in L7, which is my recommendation.

## Stated from general knowledge

WordPiece and BERT, SentencePiece, weight tying in GPT-2, GPT-2's configurations, DPO, beam search, that Kaplan's co-authors include later Anthropic founders, and the patch-based explanation of multimodal input. Standard facts, not checked against a specific source here.

## Engineering recommendations, not facts

Every production architecture in this module. **None describes a specific company's system**, and Anthropic doesn't publish its training or serving architecture.

L2 and L3 give a production consequences table, rather than the usual three-stage architecture. They explain model internals, and a forced architecture would have added nothing. AI02 L5 and HLD M21D hold the relevant designs.

## Topic check against your PDFs

Checked against `LLM complete handbook.pdf` and `Transformer handbook .pdf`, using page thumbnails as topic checklists only. No facts were taken from them.

| Their topic | Covered in |
| --- | --- |
| LLM basics, tokenization, embeddings | L1, L2 |
| Self-attention, multi-head, positional encoding, encoder and decoder blocks | L2, L3 |
| Training pipeline, fine-tuning and alignment | L4, AI11 L1 |
| Inference, decoding strategies, stopping, optimizations | L5 |
| Evaluation of LLMs | AI10 L3 |
| Prompt engineering | AI03 L2 |
| Ecosystem and frameworks | AI07 |
| Multimodal, future trends | L7, AI11 |

**One thing to watch:** the handbook's model tables list older models and context sizes, such as Claude 3 at 200K. Don't rely on them. Use the current models page.
