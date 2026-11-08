<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Part V, Certification</div>

# Certification, Mock Exam 3

## 60 Items in 120 Minutes

<div class="sub">Scenarios: Code Generation with Claude Code, Multi-Agent Research System, Developer Productivity with Claude, Claude Code for Continuous Integration. Set a timer, work without notes, and mark your answers on paper.</div>

<table class="meta">
<tr><td>Items</td><td>60, in 4 scenario sets of 15</td></tr>
<tr><td>Time</td><td>120 minutes</td></tr>
<tr><td>Domains</td><td>16, 11, 12, 12 and 9 items, rounded from the guide's weights</td></tr>
<tr><td>Status</td><td>Original practice items, written from the guide. Not real or leaked</td></tr>
</table>
</div>

<div class="lesson">

# Before You Start

::: keyidea
==**These are original practice items, not real or leaked exam questions, and the company names are invented. A score here doesn't predict your exam result.**==
:::

Follow the real conditions: one sitting, no notes, flag and return. Where the guide and current products differ, the answers follow the guide, and an explanation may add a *Build today* note. The real exam's wording may be longer and harder, as candidates report.

**Checkpoints:** 28 minutes after Scenario 1, 56 after Scenario 2, 84 after Scenario 3 and 110 after Scenario 4.

**Targets, which are my own and not official:** 80% overall, and no domain under 70%.

</div>

<div class="lesson">

# Scenario: Code Generation with Claude Code

::: key
Larkspur Software's 25 developers use Claude Code across a Spring Boot backend and a TypeScript frontend. They want consistent code generation, reliable refactoring and useful documentation, and they often run long sessions on an old codebase.
:::

::: qa
**Q1.** Which location holds instructions that only one developer receives, and that teammates never get through version control?

A. The repository's root `CLAUDE.md`<br>
B. `.claude/CLAUDE.md` in the repository<br>
C. A `CLAUDE.md` in a subdirectory<br>
D. `~/.claude/CLAUDE.md`
:::

::: qa
**Q2.** The backend and frontend packages have different standards, and the shared standards live in `docs/standards/`. What's the guide's modular way to include only what each one needs?

A. A single user-level file containing the standards for both of the packages<br>
B. A copy of all the standards files pasted into each package's `CLAUDE.md`<br>
C. `@import` of the relevant standards files, in each package's `CLAUDE.md`<br>
D. A slash command that developers must remember to run in each new session
:::

::: qa
**Q3.** Which belongs in a skill, rather than in `CLAUDE.md`?

A. The rule that all public methods need documentation<br>
B. The team's naming convention used in all generated code<br>
C. A step-by-step release-notes workflow used about once a month<br>
D. The project's build and test commands, used every day
:::

::: qa
**Q4.** A brainstorming skill produces many rejected ideas, which then bias later work in the session. What's the best frontmatter change?

A. `context: fork`, so the exploration stays out of the main conversation<br>
B. `paths`, so the skill runs only on files that have brainstorming content<br>
C. `argument-hint`, so developers provide a narrower topic for brainstorming<br>
D. `allowed-tools: Write`, so the skill records its ideas to a file only
:::

::: qa
**Q5.** Why might a `.claude/rules/` file with `paths: ["**/*.test.ts"]` be better than a large `CLAUDE.md` section on tests?

A. It's stored in each user's home directory, so developers can each change it<br>
B. It runs as a hook, so each test is executed automatically after any edit<br>
C. It loads only when matching files are edited, which reduces irrelevant context<br>
D. It's applied by the API, so the model can never fail to follow what it says
:::

::: qa
**Q6.** Which is the best fit for plan mode?

A. Choosing how to restructure a monolith module into services, with several valid approaches<br>
B. Renaming one local variable within a single short function<br>
C. Fixing a typo in a log message whose location is known<br>
D. Adding a date check to a function that's already been identified by a failing test
:::

::: qa
**Q7.** A multi-phase refactor keeps running out of context during discovery, before implementation begins. What's the best approach?

A. Put the full source for each file into `CLAUDE.md` for the rest of the phases<br>
B. Use the Explore subagent for discovery, keeping its summaries in the main session<br>
C. Ask Claude to skip discovery, and work from what's typical for Spring Boot projects<br>
D. Turn off plan mode, so that discovery goes straight into making the changes
:::

::: qa
**Q8.** A developer describes a CSV transformation in prose, and Claude's output differs on each attempt. What should they do next?

A. Switch to a much shorter prose description<br>
B. Ask Claude to explain the prose back to them<br>
C. Repeat the same prose with stronger emphasis<br>
D. Provide 2–3 concrete input and output examples
:::

::: qa
**Q9.** Three independent bugs exist in unrelated modules. How should the developer give feedback, and why?

A. All at once, because a single message always gives the best results<br>
B. All at once, because Claude can only remember one message at a time<br>
C. Sequentially, because fixes for independent issues don't interact<br>
D. Sequentially, because a session can contain just one fix for each file
:::

::: qa
**Q10.** An API-based helper "forgets" the previous question on each turn. What's the most likely cause?

A. The system prompt is limiting the amount that the model can recall<br>
B. The model's memory fills up after each response has been returned<br>
C. `stop_reason` was `end_turn`, which erases the server's stored memory<br>
D. The client isn't sending the complete conversation history in each request
:::

::: qa
**Q11.** A long session's progressive summary changes "retry 3 times, then fail after 30s" into "retry a few times". Which principle does this show, and what prevents it?

A. Summaries lose only customer details, so this technical detail is safe to lose in the session<br>
B. Summaries lose specifics, so keep such values in a persistent facts block outside the summary<br>
C. Long sessions always use a smaller model, so select a larger model at the start of the session<br>
D. Models round all numbers by design, so an instruction not to round is what prevents it
:::

::: qa
**Q12.** A developer asks about a class found an hour ago, and Claude answers with generic advice. What should the agent have been doing during exploration?

A. Avoiding reading any files, so that its context stays as small as possible<br>
B. Using a smaller `max_tokens`, so that answers don't exceed a limit<br>
C. Re-reading every file in the repository before answering each question<br>
D. Maintaining a scratchpad file of key findings, and referring back to it
:::

::: qa
**Q13.** Between two phases of a large investigation, what does the guide recommend?

A. Keep all raw output from the first phase in the main session for later phases<br>
B. Summarize key findings from one phase, and inject them into the next phase's subagents<br>
C. Discard all findings, so that each phase can look at the code without any bias<br>
D. Run the phases in reverse order, so that the later findings are considered first
:::

::: qa
**Q14.** An overnight multi-agent migration analysis crashes. What should recovery rely on, by the guide?

A. A fresh run of the entire analysis, which is always more reliable than any recovery<br>
B. Each agent's exported state in a known location, plus a manifest the coordinator loads on resume<br>
C. The model's memory of its earlier work, which resumes automatically once the process has been restarted<br>
D. The final report file, which holds the complete working state of every agent involved
:::

::: qa
**Q15.** What's the main value of delegating "find all usages of this config key" to a subagent?

A. The subagent can use tools without any restrictions at all<br>
B. The subagent has access to memory from all earlier sessions<br>
C. The main agent's model automatically becomes faster for later work<br>
D. The verbose output stays out of the main agent's context
:::

</div>

<div class="lesson">

# Scenario: Multi-Agent Research System

::: key
Quillstone Research runs a multi-agent system on the Agent SDK. The coordinator delegates to specialists for web search, document analysis, synthesis and report writing. Clients rely on its cited reports for investment decisions.
:::

::: qa
**Q16.** Which value should keep the coordinator's loop running?

A. The response includes some text<br>
B. `stop_reason` is `tool_use`<br>
C. The response is shorter than usual<br>
D. `stop_reason` is `end_turn`
:::

::: qa
**Q17.** What's isolated about a subagent's context in this system?

A. It shares context with its sibling subagents, but not with its coordinator<br>
B. It can't receive anything at all from the coordinator, including findings<br>
C. It doesn't automatically inherit the coordinator's conversation history<br>
D. It must run on a different model from the one the coordinator is using
:::

::: qa
**Q18.** Choose the coordinator responsibilities described in the guide. **Select 2.**

A. Task decomposition and delegation<br>
B. Writing each subagent's tool code at runtime<br>
C. Holding the shared memory that all subagents read directly<br>
D. Aggregating results, and deciding which subagents to invoke<br>
E. Letting specialists resolve errors among themselves
:::

::: qa
**Q19.** Which `AgentDefinition` fields does the guide highlight?

A. API key, region and billing account<br>
B. Description, system prompt and tool restrictions<br>
C. Temperature, seed, stop sequences and retries<br>
D. Database URL, cache size and timeout
:::

::: qa
**Q20.** Which prompt best prepares the synthesis subagent?

A. A list of numbered steps for synthesis, with a request to look up what earlier agents found<br>
B. The goal and quality criteria, plus complete findings as structured data with source metadata<br>
C. "Synthesize the findings from earlier", relying on the coordinator's history being there<br>
D. The goal only, as a subagent automatically knows what its sibling subagents have discovered
:::

::: qa
**Q21.** A new client topic is broad and poorly defined, and the useful directions aren't known up front. Which decomposition fits?

A. None: send one prompt with the whole topic to a single subagent<br>
B. Fixed: use the same five subtopics as last month's unrelated report<br>
C. Dynamic: let intermediate findings shape subsequent subtasks<br>
D. Reverse: write the report, then look for sources that support it
:::

::: qa
**Q22.** An analyst wants to explore a bull case and a bear case from one shared analysis. What's the right session approach?

A. Resume the same session for both cases<br>
B. Use `-p`, and rely on the output being shared<br>
C. Fork from the analysis, once per case<br>
D. Use `/compact`, and write both in one session
:::

::: qa
**Q23.** A compliance policy says no report may be sent until a source check has passed. Which implementation is appropriate?

A. The report subagent includes a line stating that sources have been checked<br>
B. A programmatic gate blocks the send step until the check's result is recorded<br>
C. The coordinator's system prompt asks it to always run the check before sending<br>
D. A few-shot example shows the check being run before a report is sent
:::

::: qa
**Q24.** A generic `analyze_document` tool gives uneven results. Which change follows the guide?

A. Add a required `instructions` string, so that callers can say exactly what they'd like<br>
B. Split it into `extract_data_points`, `summarize_content` and `verify_claim_against_source`<br>
C. Rename it `analyze_document_v2`, and keep its description exactly as it was before<br>
D. Add three more generic tools, so that Claude has more choices for each document
:::

::: qa
**Q25.** Which responsibilities belong to a search subagent facing a transient failure?

A. Report success, and leave out the failure, so that the coordinator's plan isn't disrupted<br>
B. Stop the whole workflow straight away, so that no partial data can enter the report<br>
C. Recover locally, and propagate only what it can't resolve, with partial results and attempts<br>
D. Propagate every failure at once, so that the coordinator can make every retry decision
:::

::: qa
**Q26.** Why is limiting agents to 4–5 tools, rather than 18, an example of a principle rather than a rule?

A. The point is that scope affects selection reliability, and the number is only an illustration<br>
B. The exam guide has a table setting a maximum number for each of the agent types<br>
C. The API rejects a request whenever it contains a tool list longer than exactly five entries<br>
D. MCP servers can only ever expose four or five tools at any one time
:::

::: qa
**Q27.** Tools from three configured MCP servers appear for the agent. When are they discovered, and how are they available?

A. At deployment, and only one server is active per session<br>
B. On each request, one server at a time, in alphabetical order<br>
C. Only after a model has requested a server by name<br>
D. At connection time, and all are available simultaneously
:::

::: qa
**Q28.** Which subagent message gives the coordinator the most useful information?

A. `{"status":"search unavailable","message":"The search service could not be reached. Please try again."}`<br>
B. `{"error":true}`<br>
C. `{"status":"ok","results":[]}`<br>
D. `{"failure":"timeout","query":"EU tariffs 2026","partial":[2 results],"alternatives":["trade database"]}`
:::

::: qa
**Q29.** A report needs earnings figures, market news and a technical analysis. How should the synthesis render these?

A. All of it as bullet points, so that the whole report is uniform throughout<br>
B. Financial data as tables, news as prose, and technical findings as structured lists<br>
C. All of it as tables, so that readers can compare any item with any other<br>
D. All of it as prose, so that the report reads smoothly from start to finish
:::

::: qa
**Q30.** A source says "preliminary estimate", and the synthesis writes the figure as established fact. What should change?

A. Ask the report subagent to add "approximately" to every figure in every report that it produces from now on<br>
B. Preserve source characterizations and separate well-established findings from contested ones<br>
C. Exclude all preliminary sources, including the only sources on a given topic<br>
D. Remove all qualifiers from reports, so that clients can read them more easily
:::

</div>

<div class="lesson">

# Scenario: Developer Productivity with Claude

::: key
Pinecrest Bank's platform team builds a developer productivity agent on the Agent SDK. It explores legacy Java services, generates boilerplate and automates repetitive changes. It uses built-in tools and connects to Jira and an internal documentation service over MCP.
:::

::: qa
**Q31.** What makes this agent model-driven, as opposed to a pre-configured tool sequence?

A. The agent runs on the newest available Claude model<br>
B. The tool order is held in a configuration file that the team can edit<br>
C. Claude decides which tool to call next, based on what it has already learned<br>
D. Every step is approved by a human before the tool runs
:::

::: qa
**Q32.** A coordinator always runs exploration, generation, testing and documentation, even for "what does this class do?" What's the guide's lesson?

A. Analyze what the query needs, and invoke only the relevant subagents<br>
B. Have the user choose the subagents, as coordinators can't analyze a query<br>
C. Always run all subagents, so that results are consistent across all queries<br>
D. Add a caching layer, so that the full pipeline is cheaper on simple queries
:::

::: qa
**Q33.** Which statement is correct about spawning subagents in the guide's terms?

A. Only one subagent can run in any session<br>
B. Subagents share the coordinator's memory automatically<br>
C. Each subagent has to be a separate MCP server<br>
D. The coordinator's `allowedTools` must include `"Task"`
:::

::: qa
**Q34.** Automation must never run `Bash` commands that touch the production database, and failures should be redirected to a ticket. Which is most appropriate?

A. A PostToolUse hook that reports the command to the platform team, once it has finished running<br>
B. A tool-interception hook that blocks such commands, and redirects to a ticket workflow<br>
C. A subagent whose only job is reading the logs for any production access<br>
D. A note in `CLAUDE.md` asking the agent never to touch production data
:::

::: qa
**Q35.** Which task is the best fit for a PostToolUse hook?

A. Normalizing mixed timestamp formats from two MCP tools<br>
B. Preventing a tool from running above a certain limit<br>
C. Choosing which subagent should handle a user's query<br>
D. Writing the final answer for the engineer to read
:::

::: qa
**Q36.** A review of a large generated change misses cross-module bugs, and says conflicting things about similar code. Which decomposition does the guide support?

A. Analyze each file individually, then run a cross-file integration pass<br>
B. Analyze only the largest file, and assume the other files are similar<br>
C. Analyze the files in random order, then keep only the first findings<br>
D. Analyze all the files at once, and ask the model to take more care
:::

::: qa
**Q37.** When should an engineer prefer resuming over starting fresh?

A. When most prior tool results are stale<br>
B. When the code has been heavily rewritten<br>
C. When they want an independent reviewer<br>
D. When prior context is mostly still valid
:::

::: qa
**Q38.** Exploration subagents return overlapping dependency analyses. Which is the coordinator's best fix?

A. Remove all but one subagent, however large the codebase turns out to be<br>
B. Tell the subagents to compare their results directly with each other<br>
C. Assign each subagent a distinct area, such as a service or dependency type<br>
D. Discard any finding that appears in more than one subagent's output
:::

::: qa
**Q39.** Which tool description best follows the guide?

A. "Searches Jira issues by JQL. Input: a JQL string. Returns: key, summary, status. Use for issues, not documentation."<br>
B. "Jira search."<br>
C. "Searches for things in Jira. This tool is very powerful and is useful for a wide variety of tasks you may have."<br>
D. "Use this tool whenever you need information of any sort, because it's the best tool available to the agent."
:::

::: qa
**Q40.** Jira returns a permission error for a restricted project. What should the MCP tool do?

A. Return `isError` with a permission category, marked not retryable, and describe what's wrong<br>
B. Retry until it succeeds, since permissions are often restored in a moment<br>
C. Return text saying "Operation failed", because further details aren't useful to the agent in this case<br>
D. Return an empty list, which keeps the agent from raising unnecessary alarms
:::

::: qa
**Q41.** The team needs a Jira integration. What does the guide suggest first?

A. Use an existing community MCP server, and reserve custom servers for team-specific workflows<br>
B. Give the agent `Bash`, and have it write a new Jira client for each request<br>
C. Build a custom MCP server, because custom servers always fit a team's needs better than community ones<br>
D. Skip MCP, and paste the Jira data into `CLAUDE.md` each morning before work
:::

::: qa
**Q42.** Which belongs in `.mcp.json`?

A. `"Authorization": "see the wiki page for the token"`<br>
B. `"Authorization": "Bearer abc123-real-token"`<br>
C. `"Authorization": "Bearer ${JIRA_TOKEN}"`<br>
D. `"Authorization": "read ~/.claude/CLAUDE.md"`
:::

::: qa
**Q43.** Which sequence best builds understanding of a legacy flow?

A. Grep for the entry point, then Read and follow imports to trace the flow<br>
B. Glob for all Java files, then Write a summary of each, then read those<br>
C. Read every file in the repository, then Grep for whatever seems relevant<br>
D. Edit the entry point to add logging, then run it in the production system
:::

::: qa
**Q44.** An agent needs to find where an error message is produced, and then see how the function is called. Which tools fit? **Select 2.**

A. Grep for the function's name to find its callers<br>
B. Write to create an index of error messages<br>
C. Edit to search through file contents<br>
D. Glob for the error message text<br>
E. Grep for the error message text
:::

::: qa
**Q45.** You want to be sure the agent responds with a tool call and not conversational text, but it may choose the tool. Which setting fits, in the guide's terms?

A. `max_tokens: 1`<br>
B. `tool_choice: {"type": "tool", "name": "search"}`<br>
C. `tool_choice: "any"`<br>
D. `tool_choice: "auto"`
:::

</div>

<div class="lesson">

# Scenario: Claude Code for Continuous Integration

::: key
Ironwood Commerce runs Claude Code in its pipeline. It reviews every pull request before merge, runs a nightly security audit, generates tests and posts findings to developers. Findings must be precise and actionable.
:::

::: qa
**Q46.** Which is the mechanism for giving CI-invoked Claude Code its testing standards and review criteria?

A. A CI runner's `~/.claude/CLAUDE.md`<br>
B. A long `--resume` session history<br>
C. `CLAUDE.md` in the repository<br>
D. An environment variable holding each rule
:::

::: qa
**Q47.** Which pipeline command is best for automated, parseable, non-interactive review?

A. `claude "..." --output-format text`<br>
B. `claude -p "..." --output-format text --append-system-prompt "Reply in JSON"`<br>
C. `claude -p "..." --output-format json --json-schema "$SCHEMA"`<br>
D. `claude --resume last`
:::

::: qa
**Q48.** Reviewers want the same review procedure available to everyone, through the repo. Which location is right, in the guide's terms?

A. `~/.claude/skills/review/SKILL.md` or `~/.claude/commands/review.md`<br>
B. `~/.claude/commands/review.md`<br>
C. `.claude/skills/review/SKILL.md` or `.claude/commands/review.md`<br>
D. `~/.claude.json`
:::

::: qa
**Q49.** Which instruction is an explicit criterion?

A. "Report issues a senior engineer would care about."<br>
B. "Be conservative about what you report here."<br>
C. "Report only the issues that you're highly confident about, and leave out everything else."<br>
D. "Report SQL built by string concatenation with user input. Skip formatting issues."
:::

::: qa
**Q50.** Severity labels vary between runs for the same issue. What's the best fix?

A. Define each level, with concrete code examples<br>
B. Remove severity labels from all findings entirely<br>
C. Average severity across three different runs<br>
D. Add a sentence asking the model to be consistent
:::

::: qa
**Q51.** What's the guide's main concern about a high false-positive category?

A. It costs much more than other categories to run through the API<br>
B. It breaks the JSON schema used for the review's findings<br>
C. It makes the batch API stop processing the job in progress<br>
D. It undermines developers' confidence in accurate categories as well
:::

::: qa
**Q52.** Which few-shot example design most reduces false positives?

A. One example of a clean file, and nothing else<br>
B. Pairs showing an acceptable pattern and a similar genuine issue, with the reasoning<br>
C. Several examples of the same bug in different languages<br>
D. A dozen obvious bugs, along with the exact output format that's expected for each of them
:::

::: qa
**Q53.** Findings are correct, but in varying formats that are hard to act on. What's the guide's most effective technique?

A. Few-shot examples showing location, issue, severity and suggested fix<br>
B. A larger model, with no changes to the prompt at all<br>
C. Longer prose instructions that describe the desired output for each finding<br>
D. A request to write the findings out with greater care
:::

::: qa
**Q54.** The schema marks `suggested_fix` as required, but some findings have no clear fix, and invented fixes have appeared. What should change?

A. Add a minimum string length for that field<br>
B. Make `suggested_fix` nullable<br>
C. Remove findings that have no clear fix<br>
D. Add "be accurate" to the system prompt
:::

::: qa
**Q55.** Which schema field supports later analysis of why developers dismiss findings?

A. `stop_reason`<br>
B. `isRetryable`<br>
C. `custom_id`<br>
D. `detected_pattern`
:::

::: qa
**Q56.** Which job suits the Message Batches API?

A. An interactive debugging session<br>
B. The check that blocks each merge<br>
C. A developer's live question<br>
D. The nightly security audit
:::

::: qa
**Q57.** A manager wants to save 50% by moving pre-merge reviews to batch. What's the best response?

A. Batch has no latency guarantee and can take up to 24 hours, so blocking checks should stay synchronous<br>
B. Batch is fine for pre-merge reviews, as long as developers submit a smaller number of files in each pull request<br>
C. Batch is fine for pre-merge reviews, as it's always finished within a few minutes<br>
D. Batch can't be used for any code analysis, as it doesn't support the code in requests
:::

::: qa
**Q58.** Why can't a batch request carry out a multi-turn audit that runs tools as it goes?

A. Batches don't accept any `tools` parameter in a request, in any form<br>
B. Batches require a human to approve each of the tool calls that are made during the audit<br>
C. Batches are limited to one document of a very small size for each job<br>
D. Batches can't execute tools mid-request and return the results within that request
:::

::: qa
**Q59.** Why is a second, independent Claude instance better at review than asking the generator to double-check itself?

A. It doesn't carry the generator's reasoning context, so it's less inclined to accept prior decisions<br>
B. It runs with extended thinking, which self-review is not able to use at all<br>
C. It always runs on a stronger model, whichever configuration has been selected<br>
D. It has access to the generator's private thinking, so it can see where the generator's mistakes came from
:::

::: qa
**Q60.** A team wants to skip per-file analysis for large PRs, and just use extended thinking on one pass. What's the guide's position?

A. A single pass is enough if its output is restricted by a schema that requires per-file sections<br>
B. Large PRs shouldn't be reviewed by a model, so the team should send them only to human reviewers<br>
C. Extended thinking fully replaces multi-pass review, so it's the simplest and most effective answer for large changes<br>
D. Split them into per-file passes plus integration passes, to avoid attention dilution and contradictory findings
:::

</div>

<div class="lesson">

# Answer Key and Score Sheet

| Q | Ans | Q | Ans | Q | Ans | Q | Ans |
| ---: | --- | ---: | --- | ---: | --- | ---: | --- |
| 1 | D | 2 | C | 3 | C | 4 | A |
| 5 | C | 6 | A | 7 | B | 8 | D |
| 9 | C | 10 | D | 11 | B | 12 | D |
| 13 | B | 14 | B | 15 | D | 16 | B |
| 17 | C | 18 | A, D | 19 | B | 20 | B |
| 21 | C | 22 | C | 23 | B | 24 | B |
| 25 | C | 26 | A | 27 | D | 28 | D |
| 29 | B | 30 | B | 31 | C | 32 | A |
| 33 | D | 34 | B | 35 | A | 36 | A |
| 37 | D | 38 | C | 39 | A | 40 | A |
| 41 | A | 42 | C | 43 | A | 44 | A, E |
| 45 | C | 46 | C | 47 | C | 48 | C |
| 49 | D | 50 | A | 51 | D | 52 | B |
| 53 | A | 54 | B | 55 | D | 56 | D |
| 57 | A | 58 | D | 59 | A | 60 | D |


| Domain | Items | Your score | Target |
| --- | ---: | ---: | ---: |
| 1. Agentic Architecture and Orchestration | 16 | | 70%+ |
| 2. Tool Design and MCP Integration | 11 | | 70%+ |
| 3. Claude Code Configuration and Workflows | 12 | | 70%+ |
| 4. Prompt Engineering and Structured Output | 12 | | 70%+ |
| 5. Context Management and Reliability | 9 | | 70%+ |
| **Total** | **60** | | **80%+** |

The guide doesn't publish how raw scores map to the scaled 720, so these targets are only study guidance.

::: notespace short Your error log: item, what you picked, why it was wrong
:::
</div>

<div class="lesson">

# Explanations

**1. D** (TS 3.1). User-level instructions apply only to that user. The others are in the repository, so they're shared.

**2. C** (TS 3.1). `@import` keeps files modular and selective. *Build today:* the current syntax is written `@path`. Copies drift, commands get forgotten, and user-level isn't shared.

**3. C** (TS 3.2). Skills suit on-demand, task-specific workflows. Always-relevant standards belong in `CLAUDE.md`.

**4. A** (TS 3.2). Isolating exploratory context is what `context: fork` is for. The others don't stop the output from entering the session.

**5. C** (TS 3.3). Path-scoped rules are conditional context. They're still instructions, not enforcement, and they aren't hooks.

**6. A** (TS 3.4). Architecture and multiple approaches call for plan mode. The rest are simple, well-scoped changes.

**7. B** (TS 3.4). Explore isolates verbose discovery and returns summaries. Skipping discovery invites errors, and pasting source wastes context.

**8. D** (TS 3.5). Examples are the guide's answer when descriptions are interpreted inconsistently.

**9. C** (TS 3.5). Independent issues can go one at a time. Together is for interacting problems. The other reasons are false.

**10. D** (TS 5.1). The Messages API needs the history each time. Coherence depends on passing it. `end_turn` erases nothing.

**11. B** (TS 5.1). Progressive summarization risks condensing numbers. A separate facts block preserves them. The other claims aren't true.

**12. D** (TS 5.4). A scratchpad persists findings across context boundaries. Rereading everything reintroduces the problem.

**13. B** (TS 5.4). Summarizing, then injecting, preserves findings without the raw output. The other approaches waste context or lose work.

**14. B** (TS 5.4). Structured exports and a manifest let the coordinator resume and inject state into prompts. The others either lose work, or assume state that isn't there.

**15. D** (TS 5.4). Isolation is the value. Subagents don't have those other properties by default.

**16. B** (TS 1.1). `tool_use` means tools must run and the loop continues. `end_turn` ends it. Text and length aren't signals.

**17. C** (TS 1.2). Isolation means no automatic inheritance. Findings can still be passed in the prompt.

**18. A and D** (TS 1.2). The guide names decomposition, delegation, aggregation and selection, with all communication and error handling going through the coordinator.

**19. B** (TS 1.3). The guide names descriptions, system prompts and tool restrictions. The others aren't what it highlights.

**20. B** (TS 1.3). It needs goals, criteria and the complete findings. Nothing is inherited, and procedural steps reduce adaptability.

**21. C** (TS 1.6). Open-ended work suits adaptive decomposition. A fixed list ignores discovery, and writing first would bias the sources.

**22. C** (TS 1.7). Forks give independent branches from a shared baseline.

**23. B** (TS 1.4). A required order needs a programmatic prerequisite. The others are probabilistic, and a statement in a report proves nothing.

**24. B** (TS 2.1). Purpose-specific tools with defined contracts are the guide's approach. A free-form parameter keeps the tool generic.

**25. C** (TS 2.2). Local recovery with structured propagation is the guide's approach. The others are its anti-patterns, or overload the coordinator.

**26. A** (TS 2.3). The guide's example illustrates decision complexity. It isn't a protocol limit.

**27. D** (TS 2.4). The guide says tools from all configured servers are discovered at connection and available simultaneously.

**28. D** (TS 5.3). Failure type, query, partial results and alternatives enable recovery. Generic statuses hide context, and empty success is misleading.

**29. B** (TS 5.6). The guide says to render content types appropriately, instead of converting everything to a uniform format.

**30. B** (TS 5.6). The guide requires preserving characterizations and methodological context, with explicit sections for established and contested findings.

**31. C** (TS 1.1). The guide defines this distinction by who decides the next action.

**32. A** (TS 1.2). Dynamic selection avoids unnecessary subagents. Caching keeps the wasted pipeline.

**33. D** (TS 1.3). The guide states the first, and it says subagents don't inherit context. *Build today:* the tool is named `Agent`.

**34. B** (TS 1.5). A pre-execution block is a guarantee. PostToolUse runs too late, a note is guidance, and log reading is detection only.

**35. A** (TS 1.5). PostToolUse transforms results before the model processes them. Blocking must happen before execution.

**36. A** (TS 1.6). Per-file plus integration avoids attention dilution. The others ignore the problem.

**37. D** (TS 1.7). Resume when context is mostly valid. Stale results call for a fresh session with a summary, and independent review needs a separate instance.

**38. C** (TS 1.2). Partitioning minimizes duplication. Direct comparison breaks routing, and agreement isn't a reason to discard.

**39. A** (TS 2.1). It states purpose, inputs, outputs and boundaries. The others are vague, minimal or overlapping.

**40. A** (TS 2.2). Permission errors are non-retryable, and they need a clear description. An empty list falsely implies no issues.

**41. A** (TS 2.4). Standard integrations should use community servers. Custom servers are for team-specific needs.

**42. C** (TS 2.4). Environment expansion keeps credentials out of version control. A real token would be committed, and the others don't work.

**43. A** (TS 2.5). The guide describes incremental understanding, rather than reading everything upfront.

**44. A and E** (TS 2.5). Grep searches contents. Glob matches paths, and Write and Edit modify files.

**45. C** (TS 2.3). `any` requires some tool while leaving the choice open. *Build today:* it's rejected on some current models.

**46. C** (TS 3.6). The guide names `CLAUDE.md` as the mechanism. Runner home files aren't shared in the repo. *Build today:* `--bare` skips `CLAUDE.md`.

**47. C** (TS 3.6). It's non-interactive, structured and schema-checked. The others wait for input or return text.

**48. C** (TS 3.2). Project-scoped files are shared. The others are personal.

**49. D** (TS 4.1). It defines what to report and what to skip. The rest are vague, or based on confidence.

**50. A** (TS 4.1). Concrete criteria and examples give consistent classification.

**51. D** (TS 4.1). False positives erode trust across categories. The other claims aren't what's described.

**52. B** (TS 4.2). Distinguishing acceptable from genuine, with reasoning, supports generalization.

**53. A** (TS 4.2). Few-shot examples are the guide's answer when instructions alone give inconsistent format.

**54. B** (TS 4.3). Nullable removes the pressure to fabricate. A minimum length makes it worse, and dropping findings loses valid issues.

**55. D** (TS 4.4). `detected_pattern` records which construct triggered a finding. The others serve other purposes.

**56. D** (TS 4.5). Batches fit non-blocking, latency-tolerant work, as there's no latency guarantee.

**57. A** (TS 4.5). Blocking workflows don't suit batch. The other statements misstate what batch does.

**58. D** (TS 4.5). The guide says batches don't support multi-turn tool calling within a request.

**59. A** (TS 4.6). Independence is the point. The guide says independent instances beat self-review instructions and extended thinking.

**60. D** (TS 4.6). Multi-pass is the guide's design. A schema shapes the output without fixing the attention problem.

</div>
