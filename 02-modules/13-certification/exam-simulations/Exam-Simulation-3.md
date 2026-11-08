# Exam Simulation 3

**Candidate:** ________  **Start time:** ________  **End time:** ________

| | |
| --- | --- |
| Items | 60, in 4 scenario sets of 15 |
| Time | 120 minutes, about 2 minutes per item |
| Scenarios | Multi-Agent Research System, Developer Productivity with Claude, Code Generation with Claude Code, Claude Code for Continuous Integration |

## Rules

- Work in one sitting, with no notes, tools or AI help, and set a 120-minute timer.
- **To tick an answer,** change `[ ]` to `[x]`. Editors with Markdown checkboxes let you click instead.
- Each item says how many answers to select. A multiple-response item only counts if every choice is right.
- Don't open the `keys` folder until you've finished.
- When you've finished, save this file and ask Claude to evaluate it.

> These are original practice items, written from the public exam guide. They aren't real or leaked exam questions, the company names are invented, and a score here doesn't predict your exam result.

---

## Scenario 1: Multi-Agent Research System

*Suggested checkpoint: start by minute 0.*

Orchard Research is building a multi-agent research system on the Claude Agent SDK. A coordinator delegates to specialized subagents that search the web, analyze documents, synthesize findings and generate reports. Policy teams rely on the system's cited reports, which must show what's established, what's contested and where evidence is missing.

### Question 1

Which two statements are true of a standard subagent spawned by the coordinator? **Select 2.**

- [ ] A. Its own tool calls stay in its context, and its final message returns to the coordinator
- [ ] B. Its prompt from the spawning call is the channel for passing it context
- [ ] C. It shares a memory store with other subagents by default
- [ ] D. It restores its context from the last time it was invoked
- [ ] E. It can read the tool results from the coordinator's earlier turns

### Question 2

A coordinator's prompt to the analysis subagent says, "Analyze the second source we found earlier, and compare it with the one from before." The subagent returns vague, generic commentary. What's wrong? **Select 1.**

- [ ] A. The prompt is too short, because subagent prompts must meet a minimum required length
- [ ] B. The subagent's memory should have been reloaded from the earlier run with `--resume`
- [ ] C. It has no access to prior context, so those references point at nothing
- [ ] D. The prompt needs a forced `tool_choice`, so that the subagent will use its analysis tools

### Question 3

A report on "the economic effects of aging populations" covers only pensions and healthcare costs, though every subagent reports success. Logs show the assignments were "pension funding," "healthcare spending" and "care home costs." The team proposes tightening the synthesis prompt. What's your assessment? **Select 1.**

- [ ] A. The search subagent is at fault, because its queries should have gone beyond what it was assigned
- [ ] B. The team is right, because synthesis should have identified and added the missing labor market effects
- [ ] C. The gap comes from the coordinator's narrow decomposition, so the fix belongs there
- [ ] D. The report subagent should reject any report that has fewer than five major topic sections

### Question 4

A shared MCP server gives the research subagents access to a licensed data feed, and requires an API key. Every developer needs the server, and each has their own key. How should it be configured? **Select 1.**

- [ ] A. In the project's `CLAUDE.md`, with a note telling developers not to share their key values
- [ ] B. In `.mcp.json`, with a single shared key committed, so that nobody has to configure anything
- [ ] C. In `.mcp.json`, using `${DATA_FEED_KEY}`, with each developer's key in their own environment
- [ ] D. In each developer's `~/.claude.json`, because only user scope is able to hold credentials

### Question 5

The coordinator reviews a draft and finds no coverage of the regulatory position. Which next step fits the iterative refinement pattern? **Select 1.**

- [ ] A. Restart the whole pipeline from scratch, using the same queries and the same set of subagents
- [ ] B. Re-delegate targeted regulatory queries to search and analysis, then re-invoke synthesis
- [ ] C. Accept the draft, since all of the subagents reported that their work had finished successfully
- [ ] D. Ask synthesis to write the regulatory section from its general knowledge, and mark it as provisional

### Question 6

Your coordinator's loop stops when the reply contains the phrase "research complete." On some runs it never stops, and on others it stops while Task calls are still pending. What should control this loop? **Select 1.**

- [ ] A. The `stop_reason` on each response, continuing on `tool_use` and finishing on `end_turn`
- [ ] B. A rule in the system prompt requiring the exact phrase "research complete" at the end of each run
- [ ] C. A fixed count of ten iterations, which is more than almost all of the research runs have needed
- [ ] D. A broader list of phrases, so that more ways of saying the work is finished can be recognized

### Question 7

The synthesis subagent has Task, WebSearch and all the document tools, added "for flexibility." It spends much of each run searching. What's the best fix? **Select 1.**

- [ ] A. Raise its iteration cap so that it can finish searching
- [ ] B. Restrict its tools to those its role needs
- [ ] C. Force its write tool on all turns with `tool_choice`
- [ ] D. Add "don't search" to its prompt, in capitals

### Question 8

Sources return dates as Unix time, ISO 8601 and free text such as "last spring." The synthesis step gets event order wrong. Where's the most reliable place to handle the formats that can be converted reliably? **Select 1.**

- [ ] A. A `PostToolUse` hook, which normalizes results before the agent reasons about them
- [ ] B. A hook on outgoing tool calls, which asks each source to use one preferred date format
- [ ] C. A new subagent whose only job is to read the sources again and rewrite all of their dates
- [ ] D. A longer system prompt for synthesis, which explains how to convert all of these formats

### Question 9

A search subagent's source is down, so it returns an empty result marked as success. The final report says, "no significant developments were found in this region." What's the best way to describe and correct this? **Select 1.**

- [ ] A. The report's wording is too strong, so synthesis should be told to use more cautious language
- [ ] B. Silently suppressing the error misrepresents a failure, so return structured error context instead
- [ ] C. The whole run should terminate whenever a source is down, as partial reports can't be trusted
- [ ] D. The subagent behaved correctly, since a coordinator shouldn't be troubled by a temporary source failure

### Question 10

A search tool returns "no results" for a very specific query. On the same day, the same tool times out on another. Which two statements describe how these should be treated? **Select 2.**

- [ ] A. The timeout is an access failure, which may justify a retry decision
- [ ] B. Both should be reported as errors, since neither one has returned any findings
- [ ] C. The no-results response is a successful query, and is valid information
- [ ] D. Both should be reported as empty success, to keep the workflow from being interrupted
- [ ] E. The no-results response should be retried until the query returns at least one result

### Question 11

Analysis finds that two credible sources disagree on a key figure. The analysis subagent currently chooses one, then passes it on. How should it behave? **Select 1.**

- [ ] A. Choose the value from the source that it considers the more reputable, then note that one was rejected
- [ ] B. Average the two values, and pass on the result with a note saying that it's an estimate
- [ ] C. Complete its analysis with both values annotated, so that the coordinator decides how to reconcile them
- [ ] D. Stop its analysis, and return an error to the coordinator, because it can't proceed with conflicting data

### Question 12

The coordinator has finished a second refinement round on a report about water policy. One reviewer wants to stop, because two rounds is the team's habit, while another wants to continue until the subagents have nothing further to add. According to the pattern, what should decide when a normal run stops? **Select 1.**

- [ ] A. When a subagent reports that it would like to stop
- [ ] B. When coverage meets its stated quality criteria
- [ ] C. As soon as the first draft comes back from synthesis
- [ ] D. After exactly three rounds, whatever has been found

### Question 13

Why should research findings be passed between agents as structured data, rather than prose? **Select 1.**

- [ ] A. It separates content from metadata such as source URLs, so attribution survives
- [ ] B. It means that the model doesn't have to read the text, which reduces its token use
- [ ] C. It's the only input format that a subagent's prompt is able to accept from a coordinator
- [ ] D. It verifies automatically that each of the cited claims in the findings is accurate

### Question 14

Policy requires a human sign-off before any report that names a company in a fraud allegation is published. The report subagent sometimes publishes these directly. How should this be enforced? **Select 1.**

- [ ] A. Block the publish tool in code until a recorded approval exists for that report
- [ ] B. Have compliance review a weekly list of all the published reports that mention fraud allegations
- [ ] C. Add a sentence to the report itself, telling readers that human sign-off may not have occurred
- [ ] D. Add a prominent rule on sign-off to the report subagent's system prompt and tool description

### Question 15

A 30-document literature review is run in one pass. Analysis of later documents is shallow, and the model contradicts itself about the same study. What's the best restructuring? **Select 1.**

- [ ] A. Keep the one pass, and ask the model to give each of the documents the same level of attention
- [ ] B. Analyze only the 10 most-cited documents, as those tend to contain the most useful findings
- [ ] C. Use a bigger model in one pass, with a much higher limit on the output tokens
- [ ] D. Analyze each document separately, then run a dedicated pass across all of them

---

## Scenario 2: Developer Productivity with Claude

*Suggested checkpoint: start by minute 30.*

Kestrel Tools builds an internal developer agent on the Claude Agent SDK. It helps engineers navigate a large multi-language codebase, understand legacy services, write boilerplate and automate repetitive changes. It uses the built-in tools (Read, Write, Edit, Bash, Grep, Glob) and several Model Context Protocol servers.

### Question 16

An agent has analyzed a service, and you now want one subagent to draft a migration and another to review its risks. The main agent's analysis is long, and only part of it matters to each. How should you pass context? **Select 1.**

- [ ] A. Include the relevant findings and the goal in each subagent's prompt
- [ ] B. Have each subagent query the main agent's history when it needs to know more
- [ ] C. Paste the entire raw conversation into each prompt, so nothing can be lost
- [ ] D. Rely on the subagents inheriting the main agent's analysis automatically

### Question 17

An agent runs `grep -r` through Bash for each question about the code, and its long output is filling the context. Which improvement is most reasonable? **Select 1.**

- [ ] A. Use the Grep tool with narrower patterns or paths, so that the output is focused
- [ ] B. Use Write to save the output to a file, then read the whole file back afterward
- [ ] C. Use Glob for content searches instead, as it generally returns many fewer results
- [ ] D. Keep using Bash, and instruct the model to skim through the output more quickly

### Question 18

Agents waste many calls asking "which schemas exist?" before they write a query. Your database's schemas rarely change. Which two approaches reduce this, in line with the guide's recommendations? **Select 2.**

- [ ] A. Create a separate MCP tool for each table in the database
- [ ] B. Add a prompt line telling agents to make fewer exploratory calls
- [ ] C. Expose the schema catalog as an MCP resource
- [ ] D. Raise the agents' iteration caps so that exploring costs less
- [ ] E. Let agents read the catalog first, then fetch only the relevant details

### Question 19

Your subagent's file-fetch tool fails on a permissions error when it tries to read a restricted repository. How should it report this? **Select 1.**

- [ ] A. A permission error, not retryable, with a clear explanation of what couldn't be accessed
- [ ] B. A validation error, asking the caller to correct the format of the repository's name and try again
- [ ] C. A transient error, retryable, in case the repository's permissions are changed in the near future
- [ ] D. An empty success result, since revealing that the repository exists could be a security problem

### Question 20

You need all files whose names match `**/*Controller.java`. Which tool is the right fit, and why? **Select 1.**

- [ ] A. Read, because it can load a whole directory
- [ ] B. Edit, because it uses unique text matching
- [ ] C. Grep, because it matches file names and contents
- [ ] D. Glob, because it matches file paths by pattern

### Question 21

You need your agent to always call a tool rather than replying in text, and you don't mind which tool it picks. Which `tool_choice` setting fits? **Select 1.**

- [ ] A. `"auto"`
- [ ] B. `{"type": "tool", "name": "search"}`
- [ ] C. `{"type": "text"}`
- [ ] D. `"any"`

### Question 22

A read-only `code-explorer` subagent has been given all 22 tools from every server, with a prompt telling it not to change anything. It occasionally edits files. Which option is the correct form of scoped access? **Select 1.**

- [ ] A. Keep all 22 tools, and repeat the instruction not to change anything in capitals
- [ ] B. Pass it all the tools that the coordinator has, so that it can handle any request
- [ ] C. Give it only Read, Grep and Glob, plus any read-only MCP tools its role needs
- [ ] D. Keep all 22 tools, and add a nightly check that reverts any edits that it makes

### Question 23

A hook normalizes status values from an MCP tool. The tool's owner changes the format without notice, and values start passing through unconverted, confusing the agent. What practice would catch this soonest? **Select 1.**

- [ ] A. A monthly review of the agent's reasoning, on a sample of its conversations
- [ ] B. Removal of the hook, because the model can handle most formats reasonably well
- [ ] C. An instruction asking the model to report whenever it feels confused by its data
- [ ] D. Tests on the hook, plus flagging of any unknown values as they appear

### Question 24

To reduce tokens, a contributor changes the agent's loop to send the original task and only the newest tool result on each request. On multi-step tasks, the agent now re-reads files it has already read, and it sometimes contradicts its earlier conclusions. What explains this? **Select 1.**

- [ ] A. The model caches earlier turns on the server, and the partial history is corrupting that cache
- [ ] B. Repeating steps is expected when a model uses more than a few tools during a single task
- [ ] C. Sending one tool result at a time turns on a mode in which the model restarts the task
- [ ] D. Each request has to carry the conversation, because the model only knows what it's sent

### Question 25

A `/explore-options` skill generates a dozen candidate designs, most of which get rejected. Engineers notice that code written later in the same session drifts toward the rejected designs. Which change to the skill best addresses this? **Select 1.**

- [ ] A. Add a closing instruction asking the model to forget all of the rejected ideas
- [ ] B. Add `context: fork`, so that the exploration doesn't remain in the main conversation
- [ ] C. Move the skill's text into `CLAUDE.md`, so that it's loaded in all the sessions
- [ ] D. Set `argument-hint` to "final only," so that rejected ideas are never generated

### Question 26

Agents keep misrouting between two MCP servers that both expose a `search` tool: one searches wiki pages, the other searches source code. Both are correctly configured. What's the best first fix? **Select 1.**

- [ ] A. Disconnect one of the servers, as an agent can only use one of them at any one time
- [ ] B. Make each description state what it searches, and when to use the other
- [ ] C. Add a `CLAUDE.md` note saying that the second server is usually the better option
- [ ] D. Move one server to user scope, as this lowers its priority during tool selection

### Question 27

A laptop crash interrupted an investigation of the build system. Since then, teammates have merged changes touching about half of the files that the session analyzed. An engineer asks whether to resume, or to begin a fresh session with a summary. Which factor should decide this? **Select 1.**

- [ ] A. Whether the model's version has been updated
- [ ] B. How much of the prior context is still valid
- [ ] C. Whether the session has a memorable name
- [ ] D. How many tokens the session has used so far

### Question 28

You investigated a memory leak in a session and want to return to the same investigation tomorrow. How should you do that? **Select 1.**

- [ ] A. Run `--history` with the name of the session you're looking to continue
- [ ] B. Name the session, and continue it with `--resume <session-name>`
- [ ] C. Save the session as a skill, and invoke it tomorrow by running it again
- [ ] D. Add the investigation's name to `CLAUDE.md`, so that it reloads tomorrow

### Question 29

In a trace, you see that the model asked for a Grep on `auth/` and a Read of `session.py` in one response. Your loop runs only the Grep, sends that back, and expects the model to ask for the Read again. How should the loop handle responses like this? **Select 1.**

- [ ] A. Send a separate user message for each result, with a model call between the two messages
- [ ] B. Run the first only, as the model can use just one result in each iteration of the loop
- [ ] C. Run both, and return a `tool_result` for each, matched by ID, in the next user message
- [ ] D. Merge both results into a single text block, and add it to the system prompt of the next request

### Question 30

Which is the most efficient way for an agent to understand how a request flows through an unfamiliar service? **Select 1.**

- [ ] A. Ask the model to describe how services of this type are usually structured
- [ ] B. Read all of the files first, then form a view of how the service fits together
- [ ] C. Read only the files with the largest sizes, because they hold most of the logic
- [ ] D. Find the entry point with Grep, then Read and follow imports to trace the flow

---

## Scenario 3: Code Generation with Claude Code

*Suggested checkpoint: start by minute 60.*

Granite Works has a large codebase maintained by several product teams. You use Claude Code for code generation, refactoring, debugging and documentation, and you're responsible for the team's `CLAUDE.md` files, custom commands and skills, and path-specific rules, and for guidance on using plan mode. Some sessions run for many hours across large parts of the code.

### Question 31

You're mapping a large service, and verbose search output keeps crowding the main session. Which approach best keeps the main session focused on high-level understanding? **Select 1.**

- [ ] A. Ask the main session to ignore any output that isn't relevant to the current question
- [ ] B. Spawn subagents for specific questions, such as "trace the refund flow's dependencies"
- [ ] C. Paste all of the search output into a file, and read that file into the main session
- [ ] D. Increase the maximum output tokens, so that the main session can hold more of the results

### Question 32

A subagent exploring your repo can't read one submodule because of a permissions error, but it finishes everything else. What should it return? **Select 1.**

- [ ] A. Its results, plus what failed, what was attempted, and which areas weren't covered
- [ ] B. Its results only, since one unreadable submodule isn't important enough to report
- [ ] C. An empty result, so that the main session can start the exploration over again
- [ ] D. An error only, without its results, because partial results can't be trusted

### Question 33

A subagent returns 8,000 words of reasoning to an orchestrating session with limited context, and the important facts are getting lost. What's the most effective upstream change? **Select 1.**

- [ ] A. Have the subagent return its output twice, once at the start and once at the end
- [ ] B. Have the subagent return structured key facts and citations, rather than reasoning chains
- [ ] C. Have the orchestrating session summarize each output with a one-line description
- [ ] D. Have the subagent write in shorter sentences, while keeping all of the same content

### Question 34

A data migration script mishandles nulls and empty strings, which are separate cases. What's the most effective way of communicating the required behavior? **Select 1.**

- [ ] A. Add a note to `CLAUDE.md` saying that edge cases matter a great deal for this project
- [ ] B. Ask Claude to handle edge cases more robustly, and to review the script more carefully
- [ ] C. Provide specific test cases, showing example input and the expected output for each
- [ ] D. Describe the requirement again in a longer paragraph, repeating the most important points

### Question 35

Developers often run `/migrate-db` without saying which target version they want, and the skill then guesses. Which frontmatter helps? **Select 1.**

- [ ] A. `allowed-tools`, to limit the tools used during the skill
- [ ] B. `paths`, to restrict the skill to the migrations directory
- [ ] C. `argument-hint`, to prompt for the required parameter
- [ ] D. `context: fork`, to run the skill in an isolated context

### Question 36

Which two statements about the `CLAUDE.md` hierarchy are correct? **Select 2.**

- [ ] A. A `CLAUDE.md` in a subdirectory applies to work in that part of the codebase
- [ ] B. Only one `CLAUDE.md` file can be loaded in any one session
- [ ] C. A project-level file applies only to the developer who wrote it
- [ ] D. Instructions in `~/.claude/CLAUDE.md` aren't shared with teammates through version control
- [ ] E. User-level instructions override everything for all teammates

### Question 37

Conventions for test files should apply to all `*.test.tsx` files, wherever they are in the repo, and shouldn't load otherwise. Which configuration achieves this? **Select 1.**

- [ ] A. A root `CLAUDE.md` section, with a heading naming test files
- [ ] B. `.claude/rules/testing.md`, with `paths: ["**/*.test.tsx"]`
- [ ] C. A `CLAUDE.md` file placed in every directory with a test
- [ ] D. A `/test-rules` command that developers run before each edit

### Question 38

A long debugging session has been summarized twice, and you notice the summary now says "a performance issue in checkout," where it once had "p95 latency of 2,300 ms after the 14 June deploy." What's the underlying risk? **Select 1.**

- [ ] A. The session has reached the model's output limit, and the details have been cut off
- [ ] B. The latency details are being blocked by a rule in the project's `CLAUDE.md`
- [ ] C. Progressive summarization condenses numbers and dates into vague statements
- [ ] D. Summaries can only retain text from the last few turns of the conversation

### Question 39

You're asked to restructure a monolith into services, with several workable boundaries. A colleague suggests starting direct execution on the "obvious" modules to build momentum. What's the best response? **Select 1.**

- [ ] A. Begin direct execution, and let the architecture emerge from what's learned along the way
- [ ] B. Begin in plan mode to compare boundaries, then execute the plan that's agreed
- [ ] C. Stay in plan mode throughout, because changes that large should never be made through Claude Code
- [ ] D. Write the whole design in a long prompt, and have Claude execute it with no further exploration

### Question 40

You're adding request deduplication to a part of the platform you've never worked on, and you suspect there are failure modes you haven't thought of. Before any code is written, which technique best brings these considerations out? **Select 1.**

- [ ] A. Have Claude ask you questions before it starts implementing
- [ ] B. Paste a long article on deduplication, and ask for code based on it
- [ ] C. Implement right away, then ask about any problems that come up
- [ ] D. Have Claude pick all the design parameters without discussion

### Question 41

Your team's `/changelog` command should be available to every developer on clone or pull. Where should it go? **Select 1.**

- [ ] A. `.claude/config.json`
- [ ] B. The root `CLAUDE.md`
- [ ] C. `~/.claude/commands/`
- [ ] D. `.claude/commands/`

### Question 42

Your root `CLAUDE.md` has grown to 1,100 lines covering testing, APIs and deployment. It's hard to maintain, and much of it is irrelevant to any given task. Which restructuring is best? **Select 1.**

- [ ] A. Move it to `~/.claude/CLAUDE.md`, so that it's no longer in the repository taking up space
- [ ] B. Copy it into each subdirectory, so that each team has its own complete version to maintain
- [ ] C. Delete sections until it's much shorter, and rely on the developers to remember the rest
- [ ] D. Split it into topic files in `.claude/rules/`, such as `testing.md` and `api-conventions.md`

### Question 43

A long documentation session summarizes earlier findings, and links to the relevant source files start disappearing from later output. What should you require in the findings? **Select 1.**

- [ ] A. A shorter summary, so that there's less text that could lose a link
- [ ] B. A confidence rating for each of the claims in the documentation
- [ ] C. A single list of files consulted, kept at the end of the document
- [ ] D. Structured claim-to-source mappings, preserved through each summary

### Question 44

A coordinator running several agents crashes three hours into a long migration analysis. How should the design allow recovery without repeating the work? **Select 1.**

- [ ] A. Have each agent export its state to a known location, and load a manifest on resume
- [ ] B. Start the whole analysis again, since state from an interrupted run can't be trusted at all
- [ ] C. Have each agent write a free-form summary to the logs, and read through them after the crash
- [ ] D. Keep all the state in the coordinator's conversation, then resume the conversation after the crash

### Question 45

Late in a long session, Claude answers questions about a class by describing what such classes "usually" do, though it read the actual class earlier. Which two practices help? **Select 2.**

- [ ] A. Raise `max_tokens`, so that its responses contain more specific detail
- [ ] B. Ask Claude to try to remember the class that it read earlier in the session
- [ ] C. Keep a scratchpad file of key findings, and refer to it for later questions
- [ ] D. Delegate further verbose exploration to subagents that return summaries
- [ ] E. Re-read every previously read file, so that the facts are refreshed in context

---

## Scenario 4: Claude Code for Continuous Integration

*Suggested checkpoint: start by minute 90.*

Vantage Retail runs Claude Code in CI/CD to review pull requests, generate test cases and post findings as inline comments. Developers can dismiss a finding, and they have complained about noise. You own the review prompts and the way findings are structured, and you want actionable feedback that the team will trust.

### Question 46

Which command is most suitable for a pipeline that posts findings as inline PR comments? **Select 1.**

- [ ] A. `claude -p "Review this PR" --batch --ci-mode`
- [ ] B. `claude "Review this PR" --output-format text`
- [ ] C. `claude --resume review-job "Review this PR"`
- [ ] D. `claude -p "Review this PR" --output-format json --json-schema "$SCHEMA"`

### Question 47

A weekly security audit of all repositories is expensive with synchronous calls. Nobody waits on the results. What's the best change, and what should you keep in mind? **Select 1.**

- [ ] A. Use the Message Batches API, along with mid-request tool calls to read each repository's files
- [ ] B. Use the Message Batches API for 50% savings, knowing that results can take up to 24 hours with no latency guarantee
- [ ] C. Use the Message Batches API, since it guarantees that all of its results will be returned within one hour
- [ ] D. Keep synchronous calls, as batch processing doesn't support any kind of code analysis work

### Question 48

You run a verification pass that has the model report its confidence alongside each finding. What's the appropriate use of this? **Select 1.**

- [ ] A. Treating the highest score as proof that the finding is accurate
- [ ] B. Discarding the findings that have low scores, without any checks
- [ ] C. Replacing all human review, as long as scores are above 90%
- [ ] D. Routing findings for review, after calibrating against known results

### Question 49

Findings must fit a schema in which `suggested_fix` is required. For design-level concerns, where no single fix is obvious, the reviewer has started attaching weak or invented fixes, and developers are complaining about them. What should you change? **Select 1.**

- [ ] A. Add "suggest the best fix you can" to the prompt
- [ ] B. Add a validator that checks each fix compiles
- [ ] C. Make the field longer, by raising its maximum
- [ ] D. Make `suggested_fix` optional or nullable

### Question 50

Findings have inconsistent shape: some give a location and fix, others a vague remark. Instructions alone haven't helped. What's the most effective addition? **Select 1.**

- [ ] A. Few-shot examples showing the format: location, issue, severity and suggested fix
- [ ] B. A second review pass that rewrites every finding after the first pass is complete
- [ ] C. A rule in capital letters saying that each finding must always contain a location
- [ ] D. A longer description of each output field, with more detailed prose explanations

### Question 51

Your job script needs Claude Code to review just the changes in the current pull request on each run, with no person present. How should the diff reach Claude Code? **Select 1.**

- [ ] A. Rely on Claude's memory of the diff from the previous job's session
- [ ] B. Open an interactive session, and paste it in through a remote terminal
- [ ] C. Add it to `CLAUDE.md`, and commit that to the repo before each run
- [ ] D. Provide it in the `-p` prompt, or as piped context from the job script

### Question 52

Developers dismiss many findings, but you can't tell which code constructs cause the false positives. What should you add to the structured findings? **Select 1.**

- [ ] A. A free-text `notes` field, so that developers can explain each of their dismissals in their own words
- [ ] B. A `detected_pattern` field, so that dismissals can be analyzed by the construct that triggered them
- [ ] C. A `dismissed` field set by the model, based on whether it thinks a developer will dismiss the finding
- [ ] D. A `confidence` field, so that any finding with a lower score is automatically hidden from developers

### Question 53

Which two approaches are most effective at catching subtle issues in code generated by Claude? **Select 2.**

- [ ] A. A second, independent Claude instance without the generator's reasoning context
- [ ] B. A rule that only those findings reported by three runs in a row are ever posted
- [ ] C. Extended thinking in the generating session, so that it can reconsider its work
- [ ] D. Separate per-file passes, plus a cross-file integration pass on large changes
- [ ] E. Asking the generating session to review its own work with more critical wording

### Question 54

A structured review output passes schema validation, but references a line number that doesn't exist in the file. What kind of error is this, and how should it be handled? **Select 1.**

- [ ] A. A syntax error, which can be fixed by adding stricter types to the JSON schema
- [ ] B. A transient error, which can be fixed by retrying the same request several times
- [ ] C. A semantic error, which needs validation logic and a retry with the specific error
- [ ] D. A batch error, which can be fixed by resubmitting the request with its `custom_id`

### Question 55

Review output contains many style remarks along with real bugs. The prompt says to "report only important issues." Which rewrite is most likely to help? **Select 1.**

- [ ] A. Ask the model to report only those findings that it's at least 90% confident about
- [ ] B. Add "be conservative" to the prompt, and repeat it at the end of the instructions
- [ ] C. List categories to report (bugs, security) and to skip (minor style, local patterns), with examples
- [ ] D. Say that important means anything a senior engineer would really care about

### Question 56

Your team wants to move every Claude Code job to the Batches API to cut costs, including the pre-merge check that blocks developers. What's your advice? **Select 1.**

- [ ] A. Move the non-blocking jobs only, and keep the pre-merge check synchronous
- [ ] B. Move the pre-merge check only, because it's where most of the cost lies
- [ ] C. Move all of the jobs, as batches almost always finish within a few minutes
- [ ] D. Move none of the jobs, as a batch can't produce any structured output

### Question 57

The review flags a harmless team pattern as a bug, but a similar-looking pattern really is a bug. You want it to tell them apart, including in new code. Which approach fits? **Select 1.**

- [ ] A. Add 2–4 examples showing each pattern, with the reasoning for why one is an issue
- [ ] B. Add a list of every file where the harmless pattern appears, for the model to skip
- [ ] C. Add a rule that all findings about this pattern should be suppressed in the reports
- [ ] D. Add many examples of real bugs of other kinds, so that the model sees what bugs look like

### Question 58

Two runs on the same code label one issue as "critical" and "minor" respectively. How should you make severity more consistent? **Select 1.**

- [ ] A. Define each severity level explicitly, with concrete code examples
- [ ] B. Remove severity from the output, and let developers decide on it
- [ ] C. Run the review twice, then take the higher of the two severities
- [ ] D. Ask the model to think harder before it assigns a severity level

### Question 59

Your review procedure is long, and is needed only in the review job. A teammate wants to add it to the root `CLAUDE.md`, so that it's always available. What do you recommend? **Select 1.**

- [ ] A. Paste it into each pull request's description, so that it's visible to the pipeline
- [ ] B. Add it to `CLAUDE.md`, as team procedures should always be loaded in every session
- [ ] C. Keep it in a checked-in skill or command, invoked by the review job
- [ ] D. Keep it on the CI runner's home directory, so that it's away from the application code

### Question 60

A pull request changes an API in one file and its callers in nine others. Per-file review passes all report no problems, but a caller is now passing the wrong argument type. What's missing? **Select 1.**

- [ ] A. A review of the largest changed files only, with greater depth
- [ ] B. A longer prompt for each per-file review, asking it to look harder
- [ ] C. A separate integration pass that looks at data flow across files
- [ ] D. A second per-file pass, using exactly the same files and prompt

---

**End of simulation.** Save this file and ask Claude to evaluate it.
