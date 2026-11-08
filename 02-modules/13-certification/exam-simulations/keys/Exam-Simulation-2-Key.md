# Exam Simulation 2: Answer Key

Don't read this until you've finished the simulation.

## Scenario 1: Customer Support Resolution Agent

**1. C** (TS 2.3, M). Forced selection guarantees a specific first call, and later turns can then adapt. `any` doesn't fix which tool, forcing on every turn traps the agent, and `auto` is only a preference. *Build today: forced modes are rejected on some current models, so a code gate is often the better route.*

**2. A** (TS 2.4, M). User scope is for personal and experimental servers, while project scope reaches everyone who pulls the repo. `CLAUDE.md` and rules hold instructions, not MCP server configuration.

**3. B** (TS 5.2, H). Policy gaps are an escalation trigger, even when a case seems simple. The agent shouldn't invent policy in either direction, and self-reported confidence is an unreliable basis for it.

**4. B** (TS 1.1, H). Tool results reach the model only through the conversation history. The model has no connection to your systems, `stop_reason` doesn't report what your code dropped, and more iterations can't restore lost data.

**5. B** (TS 5.2, M). Multiple matches call for clarification rather than heuristic selection. A different heuristic has the same flaw, showing others' details is a privacy problem, and escalation is unnecessary when one question resolves it.

**6. D** (TS 1.4, H). Handle what's possible and hand off the rest clearly. Escalating everything wastes capacity and the customer's time, pressing on risks harm, and closing abandons an open issue.

**7. D** (TS 1.1, M). Model-driven decisions suit ambiguity, while trees suit fixed paths. Code can call tools from a tree, a loop doesn't always use fewer tokens, and a loop guarantees no rule, so hard rules still need code.

**8. D** (TS 1.5, H). Only the hook is deterministic, while the prompt reduces wasted attempts and helps communication. The API doesn't know about hooks in your application, and a prompt rule isn't a guarantee.

**9. D and E** (TS 1.4, H). Humans who lack the transcript need the customer's details, root cause and a recommended action. Tool lists, full reasoning and token counts don't help them resolve the case.

**10. B** (TS 2.2, H). Business rule violations are non-retryable, and a friendly explanation lets the agent communicate appropriately. Retrying wastes calls, a fake success misleads, and stack traces aren't useful or safe for this purpose.

**11. B** (TS 1.4, H). Customer text can influence a prompt rule, but it can't change what code checks. A warning lowers the probability without removing it, sentiment analysis measures the wrong thing, and review happens after the fact.

**12. D** (TS 1.4, M). A gate protects only the tools it covers, so a new money-moving tool could bypass it. Description length enforces nothing, `stop_reason` doesn't work that way, and telling customers doesn't protect anything.

**13. D** (TS 1.5, M). Blocking with clear redirection lets the agent continue correctly. A fake success misleads, silence stalls, and silently changing the amount makes a decision nobody approved.

**14. A** (TS 2.1, M). Descriptions are the main selection signal, so they need inputs, examples, edge cases and boundaries. The others add claims or irrelevant details, and don't help distinguish the tools.

**15. C** (TS 1.7, H). When prior context is mostly valid, resume and inform. Claude doesn't detect changes by itself, starting over discards valid work, and forks carry the same stale results.

## Scenario 2: Developer Productivity with Claude

**16. B** (TS 3.4, M). Plan mode is about the size and uncertainty of a change, not its urgency. A clear, single-location fix suits direct execution.

**17. D** (TS 2.4, H). Agents fall back to built-in tools when MCP descriptions don't explain what they offer. Removing Grep breaks code search, a keyword router is over-engineered, and scope sets who gets a server rather than a priority.

**18. C** (TS 1.7, H). Forks give independent conversation branches from one baseline, but the working directory is shared unless you isolate it. Alternating mixes them, blank sessions lose the analysis, and resuming continues one history.

**19. D** (TS 2.5, M). Glob matches file names and paths, while Grep searches file contents. Reading everything wastes context.

**20. C** (TS 1.3, H). The description drives selection, the system prompt sets the role, and tool restrictions limit what it can do. The specific job arrives through the spawning prompt. The other options leave the misuse possible.

**21. A and E** (TS 1.6, H). Dynamic decomposition suits work where the next step depends on findings. Known, repeatable sequences are simpler and more predictable as a prompt chain.

**22. A** (TS 2.5, H). Start with content search to find the entry point, then read and follow it incrementally. Reading everything exhausts context, and following imports from the top is slow and wasteful.

**23. C** (TS 1.5, H). Interception before execution is enforcement, and the others are guidance. *Build today: text matching can be bypassed by other forms of a command, so combine it with deny rules and sandboxing.*

**24. A** (TS 2.5, H). Tracing through wrapper modules means finding each exported name, then searching for each. Reading everything wastes context, a broad pattern floods the output, and estimates aren't evidence.

**25. D** (TS 1.7, M). With mostly stale results, a fresh session with a summary is more reliable. Compaction summarizes without re-reading, forks inherit the stale results, and asking for care doesn't refresh anything.

**26. B** (TS 2.4, H). All configured servers' tools are discovered at connection and available together, which increases decision complexity. The other explanations describe behavior that doesn't exist. *Build today: Claude Code can defer tool definitions with tool search, which reduces this cost.*

**27. A** (TS 2.4, M). Standard integrations should use existing servers, with custom servers reserved for team-specific workflows. Building first adds needless work, and scraping is fragile when there's an API.

**28. B** (TS 1.6, M). Open-ended tasks should start by mapping and prioritizing, then adapt as dependencies appear. Alphabetical or size-based orders ignore importance, and one huge response dilutes attention.

**29. B** (TS 1.3, M). The coordinator needs the Task tool in `allowedTools` to spawn subagents. The others don't give it that ability. *Build today: current docs call this tool `Agent`, with `Task` kept as an alias.*

**30. A** (TS 2.5, H). Edit needs unique anchor text, and Read plus Write is the reliable fallback. In practice, adding surrounding context to make the match unique also works. Grep and Glob don't modify files, and retrying can't make the text unique.

## Scenario 3: Claude Code for Continuous Integration

**31. A** (TS 3.6, H). `CLAUDE.md` in the repo is how CI-invoked Claude Code gets project context. User-level files aren't on the runner, comments don't train future runs, and more output doesn't improve focus.

**32. C** (TS 3.6, H). A session retains its reasoning from generation, making it less likely to question itself. An independent instance sees the code fresh. Self-review instructions and repeats keep the same bias.

**33. C** (TS 4.1, H). Specific categorical criteria beat general instructions such as "be conservative." Confidence-based filtering doesn't define what counts as a problem, and temperature doesn't improve precision.

**34. D** (TS 4.1, H). High false positive categories undermine trust in the accurate ones. Disabling just that category restores trust while you fix it. Disabling everything loses valuable findings, and the other two leave the noise in place.

**35. A** (TS 3.1, M). Project-level memory travels with the code, and it reaches every runner. Copies drift, prompt variables aren't versioned with the code, and that flag isn't real.

**36. C** (TS 3.6, H). Passing prior findings lets Claude avoid duplicates and track what's unresolved. Deleting comments loses the history, reviewing only the last commit misses interactions, and a single review misses later changes.

**37. A** (TS 3.6, M). `-p` (or `--print`) runs Claude Code in non-interactive mode. `CLAUDE_HEADLESS` and `--batch` aren't real options for this, and terminal redirection is a workaround that doesn't address it.

**38. C** (TS 4.6, H). Multi-pass review avoids attention dilution and handles cross-file issues separately. Asking for attention doesn't change the structure, requiring consensus suppresses real findings, and size-based sampling misses bugs.

**39. C** (TS 3.6, M). Claude can't avoid duplicating tests that it can't see. Scheduling and format don't show coverage, and generating more wastes effort.

**40. D** (TS 3.2, M). A checked-in definition is versioned and available to both CI and developers. Personal skills are only on one machine, and a job description or wiki isn't something Claude Code runs.

**41. C** (TS 3.4, H). Many files and several valid designs point to plan mode, followed by execution of the chosen approach. Direct execution risks rework, and the other two don't help you choose an approach.

**42. B** (TS 3.5, H). Test-driven iteration defines requirements up front, and failures guide progress. Tests written afterward can mirror bugs, a self-report isn't evidence, and one test misses edge cases.

**43. B** (TS 3.3, H). Glob-scoped rules cover files by location and pattern, and they load only when relevant. Per-directory copies drift, the root file loads everywhere, and `scope` isn't an option for this.

**44. D** (TS 4.5, H). Batches suit latency-tolerant work, with up to 24 hours of processing and no latency guarantee, so they can't serve a blocking check. The nightly report doesn't need an immediate answer, so it can take the discount.

**45. C and E** (TS 3.6, H). Those two produce machine-parseable output that follows your structure. The other three aren't real options.

## Scenario 4: Structured Data Extraction

**46. A** (TS 4.4, M). A generic retry gives the model nothing to correct, while specific errors guide it. More attempts, added emphasis and token limits don't tell it what's wrong.

**47. C** (TS 5.3, M). Coverage annotations make gaps visible. The other options misrepresent or invent what's known.

**48. A** (TS 4.3, M). With tool use for extraction, the structured data is the tool call's input. You don't need to run anything further. Text blocks aren't the structured output, the API doesn't send you a `tool_result`, and `stop_reason` only says why the model stopped.

**49. C** (TS 4.2, H). Few-shot examples are effective at reducing hallucination with informal formats, and show the desired handling. A stronger instruction is still only prose, a threshold doesn't detect invention, and a required field increases the pressure to invent.

**50. A** (TS 5.1, H). This is the "lost in the middle" effect, which a summary and clear structure help mitigate. Reordering moves the problem, output limits aren't the cause, and asking for attention doesn't change it.

**51. D** (TS 5.3, H). Structured error context lets the coordinator choose how to recover. An empty result hides the failure, a stack trace isn't a recovery decision, and terminating on one failure is an anti-pattern.

**52. A and B** (TS 4.4, H). Retries work when the information is present but was formatted or placed wrongly. They can't recover what's absent from the input or unreadable.

**53. B** (TS 5.5, H). Aggregate accuracy can hide weak segments. Validate by document type and field before reducing review. Stopping everything is an overreaction, and a higher average doesn't necessarily fix one field.

**54. D** (TS 4.5, H). `custom_id` correlates requests and responses. Order and timing aren't reliable, and text isn't a stable identifier.

**55. B** (TS 4.3, M). A schema controls structure, but doesn't by itself specify how to normalize source formats, so include rules in the prompt. The others don't define a consistent format.

**56. B** (TS 4.5, M). A batch request can't run a multi-turn tool loop. Include the needed data up front or use the synchronous API. The others aren't limits of the Batches API.

**57. A** (TS 5.6, M). Dates prevent temporal differences being misread as contradictions. Keeping one value, averaging or guessing all lose or distort the data.

**58. A** (TS 4.3, H). Forcing a named tool ensures a particular extraction runs first, and subsequent steps follow. `any` doesn't fix which tool, `auto` may not call it, and removing tools loses structured enrichment. *Build today: forced modes are rejected on some current models, so check the docs.*

**59. D** (TS 5.1, M). Verbose results consume context out of proportion to their relevance, so trim them first. Removing results outright loses information, ignored fields still occupy context, and more capacity leaves the waste in place.

**60. C** (TS 5.6, H). Different content types should be rendered appropriately, rather than converted to one format. Dropping sections loses content, and attachments don't fix the report.
