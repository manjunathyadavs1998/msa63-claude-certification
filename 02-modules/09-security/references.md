# AI09: References and Verification

Numbers are recomputed by `scripts/verify_ai09.py`, which also checks lesson Java against the sources. Outputs come from `code/ai09-security/` and `code/ai08-java/`. Last checked 2026-11-05.

## How to run it

```bash
cd code/ai09-security
javac --release 21 -d out src/course/security/*.java
java -cp out course.security.TrifectaCheck     # also InjectionContainment, ToolGuards, DataGuards, GuardrailMath

cd ../ai08-java
mvn -B test < /dev/null                        # 31 tests. SecurityLabTest prints LAB| lines
```

## Code status

| Lesson | Code | Status |
| --- | --- | --- |
| L1 | `TrifectaCheck` | Run. Reads declared properties only |
| L2 | `InjectionContainment` | Run. My invented rules and attack list, with the model's obedience assumed |
| L2, L3 | `SecurityLabTest` | Run on real `AgentService` and H2 ledger, with a **scripted** model |
| L3 | `OrderBoundRefundPolicy` | Run. In-memory sketch. Doesn't release a reservation if a refund later fails |
| L3 | `ToolGuards` | Run. Fake DNS table. Path check run against a real Windows junction, and symlinks not tested, as this account can't create them |
| L4 | `DataGuards` | Run, on invented data. Regex sketch, unkeyed hash chain |
| L5 | `GuardrailMath` | Run. All rates are illustrative inputs |
| L5 | Harmlessness screen, classifiers | **Not implemented.** An honest test needs a real model |

## What has never happened

- **No real model has been attacked or called.** Every "compromised model" is a script. These labs show what code enforces, and measure nothing about how often a model is fooled.
- No real MCP OAuth flow, sandbox escape test, or Claude Code permission test ran for this module. Claude Code facts come from its docs.
- No guardrail product was evaluated.
- No regulation was read, and nothing here is legal advice.

## Findings, with their limits

| Finding | Limit |
| --- | --- |
| AI08's `RefundPolicy` paid another customer's order, took a negative amount, and paid twice with new IDs | Reproduced by `SecurityLabTest`. The original class is left unchanged, and that test documents it |
| A non-numeric amount crashed the policy, and the model was told it could retry | Same test. The ledger row also stays at STARTED |
| `ToolLedger` returns a stored result to another case that reuses an ID | Reached with a scripted ID. I believe the API generates IDs, and I haven't tested this against the real API. Exercise 3 fixes it |
| A text-only path check passes a link that leads outside the workspace | Tested with a junction |
| Allowlisted commands can read private keys | By construction of a text check. Shows why a sandbox is needed |
| A destination allowlist doesn't protect a refund tool | My rule set |

## Defects in my own labs, found before publishing

| Defect | How found |
| --- | --- |
| `checkUrl` returned ALLOW for a host that didn't resolve, so it failed open | Reading the output |
| `checkCommand` allowed a lone `&` | Adding a test row |
| The figure check used substring matching, so 1.30 passed against 41.30 | Rereading the code |
| A retry table used 14% and 1.4% as per-attempt rates, while Anthropic's figures already combine 100 attempts per environment | Rereading the source notes. Now uses plainly illustrative rates |
| L2 first said "a factor of seven or more." The ratios are 5.0 to 7.3 | `verify_ai09.py` |

## Sources read

Anthropic and MCP pages were read on 2026-11-05. Outlines were produced by a fetch tool, and I quote only text it marked as quoted, except where noted as read in full.

| Source | Read how | Used for |
| --- | --- | --- |
| [Mitigating Prompt Injections in Browser Use](https://www.anthropic.com/research/prompt-injection-defenses), Anthropic, Nov 2025 | Outline | L1, L2: definition, rates, layers |
| [Beyond permission prompts: Claude Code sandboxing](https://www.anthropic.com/engineering/claude-code-sandboxing), Anthropic, Oct 2025 | Outline | L3 |
| [Constitutional Classifiers](https://www.anthropic.com/research/constitutional-classifiers), Anthropic, Feb 2025 | Outline | L1, L5 |
| [Many-shot jailbreaking](https://www.anthropic.com/research/many-shot-jailbreaking), Anthropic | Outline | L5 |
| [Mitigate jailbreaks and prompt injections](https://platform.claude.com/docs/en/test-and-evaluate/strengthen-guardrails/mitigate-jailbreaks) | In full | L1, L2, L5 |
| [Claude Code security](https://code.claude.com/docs/en/security) | In full | L1, L3 |
| [Claude Code permissions](https://code.claude.com/docs/en/permissions) | Sections on rule order, Bash limits, hooks, sandboxing and managed settings | L3 |
| [MCP security best practices, 2026-07-28](https://modelcontextprotocol.io/specification/2026-07-28/basic/security_best_practices) | In full. **The 2025-11-25 page, which the Java SDK's version uses, wasn't read** | L3 |
| [API and data retention](https://platform.claude.com/docs/en/manage-claude/api-and-data-retention) | Sections on approach, ZDR scope, Covered Models, eligibility and retention regardless | L4 |
| [Prompt caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching) | Isolation passages, of the first 100,000 characters | L4 |
| [OWASP Top 10 for LLM Applications 2025](https://genai.owasp.org/llm-top-10/) | **List page only.** Glosses and mappings are mine | L1, L5 |
| [The lethal trifecta](https://simonwillison.net/2025/Jun/16/the-lethal-trifecta/), Simon Willison, Jun 2025 | Outline | L1, L2 |
| [Defeating Prompt Injections by Design (CaMeL)](https://arxiv.org/abs/2503.18813) | **Abstract only** | L2 |
| [Design Patterns for Securing LLM Agents](https://arxiv.org/abs/2506.08837) | **Abstract only.** The three patterns in L2 are my simplification, and not the paper's definitions | L2 |

## Not verified

- Per-model rates beyond the models in Anthropic's table. No newer figures were sought.
- Whether Slack unfurls links in a way that exfiltrates data (Exercise 2 says to verify).
- Whether embeddings can be reversed to their source text. L4 gives a cautious recommendation only.
- The isolation levels table for code execution is general practice, and it's labelled as a recommendation.
