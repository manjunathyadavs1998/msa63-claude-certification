# Exam Simulation 1

**Candidate:** ________  **Start time:** ________  **End time:** ________

| | |
| --- | --- |
| Items | 60, in 4 scenario sets of 15 |
| Time | 120 minutes, about 2 minutes per item |
| Scenarios | Customer Support Resolution Agent, Multi-Agent Research System, Code Generation with Claude Code, Structured Data Extraction |

## Rules

- Work in one sitting, with no notes, tools or AI help, and set a 120-minute timer.
- **To tick an answer,** change `[ ]` to `[x]`. Editors with Markdown checkboxes let you click instead.
- Each item says how many answers to select. A multiple-response item only counts if every choice is right.
- Don't open the `keys` folder until you've finished.
- When you've finished, save this file and ask Claude to evaluate it.

> These are original practice items, written from the public exam guide. They aren't real or leaked exam questions, the company names are invented, and a score here doesn't predict your exam result.

---

## Scenario 1: Customer Support Resolution Agent

*Suggested checkpoint: start by minute 0.*

Larkfield Home sells furniture online. You're building a support resolution agent on the Claude Agent SDK, with custom MCP tools `get_customer`, `lookup_order`, `process_refund` and `escalate_to_human`. The agent handles returns, billing disputes and account issues, and the target is 80%+ first-contact resolution. It may refund up to $400 without approval. Human agents can see only what the agent sends them, not the conversation.

### Question 1

When the order database times out, `lookup_order` returns `{"orders": []}`, the same result as a search that found nothing. Customers are being told that their orders don't exist. What's the right fix? **Select 1.**

- [ ] A. Have the agent retry every empty result three times before it tells the customer that no orders were found
- [ ] B. Add a prompt instruction that an empty result may be caused by a timeout, so the agent should word its reply carefully
- [ ] C. Return an error in both cases, so the agent treats any empty result as something that needs a human
- [ ] D. Flag the timeout with `isError` as a retryable failure, and keep the empty list for real searches with no matches

### Question 2

A customer writes: "This is the third time I've contacted you about this and I'm really annoyed." The problem is a standard damaged-item replacement that policy clearly covers. They haven't asked for a person. What should the agent do? **Select 1.**

- [ ] A. Acknowledge the frustration and offer to resolve it, escalating if they then state a preference for a human
- [ ] B. Escalate straight away, because repeated contact and negative sentiment show the case is beyond the agent
- [ ] C. Ask the customer to rate their frustration, and escalate if it falls above a configured threshold value
- [ ] D. Continue with the replacement without responding to the frustration, so that the reply stays focused on the task

### Question 3

Human agents are reopening escalated cases and asking customers to repeat themselves. Your escalation note currently reads: "Customer needs help with a refund, please review." Given how this deployment works, what should the agent send instead? **Select 1.**

- [ ] A. The same note, with a link to the conversation so that the human can read it if they need more detail
- [ ] B. The full raw transcript with all tool results, so that nothing relevant can be left out of the handoff
- [ ] C. A confidence score and a sentiment rating, so that the human can decide how urgently to treat the case
- [ ] D. A structured summary with the customer ID, root cause, refund amount and recommended action

### Question 4

`process_refund` returns "Operation failed" for timeouts, invalid amounts and refunds outside the 30-day policy window. The agent retries all of them, sometimes three times, and then gives customers a vague apology. Which change best supports recovery? **Select 1.**

- [ ] A. Keep the single message, but instruct the agent never to retry any failed tool call more than a single time
- [ ] B. Retry every failure automatically inside the tool, and only report back after five attempts have been made
- [ ] C. Return an error category, a retryable flag and a customer-friendly explanation, so that policy refusals aren't retried
- [ ] D. Return an HTTP status code in the message, and have the agent look up what each of the codes means

### Question 5

You're reviewing your agent's rules, to decide which need programmatic enforcement and which can stay as prompt guidance. Which two belong in programmatic enforcement? **Select 2.**

- [ ] A. Replies to customers with damaged items begin by acknowledging the inconvenience
- [ ] B. Replies stay under 150 words, unless the case is unusually complicated
- [ ] C. The agent mentions store credit as an option when a customer seems undecided
- [ ] D. No refund above the $400 limit executes without human approval
- [ ] E. No order operation runs until `get_customer` has returned a verified customer ID

### Question 6

Finance requires that no refund above $400 ever executes without a human, and that these cases still reach someone who can act on them. Which design meets the requirement? **Select 1.**

- [ ] A. A hook that intercepts outgoing `process_refund` calls above the limit, blocks them, and redirects to `escalate_to_human`
- [ ] B. A line in the tool's description stating the $400 limit, with few-shot examples of the agent escalating larger amounts
- [ ] C. A nightly job that compares refund amounts with the limit, and reverses any that have been paid in error
- [ ] D. A `PostToolUse` hook that detects refunds above the limit, then raises an alert to the finance team for review

### Question 7

To support new request types, the team has grown the agent from 4 tools to 18. Tool selection has become less reliable, including for requests that worked before, and no description has changed. Which response best addresses this? **Select 1.**

- [ ] A. Lengthen every description to the same word count, so that no tool stands out over the other ones
- [ ] B. Keep role-specific tool sets, moving specialized groups behind dedicated subagents
- [ ] C. Add a sentence to the system prompt, asking the agent to consider all 18 tools carefully before choosing
- [ ] D. Force a specific tool on each turn with `tool_choice`, chosen by keyword matching on the customer's message

### Question 8

A prerequisite gate blocks `process_refund` until `get_customer` has returned a verified customer ID, and it hasn't failed since launch. An audit now finds a refund paid against an order belonging to a different customer: the verified caller had supplied someone else's order number. What should you change first? **Select 1.**

- [ ] A. Extend the check in code, so that the order's customer ID must match the verified ID in the session
- [ ] B. Add few-shot examples of the agent refusing to act on orders that belong to other customers
- [ ] C. Require the customer to restate their order number twice, and compare both values before the refund runs
- [ ] D. Add an instruction telling the agent to confirm that each order belongs to the person it's speaking to

### Question 9

`lookup_order` returns Unix timestamps and numeric status codes, while `get_customer` returns ISO 8601 dates. The agent sometimes tells customers their return window has passed when it hasn't. The backend teams can't change their APIs this quarter. What's the most reliable fix? **Select 1.**

- [ ] A. Add a `PostToolUse` hook that converts dates and status codes to one readable form before the model sees them
- [ ] B. Add a table to the system prompt that explains each status code and how to convert each date format correctly
- [ ] C. Add a hook on outgoing calls, so that each tool request specifies which date format the tool should use
- [ ] D. Add a subagent whose only job is converting each tool's dates and codes before the main agent reasons about them

### Question 10

Tool descriptions were rewritten last month and are clear. Even so, when customers write "there's a charge on my account I don't recognize," the agent calls `get_customer` and never looks at orders. Your system prompt includes: "Whenever a customer mentions their account, check the customer record first." What should you examine? **Select 1.**

- [ ] A. The length of the descriptions, as they're likely to be too long for the model to apply them consistently
- [ ] B. The need for a routing classifier, because descriptions alone can't reliably separate customer and billing requests
- [ ] C. The system prompt's keyword-sensitive instruction, which is creating an association that overrides the descriptions
- [ ] D. The order of tools in the array, since Claude gives more weight to the tools that are listed first

### Question 11

Your loop uses `max_iterations = 6` as its normal exit, and returns whatever text exists at that point. Conversations with several issues are being cut off with work unfinished. A teammate proposes raising the value to 25, because the longest run seen so far took 19 iterations. How should you respond? **Select 1.**

- [ ] A. Split multi-issue conversations into separate sessions of six iterations, so that each one fits within the limit
- [ ] B. Make `stop_reason` the normal exit, and keep a high cap only as a safeguard that flags the run as incomplete
- [ ] C. Accept 25, since it covers every observed conversation and leaves a reasonable margin for future growth
- [ ] D. Remove all limits on the loop, because any iteration cap is an anti-pattern in a model-driven agent

### Question 12

A customer writes that a sofa arrived damaged, that a delivery fee was charged twice, and that a refund from March never came. The agent handles the three in sequence and its final reply contradicts an earlier one about the same order. Which approach fits the guide's pattern for requests like this? **Select 1.**

- [ ] A. Escalate immediately, because any request that contains more than one concern exceeds what an agent can resolve
- [ ] B. Handle the most valuable concern first, and ask the customer to open new tickets for whatever remains afterward
- [ ] C. Decompose them into distinct items, investigate each in parallel with shared context, then give one unified resolution
- [ ] D. Run each concern in its own new session, and send the customer three separate replies as each one finishes

### Question 13

After long conversations, your summarization step turns "refund of $312.40 promised by 14 May for order 7781" into "customer expects a refund soon." Later replies are vague or wrong on the details. What's the best fix? **Select 1.**

- [ ] A. Stop passing the history to the model, and have the agent ask customers to restate the relevant details
- [ ] B. Instruct the summarizer to try harder to keep the most important facts when it's condensing the conversation
- [ ] C. Keep amounts, dates, order numbers and statuses in a persistent case facts block, outside the summarized history
- [ ] D. Summarize more often, so that each summary covers a shorter stretch of the conversation and loses less of it

### Question 14

Support logs show conversations ending right after the agent says "Let me pull up that order." The same response also carried a `lookup_order` request, but your loop returned the sentence to the customer and stopped, because it treats any text block as a final answer. Which change addresses the cause? **Select 1.**

- [ ] A. Continue running tools while `stop_reason` is `tool_use`, and return to the customer only on `end_turn`
- [ ] B. Add a follow-up call after each response, asking Claude to confirm whether it considers the request complete
- [ ] C. Keep the text check, but add a list of phrases such as "let me" that indicate more work is still coming
- [ ] D. Instruct Claude in the system prompt never to write explanatory text in a response that also requests a tool

### Question 15

Your team shares the MCP configuration for the internal orders server, and it needs an API token. A developer suggests committing the token to the private repository to simplify onboarding. What should you do? **Select 1.**

- [ ] A. Commit `.mcp.json` with `${ORDERS_API_TOKEN}`, and have each developer set the variable in their environment
- [ ] B. Base64-encode the token in `.mcp.json`, so that it can't be read directly by anyone browsing the repository
- [ ] C. Commit `.mcp.json` with the real token, as access to the private repository is already controlled by your company
- [ ] D. Have each developer configure the server separately in `~/.claude.json`, so that no project configuration exists

---

## Scenario 2: Multi-Agent Research System

*Suggested checkpoint: start by minute 30.*

Meridian Insight is building a multi-agent research system on the Claude Agent SDK. A coordinator delegates to four subagents: one searches the web, one analyzes documents, one synthesizes findings and one generates reports. The system's reports must be comprehensive and fully cited, and analysts use them for investment decisions.

### Question 16

Three parallel search subagents, each given "research the European battery market," return mostly the same sources. Total cost has tripled, while coverage has barely improved. What's the most effective first change? **Select 1.**

- [ ] A. Increase the number of subagents, since a larger group should naturally explore more of the available sources
- [ ] B. Have the coordinator give each subagent a distinct subtopic or source type
- [ ] C. Add a deduplication step after the search phase, to remove repeated sources before analysis begins
- [ ] D. Allow the subagents to message one another during their runs, so that each can see what the others found

### Question 17

The coordinator's prompt gives each search subagent 14 exact steps, such as "open the first PDF, read its executive summary, then search for its title." Results are poor whenever a source doesn't look as the steps expect. What's the better prompt design? **Select 1.**

- [ ] A. Add further numbered steps, covering each of the unexpected source types that have been seen so far
- [ ] B. State the research goals and quality criteria, and let each subagent adapt its approach to the sources
- [ ] C. Force the order of the subagent's tools with `tool_choice`, so that the steps can't be skipped or reordered
- [ ] D. Move the 14 steps to the coordinator itself, and have it run them directly rather than delegating them

### Question 18

The search subagent's tool times out on some queries. Today, any timeout immediately fails the subagent, and the coordinator receives only "search failed," even if four of five queries had already succeeded. Which design is best? **Select 1.**

- [ ] A. Retry each failed query indefinitely within the subagent, so that the coordinator never has to handle a failure
- [ ] B. Terminate the whole run when any query fails, so that no report can be generated from incomplete research
- [ ] C. Return the four good results as a complete success, so that the coordinator isn't troubled by one failed query
- [ ] D. Retry transient failures locally, then report unresolved errors with what was attempted and any partial results

### Question 19

The coordinator often sends web pages to `process_file` rather than `process_source`. The two descriptions are nearly identical, both saying the tool "analyzes the provided content." What's the best first fix? **Select 1.**

- [ ] A. Add a routing layer that checks the input type, and selects the right tool before the model is involved
- [ ] B. Merge them into a single `process` tool, and have it work out internally what kind of input it received
- [ ] C. Add an instruction to the coordinator's system prompt asking it to think more carefully about these two tools
- [ ] D. Rename them by purpose, and rewrite the descriptions to state inputs, outputs and when to use each

### Question 20

Citations keep degrading as findings pass from search to analysis to synthesis. Which two changes best preserve attribution across these hops? **Select 2.**

- [ ] A. Require each step to keep and merge claim-to-source mappings when it combines or summarizes findings
- [ ] B. Give the synthesis agent access to the search subagent's conversation history as it writes
- [ ] C. Have each subagent attach a self-reported confidence score to every finding that it returns
- [ ] D. Add a closing bibliography that lists every source consulted at any point during the run
- [ ] E. Pass findings as structured data, with content separate from metadata such as URL, document name and page

### Question 21

For a broad topic, the coordinator emits one Task call, waits for the result, then emits the next, for each of five independent research areas. A run takes about 40 minutes. How should you reduce the elapsed time without losing coverage? **Select 1.**

- [ ] A. Set a `parallel` option in the coordinator's system prompt, so that the Task calls are not run in order
- [ ] B. Start five separate coordinators with one area each, then merge their reports at the end in a new session
- [ ] C. Emit all five Task calls in a single coordinator response, so that they run in parallel
- [ ] D. Combine the five areas into one Task call, so that a single subagent can cover them all together

### Question 22

The document analysis subagent has a general `fetch_url` tool, and sometimes wanders off to unrelated websites during analysis. You want it to load only the documents it has been assigned. Which change is most direct? **Select 1.**

- [ ] A. Log every URL that the subagent visits, and have a person review the list at the end of each week
- [ ] B. Add a hook that summarizes each fetched web page, to reduce how much context these visits consume
- [ ] C. Replace `fetch_url` with a constrained `load_document` tool that validates document URLs
- [ ] D. Add a sentence to the subagent's system prompt, saying that it should only fetch relevant documents

### Question 23

Each subagent currently handles its own failures differently, and two have begun calling each other directly to save time. Investigating a bad report now means reading four separate logs, with no single record of what information went where. Which change best addresses this? **Select 1.**

- [ ] A. Add a shared memory store that all subagents can read and write, so that each can see the others' work
- [ ] B. Route all subagent communication through the coordinator, which also handles errors and information flow
- [ ] C. Standardize each subagent's log format, and leave their direct calls in place to protect the time saved
- [ ] D. Add a monitoring subagent that subscribes to each of the conversations and reports anything unusual it finds

### Question 24

The synthesis subagent's prompt reads: "Combine the findings from the earlier agents into a report, and cite your sources." Its output is generic, and contains no real citations, even though the search and analysis subagents found good material. What's the most likely cause? **Select 1.**

- [ ] A. The prompt is too short, and needs a longer description of how the citations should be formatted
- [ ] B. The synthesis subagent needs access to web search, so that it can locate the citations itself
- [ ] C. The earlier subagents' memory expired before synthesis began, because too much time passed between them
- [ ] D. Subagents don't inherit parent context, so the findings and sources were never in its prompt

### Question 25

Two credible reports give different figures for market growth: 12% in one and 19% in the other. The synthesis subagent reports one figure with no mention of the other. The reports were published 18 months apart. How should the pipeline handle this? **Select 1.**

- [ ] A. Keep both values with their sources and dates, and annotate the difference rather than selecting one
- [ ] B. Discard both figures from the report, because credible sources that disagree can't be relied on at all
- [ ] C. Always select the most recent figure, and remove the older one, in order to keep the report easy to read
- [ ] D. Report the average of the two figures, since this gives a balanced estimate that reflects both sources

### Question 26

Analysts ask open questions such as "why did margins fall across this sector?" Your fixed four-step pipeline keeps doing work that earlier findings have shown to be irrelevant, and misses leads that those findings reveal. Which approach fits better? **Select 1.**

- [ ] A. A longer fixed pipeline with extra steps, added to cover the leads that were missed in earlier runs
- [ ] B. An adaptive plan that generates subtasks from what's discovered at each step
- [ ] C. A single large prompt that asks one agent to answer the whole question in one pass, with all sources attached
- [ ] D. A fixed pipeline for every question, with its results reviewed by an analyst before each report goes out

### Question 27

Reviewing logs, you find the synthesis subagent's round trips through the coordinator are mostly quick checks of a date or figure, with a minority needing real research. Latency is a growing concern, and the team wants synthesis to stay focused. What's the most appropriate change? **Select 1.**

- [ ] A. Give synthesis a narrow `verify_fact` tool for quick lookups, and keep routing bigger questions through the coordinator
- [ ] B. Give synthesis all of the web search tools, so that it can resolve every question without any round trips
- [ ] C. Have the search subagent run in advance and collect every fact that synthesis could possibly need to verify
- [ ] D. Batch all of the checks until the end of synthesis, then send them to the coordinator as one large request

### Question 28

Before starting any analysis, agents make many search and list calls just to learn which sector reports exist in your internal archive, which is served through an MCP server. What's the best way to reduce these calls? **Select 1.**

- [ ] A. Add a sentence to the prompt telling the agents to explore the archive less often when they begin work
- [ ] B. Increase the agents' iteration caps, so that exploration no longer crowds out their other analysis work
- [ ] C. Create one MCP tool per report, so that each report's name is visible to the agents in the tool list
- [ ] D. Expose the archive's catalog as an MCP resource, so that agents can see what's available

### Question 29

An analyst asks the system to summarize one annual report that's already attached. Logs show the coordinator still ran web search, document analysis, synthesis and report generation, in that order, as it does for every request. What should change? **Select 1.**

- [ ] A. Have the coordinator analyze each query's requirements and invoke only the subagents it needs
- [ ] B. Have each subagent look at the request when it starts, and exit straight away if it isn't needed
- [ ] C. Add a fifth subagent that decides which of the other four subagents should be skipped on each request
- [ ] D. Remove the web search subagent, as analysts most often supply their own source documents to the system

### Question 30

A single `analyze_document` tool is used for pulling figures, for writing summaries and for checking claims. Its output shape varies, and downstream steps keep failing to parse it. What's the most appropriate design change? **Select 1.**

- [ ] A. Instruct the model to always return the same output shape, however the tool happens to have been used
- [ ] B. Split it into purpose-specific tools with defined contracts, such as `extract_data_points` and `verify_claim_against_source`
- [ ] C. Keep one tool, and add a free-text `mode` parameter, so that callers can describe what they'd like it to do
- [ ] D. Add a post-processing step that tries to detect which kind of output was produced, then parses it accordingly

---

## Scenario 3: Code Generation with Claude Code

*Suggested checkpoint: start by minute 60.*

Copperline is a 30-person engineering team with a monorepo containing `web/`, `api/` and `data/` packages. You use Claude Code for code generation, refactoring, debugging and documentation. You're responsible for the team's Claude Code setup, including `CLAUDE.md` files, custom commands and skills, and for deciding when work should begin in plan mode.

### Question 31

After three hours exploring a legacy `data/` package, Claude's answers have become inconsistent, and it now refers to "typical ETL patterns" rather than the specific classes it found earlier. The exploration isn't finished. What's the best way to continue? **Select 1.**

- [ ] A. Start a blank session, and repeat the whole exploration from the beginning with no notes carried over
- [ ] B. Keep going in this session, and ask Claude to pay closer attention to the classes it found earlier
- [ ] C. Record key findings in a scratchpad file, and refer back to it for subsequent questions
- [ ] D. Raise the session's output token limit, so that Claude has more room to give specific answers

### Question 32

Terraform files are in `infra/`, `web/deploy/` and `data/jobs/`, and they all need the same conventions, which aren't relevant to other work. A teammate proposes a `CLAUDE.md` in each of these directories. What's a better approach? **Select 1.**

- [ ] A. A user-level rule on the machine of each developer who regularly works with Terraform
- [ ] B. A section in the root `CLAUDE.md`, with a heading saying that it applies only to Terraform files
- [ ] C. A rule file in `.claude/rules/` with `paths: ["**/*.tf"]` in its YAML frontmatter
- [ ] D. A skill containing the conventions, which developers run before they edit any Terraform file

### Question 33

Your team's `/review` command works well, but you'd like a stricter version for your own use, without affecting anyone else or leaving extra files in the repository. What should you do? **Select 1.**

- [ ] A. Add your stricter requirements to the project's `CLAUDE.md`, in a section that's marked as personal
- [ ] B. Add a second command to `.claude/commands/`, with a name that shows it's for your own personal use
- [ ] C. Edit the team's command in your working copy, and take care to leave it out of each commit you make
- [ ] D. Create a personal variant under `~/.claude/skills/`, with a different name

### Question 34

You've described how API error logs should be reformatted in a long paragraph, but each run interprets some part of it differently. What's the most effective next step? **Select 1.**

- [ ] A. Repeat the paragraph at both the start and the end of your message, for extra emphasis
- [ ] B. Ask Claude to infer the format from how other open-source projects usually handle their error logs
- [ ] C. Provide two or three concrete examples of input with the exact expected output
- [ ] D. Rewrite the paragraph in much greater detail, covering every interpretation that you've seen so far

### Question 35

A `/draft-changelog` skill should be able to read and write files, but should never run shell commands, after one incident where it ran a destructive script. According to the exam guide, which frontmatter should you configure? **Select 1.**

- [ ] A. `context: fork`, so that any shell commands that it runs can't affect the main session
- [ ] B. `paths`, so that the skill only applies when the developer is editing a changelog file
- [ ] C. `argument-hint`, so that developers must confirm each shell command before it's allowed to run
- [ ] D. `allowed-tools`, listing only the file tools that the skill needs

### Question 36

Each package's maintainers know which of the company's 12 standards files apply to their code. Loading all 12 everywhere is wasting context, and copies pasted into each package have started to drift. What's the best way to give maintainers control? **Select 1.**

- [ ] A. Ask each maintainer to keep their package's standards in their own user-level `CLAUDE.md` file
- [ ] B. Have each package's `CLAUDE.md` use `@import` for just the standards that its maintainers select
- [ ] C. Merge all 12 files into the root `CLAUDE.md`, and let Claude work out which standards apply to each task
- [ ] D. Build a script that generates a combined standards file per package, and run it when a standard changes

### Question 37

Two developers run the same task in `api/`, and Claude follows the team's error-handling standard for one of them but not for the other. Both are on the latest commit. What's the most direct way to begin diagnosing this? **Select 1.**

- [ ] A. Run `/memory` in both sessions, and compare which memory files are loaded
- [ ] B. Reinstall Claude Code on the affected developer's machine, then repeat the same task again
- [ ] C. Compare the Git history of each `CLAUDE.md` file, to see when the standard was last changed
- [ ] D. Ask Claude in each session to describe what it knows about the error-handling standard

### Question 38

Fixing input validation broke the error messages, and fixing those then broke the audit log. All three behaviors share one error object. You've been sending one issue per message, and each fix causes a new regression. What should you do? **Select 1.**

- [ ] A. Start a new session for each fix, so that no earlier fix can influence the one after it
- [ ] B. Disable the audit log tests until the first two issues have been fixed, then re-enable them afterward
- [ ] C. Continue one issue at a time, as these regressions should reduce as each of the fixes accumulates
- [ ] D. Describe all three issues together in one detailed message, and ask for a coherent fix

### Question 39

Which two tasks should begin in plan mode? **Select 2.**

- [ ] A. Choosing between two caching approaches that have different infrastructure requirements
- [ ] B. Fixing a bug in one file, using a stack trace that identifies the failing line
- [ ] C. Adding a date validation check to one function, with the rule already agreed
- [ ] D. Correcting a misspelled label on a page, after a report from the design team
- [ ] E. Replacing an HTTP client library used across 45 files in three packages

### Question 40

You're about to investigate a second phase of a large migration with several subagents. The first phase produced findings that these subagents will need, but your main session is getting long. What's the best way to set them up? **Select 1.**

- [ ] A. Summarize the first phase's key findings, and inject that summary into each subagent's initial context
- [ ] B. Run the second phase in the main session, so that the earlier findings stay available in its context
- [ ] C. Paste the entire main session transcript into each subagent's prompt, so that nothing can be missed
- [ ] D. Spawn the subagents with no background, since they can read the main session's history whenever they need it

### Question 41

A refactor across two packages has stalled: exploring the code has consumed nearly all of the main session's context before a single file has been changed. Which earlier decision would have avoided this? **Select 1.**

- [ ] A. Starting with direct execution, so that the refactor could proceed with no exploration of the codebase
- [ ] B. Asking Claude to read each file a second time, so that the most important facts are better retained
- [ ] C. Moving the discovery findings into the system prompt, where they'd no longer count against the context
- [ ] D. Running discovery in the Explore subagent, and keeping only its summary in the main session

### Question 42

You're deciding how to store two pieces of guidance: "all services use the shared logger" and a long procedure for cutting a quarterly release. Which assignment is right? **Select 1.**

- [ ] A. Both go in `CLAUDE.md`, as both are team standards that should be available at all times
- [ ] B. Both go in skills, so that neither takes up context until a developer has chosen to invoke it
- [ ] C. The logger rule goes in `CLAUDE.md`, and the release procedure goes in a skill
- [ ] D. The logger rule goes in a skill, and the release procedure goes in `CLAUDE.md`, because it's the longer one

### Question 43

Your `/map-dependencies` skill prints thousands of lines of analysis, and developers notice that later answers in the session get worse after running it. They still want its conclusions in the session. Which change fits best? **Select 1.**

- [ ] A. Add `argument-hint` to the skill's frontmatter, so that developers can ask it for shorter output
- [ ] B. Move the skill's instructions to `CLAUDE.md`, so that the analysis is available from the start of a session
- [ ] C. Add `context: fork` to the skill's frontmatter, so that the working stays out and only the result returns
- [ ] D. Set `allowed-tools` in the skill's frontmatter, so that it can read fewer of the project's files

### Question 44

Your exploration of the `api/` package has filled the session with verbose search output, but the findings so far are still valid and you want to continue in this session. Which built-in option is intended for this? **Select 1.**

- [ ] A. Run `--resume` to reload the session with less output
- [ ] B. Add `context: fork` to the project's `CLAUDE.md` file
- [ ] C. Run `/memory` to remove old output from the session
- [ ] D. Run `/compact` to reduce context usage

### Question 45

A developer who joined last week reports that Claude ignores the team's commit message format. Everyone who has been on the team longer gets the right format. The repository's `CLAUDE.md` doesn't mention commit messages. What's the most likely explanation? **Select 1.**

- [ ] A. The new developer has to set a configuration flag, before Claude Code will load any instructions
- [ ] B. The format is in other developers' `~/.claude/CLAUDE.md` files, which aren't shared through version control
- [ ] C. Claude Code learns commit formats by observing Git history, and hasn't yet seen enough of the developer's commits
- [ ] D. Project-level `CLAUDE.md` files are only applied after a developer has been active in a repository for some time

---

## Scenario 4: Structured Data Extraction

*Suggested checkpoint: start by minute 90.*

Alder Ledger is building a system that extracts structured data from supplier invoices, receipts and contracts using Claude. Each document type has its own JSON schema, and results flow into an accounting system. Documents vary widely in layout and quality, and some fields are absent from some documents.

### Question 46

Extraction works on invoices with a clear table of charges, but returns null for required fields when charges appear in narrative paragraphs or footnotes. The values are actually in the documents. What's the most effective change? **Select 1.**

- [ ] A. Add a stronger instruction stating that all the required fields must be found in every document
- [ ] B. Add a retry that repeats the same request whenever one of the required fields comes back null
- [ ] C. Add few-shot examples showing correct extraction from those varied document structures
- [ ] D. Make these fields optional, so that the null values no longer cause any validation failures

### Question 47

Reviewers can check only a fraction of documents each day. You want to direct their attention where it's most useful. Which approach fits? **Select 1.**

- [ ] A. Assign documents to reviewers at random, as this removes any possible bias from the routing
- [ ] B. Calibrate field-level confidence on a labeled set, and route low-confidence or contradictory documents first
- [ ] C. Use document-level confidence as the model reports it, with no calibration against known results
- [ ] D. Route the largest documents first, since they contain the most fields that could be wrong

### Question 48

You're about to run a new extraction prompt over a backlog of 400,000 documents with the Message Batches API. What should you do first to reduce wasted cost? **Select 1.**

- [ ] A. Refine the prompt on a representative sample, to improve the first-pass success rate
- [ ] B. Switch to synchronous requests, because batches can't be used for structured extraction work
- [ ] C. Run the prompt on the 100 largest documents, since these are the most likely to cause problems
- [ ] D. Submit the full backlog at once, then fix the prompt based on whichever documents fail

### Question 49

A nightly batch of 20,000 documents returns with 140 failures, most of them because the documents exceeded context limits. What's the most efficient way to handle them? **Select 1.**

- [ ] A. Resubmit the failures unchanged, because batch failures are normally temporary and tend to resolve
- [ ] B. Resubmit the whole batch of 20,000, so that all of the results are produced under the same conditions
- [ ] C. Process the 140 documents by hand, because a document that fails once can't be processed by the API
- [ ] D. Resubmit only the failures, identified by `custom_id`, after chunking the oversized documents

### Question 50

An extraction fails your validation because a contract's end date came back as "Q3 2027," and you want a retry to correct it. What should the retry request include? **Select 1.**

- [ ] A. The validation error alone, as the model will remember the document and its earlier answer from the first call
- [ ] B. The original document, the failed extraction, and the specific validation error
- [ ] C. A general instruction to be more careful with dates, along with a reminder that accuracy is important
- [ ] D. The original document alone, so that the model can start again without being influenced by its previous attempt

### Question 51

Ambiguous cases, such as a figure that could be either a fee or a deposit, are classified inconsistently. Detailed instructions haven't helped. What's the best way to use few-shot examples here? **Select 1.**

- [ ] A. Add examples of clear-cut cases only, so that the model sees the correct output format
- [ ] B. Add the examples without any explanations, so that the model doesn't copy the reasoning
- [ ] C. Add 30 examples covering every document layout that has been seen in production so far
- [ ] D. Add 2–4 targeted examples of ambiguous cases, showing the reasoning for the chosen value

### Question 52

Your system reports 97% accuracy overall, and the team proposes removing human review for high-confidence extractions. Handwritten receipts make up 4% of the volume. What should you do before reducing review? **Select 1.**

- [ ] A. Remove review now, because an overall accuracy of 97% exceeds the threshold for most finance processes
- [ ] B. Ask the model to rate its confidence on each receipt, then remove review for those rated above 90%
- [ ] C. Measure accuracy by document type and field, to check that the aggregate isn't hiding weak segments
- [ ] D. Keep every document under human review, because no automated system can be trusted for financial data

### Question 53

After three retries, the `tax_id` field is still failing validation on some invoices. Inspection shows that these invoices say "see master agreement," and the agreement isn't part of the input. What should you do? **Select 1.**

- [ ] A. Increase the retries to ten, with more forceful instructions to find the missing tax ID in the document
- [ ] B. Add few-shot examples of tax IDs, so that the model can recognize them more easily in these invoices
- [ ] C. Stop retrying, since retries can't recover information absent from the provided document
- [ ] D. Relax the validator, so that any plausible-looking value is accepted for the `tax_id` field on these invoices

### Question 54

Some invoices have line items that don't add up to the printed total, and you need to know whether the source or the extraction is at fault. Which schema design helps most? **Select 1.**

- [ ] A. Extract `stated_total` and `calculated_total`, with a `conflict_detected` boolean
- [ ] B. Extract only the `stated_total`, and treat it as authoritative in all cases for the accounting system
- [ ] C. Add a `confidence` field for the total, and accept any invoice whose confidence is above a threshold
- [ ] D. Extract only the `calculated_total`, since a computed value is more trustworthy than a printed value

### Question 55

Since moving to tool use with a strict JSON schema, parsing errors have disappeared. Finance then reports invoices where line items don't sum to the total, and vendor names placed in the address field. A teammate says the schema should have prevented this. What's accurate? **Select 1.**

- [ ] A. The schema must be incomplete, because a strict schema would have stopped both of these errors
- [ ] B. These errors show that free-text JSON would be more reliable than tool use with a schema
- [ ] C. The model isn't honoring the schema, so the extraction tool should be forced on every call
- [ ] D. Tool use removes syntax errors, while semantic errors like these need separate validation

### Question 56

Your `expense_category` enum has eight values, but new categories keep appearing in documents. Items are being forced into the closest value, and some ambiguous items are being guessed. Which two schema changes address this? **Select 2.**

- [ ] A. Remove the enum and let the model write any category as free text
- [ ] B. Add more of the likely enum values each time that a new category appears
- [ ] C. Add an `"unclear"` value for items that are truly ambiguous
- [ ] D. Add an `"other"` value, along with a free-text detail field
- [ ] E. Make the field required, so that a category is produced for every item

### Question 57

Documents must be processed within 30 hours of arrival. You plan to use the Message Batches API, which can take up to 24 hours per batch. Documents collect between submissions. Which submission schedule meets the requirement? **Select 1.**

- [ ] A. Every 8 hours
- [ ] B. Every 4 hours
- [ ] C. Every 24 hours
- [ ] D. Every 12 hours

### Question 58

`purchase_order_number` is a required string in your schema. Many receipts don't have one, and the extraction is inventing plausible-looking numbers for them. What should you change? **Select 1.**

- [ ] A. Add few-shot examples of receipts where a purchase order number is clearly printed on the document
- [ ] B. Make the field optional or nullable, so that the model can report that the value is absent
- [ ] C. Add a validation rule that checks the number's format, and retry the extraction whenever it fails
- [ ] D. Keep the field required, and instruct the model to use its best estimate when no number is present

### Question 59

Review has been reduced for high-confidence extractions. How should you keep measuring error rates, and catch new kinds of errors, in the documents that no longer get reviewed? **Select 1.**

- [ ] A. Review only the documents the model has marked as low confidence
- [ ] B. Use stratified random sampling of high-confidence extractions
- [ ] C. Compare today's confidence scores with last month's average confidence scores
- [ ] D. Wait for complaints from finance, and investigate the documents they name

### Question 60

A single endpoint receives invoices, receipts and contracts, and the type isn't known in advance. Each type has its own extraction tool and schema. You must always get structured output, not a text reply. Which configuration fits? **Select 1.**

- [ ] A. `tool_choice: "any"`, with one extraction tool per document type
- [ ] B. A forced `tool_choice` naming the invoice tool, because invoices are the most common document type
- [ ] C. One combined tool with all fields optional, called with `tool_choice: "auto"` for all three of the types
- [ ] D. `tool_choice: "auto"`, with an instruction in the prompt to call whichever extraction tool fits

---

**End of simulation.** Save this file and ask Claude to evaluate it.
