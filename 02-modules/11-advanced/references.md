# AI11: References and Verification

Numbers are recomputed by `scripts/verify_ai11.py`, which also checks lesson Java against the sources. Outputs come from `code/ai11-advanced/`. Last checked 2026-11-05.

## How to run it

```bash
cd code/ai11-advanced
javac --release 21 -d out src/course/advanced/*.java
java -cp out course.advanced.LoraLab      # also QuantLab, SparseLab, AutonomyLab, SagaLab
```

`AutonomyLab` needs `python` on the path, and takes about 20 seconds, as it waits for child processes.

## Code status

| Lesson | Code | Status |
| --- | --- | --- |
| L1 | `LoraLab` | Run. Real training of a 40 × 40 linear layer, and a logistic toy for poisoning. Chosen to suit low rank |
| L2 | `QuantLab` | Run. Real rounding of a small network. GPU price, throughput and salary are assumptions |
| L3 | `SparseLab` | Run. A real sparse autoencoder, on planted features. Hyperparameters were tuned until recovery worked |
| L4 | `AutonomyLab` | Run: real child processes on Windows. It limits time and output only, and it's no sandbox |
| L5 | `SagaLab` | Run. My own teaching engine, and not Temporal. The "model" is a random stand-in |

## What has never happened

- **No real model, LoRA tool, serving engine, GPU, Temporal, computer use or code execution tool was run.** Those facts come from papers and docs.
- No open model's licence was reviewed.
- No experiment here measures Claude.

## Findings, with their limits

| Finding | Limit |
| --- | --- |
| Rank 2 beat full fine-tuning by about 9× at 100 examples | A linear toy whose true change is rank 2 |
| A 100-fold growth in clean data cut a backdoor from 98% to 89%, at 100 poisoned examples | Toy. At 5 poisoned examples, it fell from 64% to 21%, so count doesn't fully decide it here |
| Quantizing down to 3 bits held up, with a collapse at 2 | 288 weights, and it says nothing about an LLM |
| Hook's break-even, about 155M requests a month | My assumptions, and a small API model. Larger models move it down |
| Autoencoder recovered 59 of 60 features at 3% density, and none at 40% | Planted features, and tuned hyperparameters |
| A child process outlives a killed parent, and tree-kill stopped it | On Windows, this once. Other OSs differ, and detached processes may escape |
| Durable history alone left 3 of 8 crash points wrong, and keys fixed them | My engine's model of crash points |
| Unrecorded replay changed the decision 42% of the time | A stand-in with a 70/30 split, where theory gives 42% |

## Defects in my own work, found before publishing

| Defect | How found |
| --- | --- |
| First poisoning test used fixed total steps, so share decided the result. It didn't model training by epochs | Output contradicted the source, and I found why |
| First autoencoder settings recovered nothing | Output no better than random. Tuned the penalty and rate |
| First process test: no child wrote a file, so "kill the parent" looked safe | Added a control run, and fixed the quoting by using script files |
| A shell heredoc mangled backslashes in generated Java | Compile errors. Known hazard |
| L5 diagram caption claimed "most real systems" | Rereading. Reworded |

## Sources read

Read on 2026-11-05 unless noted. Outlines were produced by a fetch tool, and I quote only text it marked as quoted.

| Source | Read how | Used for |
| --- | --- | --- |
| [LoRA](https://arxiv.org/abs/2106.09685), Hu et al. | **Abstract only** | L1 |
| [QLoRA](https://arxiv.org/abs/2305.14314), Dettmers et al. | **Abstract only** | L1 |
| [Fine-tune Claude 3 Haiku](https://claude.com/blog/fine-tune-claude-3-haiku), 2024 | Outline. **Current availability not confirmed** | L1 |
| [A small number of samples can poison LLMs](https://www.anthropic.com/research/small-samples-poison), Oct 2025 | Outline | L1, L3 |
| [Transformers quantization overview](https://huggingface.co/docs/transformers/main/en/quantization/overview) | In full | L2 |
| [PagedAttention / vLLM](https://arxiv.org/abs/2309.06180), Kwon et al. | **Abstract only** | L2 |
| [Mapping the Mind](https://www.anthropic.com/research/mapping-mind-language-model), May 2024 | Outline | L3 |
| [Tracing Thoughts](https://www.anthropic.com/research/tracing-thoughts-language-model), Mar 2025 | Outline | L3 |
| [Sleeper Agents](https://www.anthropic.com/research/sleeper-agents-training-deceptive-llms-that-persist-through-safety-training), Jan 2024 | Outline | L3 |
| [Alignment faking](https://www.anthropic.com/research/alignment-faking), Dec 2024 | Outline | L3 |
| [Agentic misalignment](https://www.anthropic.com/research/agentic-misalignment), Jun 2025 | Outline | L3 |
| [Measuring AI agent autonomy](https://www.anthropic.com/research/measuring-agent-autonomy), Feb 2026 | Outline | L4 |
| [Computer use tool](https://platform.claude.com/docs/en/agents-and-tools/tool-use/computer-use-tool) | Outline, first 100,000 characters | L4 |
| [Code execution tool](https://platform.claude.com/docs/en/agents-and-tools/tool-use/code-execution-tool) | Relevant sections | L4 |
| [Understanding Temporal](https://docs.temporal.io/evaluate/understanding-temporal) | Outline | L5 |
| Bundled API reference: Managed Agents scheduled deployments and webhooks | Relevant sections | L5 |
| HLD M21D, M11, M08 | Headings, as cross-references | L2, L5 |

## Not verified

- Whether Anthropic still offers Haiku fine-tuning, or for which models.
- That fine-tuning weakens refusals: stated as general knowledge in L1, with no study read.
- Model-file supply chain risks: stated as general knowledge in L2.
- Whether vLLM's 2–4× holds today, or on your hardware.
- All these research results are on older models, and I haven't read equivalents for current ones.
