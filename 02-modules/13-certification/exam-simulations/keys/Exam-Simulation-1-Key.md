# Exam Simulation 1: Answer Key

Don't read this until you've finished the simulation.

## Scenario 1: Customer Support Resolution Agent

**1. D** (TS 2.2, H). An access failure and a valid empty result need different handling, so the tool must distinguish them. Retrying wastes calls on real empties, treating every empty as an error causes needless escalation, and prompting can't reveal which one occurred.

**2. A** (TS 5.2, H). Sentiment isn't a proxy for complexity, and this case is within the agent's capability, so acknowledge it and offer to resolve it. An explicit request for a human, including a reiterated one, must be honored.

**3. D** (TS 1.4, M). Humans can't see the transcript, so the summary must stand alone and be actionable. A link assumes access they lack, a raw dump is slow to use, and scores don't help them resolve the case.

**4. C** (TS 2.2, M). Uniform errors prevent appropriate recovery. Structured metadata separates a transient failure from a business rule. A retry limit still can't tell them apart, codes alone leave interpretation to the model, and retrying policy refusals is wasted work.

**5. D and E** (TS 1.5, H). Rules that must always hold, particularly financial and identity rules, need deterministic enforcement. Tone, suggestions and length are judgment calls, where an occasional miss is acceptable, so prompt guidance fits them.

**6. A** (TS 1.5, M). The call has to be stopped before it runs, deterministically, and redirected. `PostToolUse` runs after the refund has happened, descriptions and examples are probabilistic, and a nightly job only acts after the money has moved.

**7. B** (TS 2.3, M). Too many tools increase decision complexity, and scoping restores reliability. Prompting doesn't reduce the choices, uniform length isn't a principle, and forcing tools by keyword removes the model's judgment.

**8. A** (TS 1.4, H). The rule must always hold and involves money, so it belongs in the same programmatic gate, based on real tool results. Prompt rules and examples are probabilistic, and repeating a number doesn't establish ownership.

**9. A** (TS 1.5, H). Normalizing results in code before reasoning is deterministic. A prompt table leaves conversion to the model, the backends can't honor a requested format, and a subagent is disproportionate for a mechanical conversion.

**10. C** (TS 2.1, H). Keyword-sensitive prompt wording can override good descriptions, and this instruction fits the symptom exactly. Nothing suggests a length or ordering effect, and a classifier is over-engineering before you've fixed the wording.

**11. B** (TS 1.1, H). The anti-pattern is an arbitrary cap as the primary stop. A higher number keeps that flaw. A cap remains sensible as a guard against runaway loops, so removing every limit goes too far, and separate sessions lose shared context.

**12. C** (TS 1.4, M). Multi-concern requests are decomposed, investigated with shared context and synthesized. Separate sessions cause the contradictions you're seeing, deferring leaves issues unresolved, and several concerns alone don't justify escalation.

**13. C** (TS 5.1, M). Transactional facts should live outside summarized history, in a block included in every prompt. More frequent summaries still compress them, an instruction is probabilistic, and asking customers to repeat themselves harms the experience.

**14. A** (TS 1.1, M). The loop must follow `stop_reason`. A response can hold text and `tool_use` together, so text says nothing about completion. Prompting Claude to stop writing text is probabilistic, phrase lists parse natural language, and a confirmation call adds cost without fixing the control flow.

**15. A** (TS 2.4, M). Environment variable expansion shares the configuration while keeping secrets out of version control. A private repo still commits the secret, per-developer setup loses the shared config, and Base64 is encoding rather than protection.

## Scenario 2: Multi-Agent Research System

**16. B** (TS 1.2, H). Partitioning scope prevents overlap at its source, and that's the coordinator's job. Deduplication treats a symptom without adding coverage, more identical agents add more duplicates, and direct messaging breaks hub-and-spoke.

**17. B** (TS 1.3, H). Goals and quality criteria let subagents adapt, while procedures break when reality differs. More steps and forced tools increase rigidity, and moving the steps doesn't change the problem.

**18. D** (TS 2.2, H). Subagents should recover locally from transient errors, and propagate only what they can't resolve, with context. Infinite retries hang, hiding the failure conceals a gap, and terminating discards good work.

**19. D** (TS 2.1, H). Overlapping descriptions are the root cause, and differentiating them is the proportionate first step. A routing layer is over-engineered, merging creates a vaguer tool, and thinking harder can't resolve descriptions that give no way to tell them apart.

**20. A and E** (TS 1.3, H). Attribution survives only if it's structured and explicitly carried forward. A bibliography can't link a claim to a source, confidence doesn't locate a source, and subagents can't read another agent's history.

**21. C** (TS 1.3, M). Multiple Task calls in one response run in parallel. Separate turns are sequential. There's no such option, separate coordinators lose central control, and one combined task invites attention dilution.

**22. C** (TS 2.3, M). A constrained tool limits what's possible, while a prompt is only guidance. Logging detects misuse afterward, and summarizing pages doesn't stop the wandering.

**23. B** (TS 1.2, H). Hub-and-spoke gives observability, consistent error handling and controlled information flow. Shared memory and a subscriber aren't part of the pattern and isolated contexts don't work that way, and uniform logs leave uncontrolled routing in place.

**24. D** (TS 1.3, M). Subagents receive only what's explicitly passed, so complete findings with sources must be in the prompt. Adding search invites misuse, formatting detail can't supply missing content, and there's no shared memory to expire.

**25. A** (TS 5.6, H). Conflicts are annotated with attribution, and dates prevent temporal differences being mistaken for contradictions. Averaging and silently choosing lose information, and dropping both loses useful findings.

**26. B** (TS 1.6, M). Open-ended investigation suits dynamic decomposition, because the next step depends on the findings. A longer fixed plan is still fixed, one large pass dilutes attention, and review doesn't change what's researched.

**27. A** (TS 2.3, H). A scoped cross-role tool serves the high-frequency need while complex cases go through the coordinator. Full search invites misuse, deferring checks undermines drafting that depends on them, and pre-collecting everything isn't feasible.

**28. D** (TS 2.4, M). Resources give visibility into available data without exploratory calls. A prompt doesn't supply the missing information, higher caps waste more, and one tool per report degrades tool selection.

**29. A** (TS 1.2, M). The coordinator should select subagents dynamically. Removing search breaks other requests, a routing subagent duplicates the coordinator's job, and subagents lack the overall view needed to decide.

**30. B** (TS 2.1, M). Generic tools should be split into purpose-specific tools with defined input and output contracts. A free-text mode keeps the ambiguity, output detection treats a symptom, and an instruction is only guidance.

## Scenario 3: Code Generation with Claude Code

**31. C** (TS 5.4, H). Referring to typical patterns is a sign of context degradation, and scratchpad files persist key findings across context boundaries. Asking for attention doesn't restore lost detail, starting blank repeats work, and output limits aren't the problem.

**32. C** (TS 3.3, H). Glob-scoped rules apply conventions by file type wherever files live, and load only when needed. The root file loads everywhere, a skill relies on memory, and user-level rules aren't shared.

**33. D** (TS 3.2, M). Personal variants with different names avoid affecting teammates. Local edits to a shared file are fragile, and the other two options place personal material in shared files.

**34. C** (TS 3.5, M). When prose is interpreted inconsistently, concrete input and output examples work best. More prose or repetition keeps the ambiguity, and other projects may use different formats.

**35. D** (TS 3.2, M). The guide teaches `allowed-tools` for restricting tools during a skill. A fork isolates context rather than capabilities. *Build today: current docs describe `allowed-tools` as pre-approval, so use deny rules or hooks to really block a tool.*

**36. B** (TS 3.1, H). Selective `@import` keeps things modular and leaves the choice with the maintainers. The root file loads everything, a generator adds machinery, and user-level files aren't shared.

**37. A** (TS 3.1, H). `/memory` shows which memory files are actually loaded. Asking the model isn't reliable, Git shows what's in the repo rather than what's loaded in a session, and reinstalling skips the diagnosis.

**38. D** (TS 3.5, H). Interacting problems should go in a single detailed message, whereas independent ones can go one at a time. New sessions lose the connection, continuing keeps regressing, and disabling tests hides the problem.

**39. A and E** (TS 3.4, H). Plan mode suits large-scale changes, several valid approaches and architectural decisions. Well-scoped changes with a clear cause suit direct execution.

**40. A** (TS 5.4, H). Summarizing before spawning passes what's needed without the noise. Subagents can't read the main session's history, a transcript wastes their context, and staying in the main session worsens the crowding.

**41. D** (TS 3.4, M). The Explore subagent isolates verbose discovery and returns a summary. Skipping exploration isn't safe for a large refactor, rereading uses more context, and a system prompt still occupies context.

**42. C** (TS 3.2, H). Always-applicable standards go in always-loaded memory, and occasional task-specific workflows go in on-demand skills. How often it applies decides this, not its importance or length.

**43. C** (TS 3.2, H). `context: fork` runs the skill in isolation, returning the result without the verbose working. `argument-hint` prompts for parameters, `allowed-tools` limits tools rather than output, and `CLAUDE.md` would load it all the time.

**44. D** (TS 5.4, M). `/compact` reduces context use during extended sessions. `/memory` shows and edits memory files, `--resume` restores the session rather than reducing it, and `context: fork` is skill frontmatter.

**45. B** (TS 3.1, M). User-level instructions aren't shared. It's a classic hierarchy issue, fixed by moving the instruction to the project level. The other explanations describe behavior that doesn't exist.

## Scenario 4: Structured Data Extraction

**46. C** (TS 4.2, H). Few-shot examples of varied formats address empty extraction when the information is present. Making them optional hides the miss, a stronger instruction may encourage fabrication, and an identical retry gives no new guidance.

**47. B** (TS 5.5, H). Confidence is useful for routing once it's calibrated with labeled data at field level, and contradictory sources also need people. Uncalibrated scores aren't reliable, random routing wastes scarce capacity, and size doesn't indicate error.

**48. A** (TS 4.5, H). Sample refinement maximizes first-pass success and reduces resubmission costs. Fixing afterward wastes a full run, the largest documents aren't representative, and batches do support this workload.

**49. D** (TS 4.5, M). `custom_id` correlates results, so you can resubmit only what failed, and with a fix for the cause. Rerunning everything wastes money, unchanged oversized documents fail again, and manual handling isn't needed.

**50. B** (TS 4.4, H). Retry-with-error-feedback includes the document, the failed output and the specific error. The API is stateless, so it needs the content again. Without the error it may repeat the mistake, and general instructions don't say what's wrong.

**51. D** (TS 4.2, M). A small number of targeted examples showing reasoning lets the model generalize to new cases. Many examples add overhead, clear-cut cases don't teach ambiguity, and removing the reasoning removes what generalizes.

**52. C** (TS 5.5, H). Aggregate accuracy can mask poor performance on specific types or fields, such as a small share of receipts. Validate by segment first. Refusing all automation goes too far, and uncalibrated self-reports aren't evidence.

**53. C** (TS 4.4, H). Retries work for format and structural errors, not for absent information. More attempts and examples can't create what's not there, and relaxing the validator invites fabrication. Provide the agreement, or record the value as missing.

**54. A** (TS 4.4, M). Capturing both values and a flag surfaces discrepancies for handling. Keeping only one value hides the conflict, and confidence doesn't tell you what's inconsistent.

**55. D** (TS 4.3, M). Schemas guarantee shape, not meaning. Sums and field placement need validation logic. Adding constraints or forcing the tool doesn't address semantic correctness, and free text would reintroduce syntax errors.

**56. C and D** (TS 4.3, M). `"other"` plus detail keeps the schema extensible, and `"unclear"` gives ambiguity an honest place to go. Free text loses consistency, enum additions never catch up, and required doesn't help.

**57. B** (TS 4.5, H). A document can wait for one full window, then up to 24 hours of processing, so the window can't exceed 6 hours (30 − 24). Only 4 hours fits, and it also leaves a margin.

**58. B** (TS 4.3, H). Required fields pressure the model to fabricate a value when the source lacks it. A format check can pass on a fabricated number, an estimate is still invented, and these examples don't show what to do when it's absent.

**59. B** (TS 5.5, M). Stratified sampling measures errors in high-confidence output and can reveal novel patterns. The other options never look at unreviewed output, or only after harm has occurred.

**60. A** (TS 4.3, H). `any` guarantees a tool call while letting the model choose the matching schema. `auto` may return text, forcing one tool mislabels other types, and an all-optional union schema loses structure. *Build today: forced modes are rejected on some current models, so check the docs.*
