# AI10: References and Verification

Numbers are recomputed by `scripts/verify_ai10.py`, which also checks lesson Java against the sources. Outputs come from `code/ai10-production/` and `code/ai08-java/`. Last checked 2026-11-05.

## How to run it

```bash
cd code/ai10-production
javac --release 21 -d out src/course/prod/*.java
java -cp out course.prod.ReliabilityLab     # also EvalLab, CostLab, OpsLab, GatewayLab

cd ../ai08-java
mvn -B test < /dev/null                     # 32 tests. SpendCapTest prints a LAB| line
```

## Code status

| Lesson | Code | Status |
| --- | --- | --- |
| L1 | `ReliabilityLab` | Run. A simulation with invented traffic and capacity, from a fixed seed |
| L1 | `SpendCapTest` | Run: real SDK and `ClaudeModel`, against a local fake. The error body is copied from the rate limits page |
| L2, L4, L5 | `CostLab` | Run. Prices from the pricing page. Success rates and rework costs are illustrative. The embedding is a toy |
| L3 | `EvalLab` | Run. **All data is synthetic, generated from a seed with accuracy I chose** |
| L4, L6 | `OpsLab` | Run. Simulations on invented traffic |
| L2, L7 | `GatewayLab` | Run: real HTTP, SSE and threads on localhost, with a fake model server whose events I wrote from the docs |
| L7 | SQL schema, OTel attributes, leases | **Not run** |

## What has never happened

- **No real model or provider was called.** The gateway's timings are local loopback on one laptop, and they vary.
- The gateway hasn't been load-tested, so its replica count is an assumption. It's a sketch: JSON is read by regex, and reservations have no expiry.
- No gateway, tracing or guardrail product was evaluated.
- OTel export wasn't run, so the token rules come from the bundled reference.
- Real-provider behaviour on client disconnect, including what it bills, wasn't tested.

## Findings, with their limits

| Finding | Limit |
| --- | --- |
| AI08's `ClaudeModel` treats a spend-cap 429 as retryable, and the SDK makes 3 requests | Against a fake, with the docs' body. Left in place, with a test. Exercise 1 fixes it |
| Bundled API reference gives $0.20 for Sonnet 5.5 cache reads, while the pricing page gives $0.10 (0.05x) | **Two Anthropic sources disagree.** I used the pricing page, and corrected AI06 L9 and AI08 L8. Check before relying on it |
| The pricing page's prose also says a cache hit costs 10% of input, while its footnotes give 0.05x for Opus 5.5 and Sonnet 5.5, and 0.025x for Fable 5.1 | I used the table and footnotes |
| The bundled reference gives two different beta header dates for `fallbacks` | L1 says to read the current page |
| Check-then-spend budget overspent by 10× in a race; reservation held at exactly the limit | My test's timings: each call takes 20 ms |
| Buffering makes first text equal whole-stream time | Local fake, and the shape is what matters |
| Global confidence threshold still accepted a weak segment at 79% | Synthetic data, designed to have that segment |
| A fixed gate on 50 tasks passes a true 85% rate in 22.6% of runs | Pure binomial simulation, and independent tasks assumed |
| In a simple outage simulation, jitter served fewer than plain backoff | **An artifact of a model where recovery capacity isn't the constraint.** Don't generalize |

## Defects in my own work, found before publishing

| Defect | How found |
| --- | --- |
| Sonnet 5.5 cache read priced at $0.20 in the first `CostLab`, and in two earlier lessons | Reading the pricing page |
| A comment said stated confidence was overconfident, when the output showed mixed | Reading the output |
| First budget test used too large a budget, so no call was refused | Reading the output |
| L7 estimates used rounded inputs inconsistently (3,472 and 579) | `verify_ai10.py` |
| Five lessons had only 2 diagrams, under the rule of 3 | Counting |
| Lesson snippet for the Wilson margin didn't match wrapped source | `verify_ai10.py` |

## Sources read

Read on 2026-11-05. Outlines were produced by a fetch tool, and I quote only text it marked as quoted, or text from pages returned verbatim.

| Source | Read how | Used for |
| --- | --- | --- |
| [Rate limits](https://platform.claude.com/docs/en/api/rate-limits) | In full | L1, L4, L6, L7 |
| [Errors](https://platform.claude.com/docs/en/api/errors) | In full | L1 |
| [Service tiers](https://platform.claude.com/docs/en/api/service-tiers) | In full | L1 |
| [Pricing](https://platform.claude.com/docs/en/about-claude/pricing) | Tables and relevant sections | L2, L5, L6, L7 |
| [Model deprecations](https://platform.claude.com/docs/en/about-claude/model-deprecations) | In full | L6 |
| [API and data retention](https://platform.claude.com/docs/en/manage-claude/api-and-data-retention) | Sections, in AI09 | L5, L6 |
| [Demystifying evals for AI agents](https://www.anthropic.com/engineering/demystifying-evals-for-ai-agents), Anthropic, Jan 2026 | Outline | L3 |
| [Define success criteria and build evaluations](https://platform.claude.com/docs/en/test-and-evaluate/develop-tests) | Outline, first 100,000 characters | L3 |
| [Other LLM gateways](https://code.claude.com/docs/en/llm-gateway) | In full | L2 |
| [Gateway compatibility guide](https://code.claude.com/docs/en/llm-gateway-protocol) | Sections on headers, streaming and responses | L2, L7 |
| Bundled API reference: `opentelemetry.md`, `cost-optimization.md`, `error-codes.md`, `model-migration.md` | Relevant sections | L1, L2, L4, L5 |
| [OpenTelemetry GenAI conventions repository](https://github.com/open-telemetry/semantic-conventions-genai) | **README only** | L4 |
| Exam guide TS 5.5 | Read from the PDF | L3 |
| HLD M11, M13, M04, M21 | Headings, as cross-references | L1, L4, L2 |

## Not verified

- PSI thresholds of 0.1 and 0.25 are common lore. L4 says so.
- Who bills what when a client disconnects mid-stream.
- Whether `anthropic-java` has a retry hook that could stop the spend-cap retries (Exercise 1 says so).
- Slack or vendor behaviours aren't relied on here.
- Availability figures, canary traffic and estimation inputs are all stated assumptions.
