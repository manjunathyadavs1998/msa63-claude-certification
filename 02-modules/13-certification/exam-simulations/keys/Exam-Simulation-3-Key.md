# Exam Simulation 3: Answer Key

Don't read this until you've finished the simulation.

## Scenario 1: Multi-Agent Research System

**1. A and B** (TS 1.3, H). Subagents have isolated context, get what's in their prompt and return a final result. They don't see the parent's results, share memory or keep memory between invocations. *Build today: a fork subagent is a separate option that copies the conversation.*

**2. C** (TS 1.3, H). Subagents don't share history, so the sources must be included in the prompt. Forcing a tool can't supply missing context, there's no minimum length, and `--resume` continues a session rather than passing context.

**3. C** (TS 1.2, H). Subagents did what they were asked, and the logs show the missing areas were never assigned. Downstream changes treat a symptom. Synthesis shouldn't add unresearched content, and a section count doesn't ensure coverage.

**4. C** (TS 2.4, H). Project scope shares the server, and environment variable expansion keeps secrets out of version control. User scope isn't required, committing keys is unsafe, and `CLAUDE.md` is committed text that doesn't configure servers.

**5. B** (TS 1.2, H). The loop evaluates gaps, targets more research and resynthesizes. A full restart repeats the same narrow work, accepting leaves the gap, and general knowledge loses citations.

**6. A** (TS 1.1, M). Natural language isn't a reliable control signal, in either direction. `stop_reason` is the explicit signal. A count ignores what the task needs, and a required phrase is still probabilistic.

**7. B** (TS 2.3, M). Agents misuse tools outside their specialization, so scoping fixes this. A prompt rule leaves the tool available, forcing every turn removes flexibility, and more iterations allow more of the wrong work.

**8. A** (TS 1.5, M). Deterministic normalization in code before reasoning is the pattern. External sources can't honor a requested format, prompts leave it to the model, and a subagent is disproportionate. Vague dates should be flagged rather than invented.

**9. B** (TS 5.3, H). Returning empty results as success hides the gap and leads to a false conclusion. Softer wording doesn't reveal the failure, and terminating the entire workflow on one failure is also an anti-pattern.

**10. A and C** (TS 2.2, H). Access failures and valid empty results are different, and need different handling. Treating them alike either hides failures or wastes effort, and retrying a valid empty result is pointless.

**11. C** (TS 5.6, H). Analysis should include conflicting values with attribution, and let the coordinator decide. Silently resolving or averaging distorts the evidence, and stopping loses the rest of the analysis.

**12. B** (TS 1.2, M). Refinement continues until coverage is sufficient. A subagent's preference isn't the criterion, a fixed count ignores quality, and the first draft hasn't been evaluated. A maximum round count remains useful as a safety limit.

**13. A** (TS 1.3, M). Structure keeps content and metadata distinct. It doesn't verify truth, the model still reads it, and prompts accept plain text too.

**14. A** (TS 1.4, H). A guaranteed requirement needs programmatic enforcement, based on a recorded result. Prompt rules are probabilistic, a disclaimer doesn't meet the requirement, and weekly review comes after publication.

**15. D** (TS 1.6, H). Separate local passes avoid attention dilution, and a cross-document pass handles connections. A bigger model and a request for attention don't change the structure, and sampling misses evidence.

## Scenario 2: Developer Productivity with Claude

**16. A** (TS 1.3, H). Context must be explicit, and should be focused on what each subagent needs. There's no inheritance, or history query, and a raw transcript wastes context.

**17. A** (TS 2.5, H). Focused searches limit what enters context. A file read back consumes the same space, Glob doesn't search contents, and skimming doesn't change what's in context.

**18. C and E** (TS 2.4, H). Resources give agents visibility into what's available, so that they can fetch only what's needed. A prompt can't supply the missing information, one tool per table degrades selection, and higher caps don't reduce calls.

**19. A** (TS 2.2, H). Retrying won't fix an access failure, and the caller must know that it didn't succeed. The category should match the cause, and consider what details to expose. Empty success hides the failure.

**20. D** (TS 2.5, M). Glob finds files by name or extension pattern. Grep searches contents, Read loads a file, and Edit modifies one.

**21. D** (TS 2.3, M). `any` requires a tool and lets the model choose. `auto` allows text, a named tool chooses for the model, and the last isn't a guide option. *Build today: forced modes are rejected on some current models, so check the docs.*

**22. C** (TS 2.3, H). Scoping limits what's possible. A prompt is only guidance, reverting acts after the fact, and passing everything down is the opposite of scoping.

**23. D** (TS 1.5, H). A hook is code, so test it and make unexpected input visible. A monthly sample is slow, removing the hook brings back the problem, and the model may not notice anything is wrong.

**24. D** (TS 1.1, H). The API doesn't keep the conversation for you here, so history must be sent each time. Without it, the model has no record of what it did. The other options describe behavior that doesn't exist. Trimming results reduces tokens safely.

**25. B** (TS 3.2, H). Isolation keeps exploratory content from influencing later work. `argument-hint` prompts for parameters, "forget" isn't reliable, and `CLAUDE.md` makes it worse.

**26. B** (TS 2.1, H). Tools from all servers sit together, so selection depends on descriptions. Scope doesn't set priority, a blanket note is wrong for many queries, and an agent can use several servers.

**27. B** (TS 1.7, H). The validity of the context drives the choice. A name only helps you find a session, and token use and model version don't tell you whether the results are still correct.

**28. B** (TS 1.7, M). Named resumption continues a specific prior conversation. The other options don't restore a conversation.

**29. C** (TS 1.1, M). Every `tool_use` needs a matching `tool_result`, and they go back together. Skipping one breaks the exchange, a call in between leaves a result missing, and results don't belong in the system prompt.

**30. D** (TS 2.5, M). Incremental exploration builds understanding while conserving context. Reading everything exhausts it, file size isn't a guide, and general knowledge isn't this code.

## Scenario 3: Code Generation with Claude Code

**31. B** (TS 5.4, M). Subagents isolate verbose exploration, while the main agent coordinates. Reading the file brings the output back, ignored text still occupies context, and output limits don't add context.

**32. A** (TS 5.3, M). Propagate unresolved errors with what was attempted and partial results, so the main session can decide. Hiding the gap misleads, and discarding results wastes work.

**33. B** (TS 5.1, H). Upstream agents should return structured data when downstream context is limited. Repeating adds to the load, a one-liner loses detail, and shorter sentences leave the volume in place.

**34. C** (TS 3.5, H). Specific input and output pins down the behavior. General requests don't say what's wrong, and a `CLAUDE.md` note is too general for this bug.

**35. C** (TS 3.2, H). `argument-hint` prompts for required parameters. The others control context, tools or file matching.

**36. A and D** (TS 3.1, H). Those two are true. Several files can load, user files affect only that user, and project files are shared.

**37. B** (TS 3.3, M). Glob-scoped rules apply by file type regardless of location, and load only when relevant. Copies drift, the root file loads everywhere, and a manual command relies on memory.

**38. C** (TS 5.1, M). Progressive summarization loses specific values, so keep them in a persistent structured location. The other explanations don't match how summaries work.

**39. B** (TS 3.4, H). Plan first for architectural decisions and several valid approaches, then execute. Starting direct risks costly rework, and plan mode isn't where the implementation stays. A long prompt skips exploration.

**40. A** (TS 3.5, M). The interview pattern surfaces considerations such as key design and failure modes before implementation.

**41. D** (TS 3.2, M). Project-scoped commands are shared through version control. The home directory is personal, `CLAUDE.md` isn't a command definition, and no such config file exists.

**42. D** (TS 3.1, M). `.claude/rules/` organizes topics. User-level files aren't shared, copies drift, and deleting loses standards.

**43. D** (TS 5.6, M). Attribution is lost in summarization unless claim-source mappings are carried forward. A general list doesn't link claims to sources.

**44. A** (TS 5.4, H). Structured state exports and a manifest let the coordinator resume and inject state into agent prompts. Conversation state may be lost or stale, restarting wastes work, and free-form logs aren't reliably usable.

**45. C and D** (TS 5.4, H). Those are signs of context degradation, and scratchpads and subagents counter it. Asking it to remember doesn't restore detail, rereading everything crowds context further, and `max_tokens` limits only the reply.

## Scenario 4: Claude Code for Continuous Integration

**46. D** (TS 3.6, M). `-p` prevents an input hang, and JSON with a schema gives machine-parseable output. The second is interactive, the third resumes a session, and the last uses options that aren't real.

**47. B** (TS 4.5, H). Latency-tolerant work suits batches, with those limits. There's no one-hour guarantee, and batches don't support executing tools mid-request.

**48. D** (TS 4.6, M). Self-reported confidence can support calibrated review routing, but it isn't proof, and raw thresholds aren't reliable on their own.

**49. D** (TS 4.3, M). Required fields can pressure the model to fabricate. Nullable fields let it report that there's no clear fix.

**50. A** (TS 4.2, H). Few-shot examples are the most effective way to get consistently formatted, actionable output when instructions alone fall short.

**51. D** (TS 3.6, H). Non-interactive input fits. The other options pollute the repo, assume memory across jobs, or need a person.

**52. B** (TS 4.4, H). Tracking which constructs trigger findings enables systematic analysis of dismissal patterns. A model can't report what developers later did, hiding by confidence isn't analysis, and free text is hard to aggregate.

**53. A and D** (TS 4.6, H). Independent instances and multi-pass structure work well. A generating session keeps its own reasoning, so self-review and extended thinking are less effective, and requiring consensus suppresses real findings.

**54. C** (TS 4.4, M). Schemas prevent syntax errors, not semantic ones. Checking the value needs code, and specific error feedback guides correction.

**55. C** (TS 4.1, H). Specific categorical criteria beat general or confidence-based instructions, which don't define what should be reported.

**56. A** (TS 4.5, M). Batches have no latency guarantee, so blocking checks should stay synchronous. Nightly and weekly jobs can use them. Batches can return structured results.

**57. A** (TS 4.2, H). Examples with reasoning distinguish acceptable patterns from real issues, and allow generalization. A file list doesn't generalize, and suppression would hide real bugs.

**58. A** (TS 4.1, M). Explicit criteria with code examples give consistent classification. Asking for more thought doesn't define the levels, and the others avoid the problem.

**59. C** (TS 3.2, H). On-demand, task-specific workflows suit skills, and a checked-in one is versioned and shared. `CLAUDE.md` loads it everywhere, a runner's home isn't versioned, and PR descriptions aren't a reliable source.

**60. C** (TS 4.6, H). Per-file passes can't see contracts that span files, so an integration pass is needed. The other options stay within single-file views or skip files.
