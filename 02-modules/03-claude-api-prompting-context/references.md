# AI03: References and Verification

Numbers are recomputed by `scripts/verify_ai03.py`, or come from running `code/ai03-claude/`. Last checked 2026-11-04.

## Code status

Everything compiles against `anthropic-java` 3.2.0 with JDK 21 and Maven. Lines are ≤ 92 characters.

| Lesson | Class | Status |
| --- | --- | --- |
| L1 | `SupportChat`, `ApiCalls` | **Compiled, not run.** They call the API |
| L2 | `PromptBuilder` | Run, and output quoted exactly |
| L3 | `Extraction` | Run, and output quoted exactly. **Model answers are scripted**, so this proves the loop's logic, not Claude's behavior |
| L3 | `ExtractionClient` | **Compiled, not run** |
| L4 | `ModelRequests` | **Compiled, not run** |
| L5 | `CacheEconomics` | Run, and output quoted exactly |
| L5 | `BatchJobs` | **Compiled, not run** |
| L6 | `ContextAssembler` | Run, and output quoted exactly. Token numbers use a rough character estimate on invented data |

So **no request has actually reached the Claude API in this course yet.** Setting `ANTHROPIC_API_KEY` would allow a real test.

## Hand-derived figures

| Claim | Recomputed | Verdict |
| --- | --- | --- |
| L2: hook table totals | 40 reported, 10 useful, 25% | ✓ Illustrative |
| L2: after disabling | 10 reported, 7 useful, 70% | ✓ |
| L5: hook costs | $0.20 per request, $200 per hour, $72,000 | ✓ |
| L5 Q1 | gap ≤ 4 hours | ✓ |
| L5 Q4 | $20.00, $1.48, 92.6% saved | ✓ |
| L6 Q5 | 23,400 | ✓ |
| Exercises 1, 4, 5 | All match the script | ✓ Exercise 4(a) was **improved**: the 256 MB limit decides, not the request count |

## Sources read

| Claim | Source | How it was checked |
| --- | --- | --- |
| Clear and direct, colleague test, motivation, 3–5 examples, XML, long context, up to 30% | [Prompting best practices](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/claude-prompting-best-practices) | Those sections read |
| Overtriggering on older anti-laziness prompts | Same page | Search matched, and relevant lines read |
| Context vs prompt engineering, attention budget, altitude, compaction, notes, subagents | [Effective context engineering](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents) | Summarized through a fetch tool, quotes checked in that output |
| Multipliers, TTLs, breakpoints, minimums, invalidation order, usage fields | [Prompt caching docs](https://platform.claude.com/docs/en/build-with-claude/prompt-caching) | First 100,000 characters read through a fetch tool |
| Batch limits, 29 days, any order, result types, billing, stacking, best-effort hits | [Batch processing docs](https://platform.claude.com/docs/en/build-with-claude/batch-processing) | Relevant lines read |
| Schema features and limits, compile cache, ordering, refusal behavior | [Structured outputs docs](https://platform.claude.com/docs/en/build-with-claude/structured-outputs) | Read through a fetch tool. The incompatibility list wasn't visible |
| All TS statements | Official exam guide | Read in full |

## From the API reference bundled with Claude Code (`claude-api` skill, cached 2026-10-06)

**Version-sensitive. Check before relying on these.**

- Per-model thinking and effort rules, including the L4 table.
- Rejected parameters on Opus 5.5.
- Forced `tool_choice` returning 400 on Opus 5.5, Sonnet 5.5, Fable 5.1 and Mythos 5.1, with its alternatives.
- Error codes, SDK retry behavior, and Java exception classes.
- Beta features: message threads, compaction, context editing, task budgets, refusal fallbacks, fast mode.
- Silent cache invalidators, and the advice to measure lower effort before building a cascade.
- Citations being incompatible with `output_config.format`.

## Differences between the exam and current products

| Topic | Exam guide | Current behavior | Taught as |
| --- | --- | --- | --- |
| Forced `tool_choice` | Reliable way to guarantee output | 400 on the newest models | Both, clearly labeled |
| Few-shot count | 2–4 targeted | Anthropic suggests 3–5 | Both. Exam answer: 2–4 |
| Batch duration | "Up to 24 hours, no latency SLA" | Same, and docs add that most finish within 1 hour | Both. Design around the guarantee |
| Structured output | Tool use is the most reliable | `output_config.format` also exists | Both |
| SLA example | 4-hour windows for 30 hours | Formula gives a 6-hour maximum | Explains the margin |

## Stated from general knowledge

Jakarta Bean Validation as Pydantic's Java equivalent. RTF and CO-STAR as prompt frameworks, seen in your `Prompt engineering .pdf`. Session locking and optimistic versioning, referenced to Java 15.

## Engineering recommendations, not facts

Every production architecture in this module, and the migration checklist. **None describes a specific company's system.** Effort's "starting use" table follows Anthropic's reference, and it's a starting point. The hook table in L2 and the dismissal table in L3 are illustrative.

## Playlist

Videos 15–19 map to this module. I used **only keyword search on their transcripts**, and didn't read them in full. The hints I sampled agree with the guide on `custom_id`, and on escalation anti-patterns. So the videos remain useful viewing, and this module doesn't summarize them.

## Topic check against your PDFs

`Prompt engineering .pdf` thumbnails were used as a checklist only. Its zero-shot, one-shot, few-shot, chain of thought, self-consistency, role, delimiters, structured output and framework topics are covered in L2 and L3. Not every template it lists (career, email and so on) is relevant here. No facts were taken from it.
