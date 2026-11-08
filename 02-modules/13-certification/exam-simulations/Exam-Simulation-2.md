# Exam Simulation 2

**Candidate:** ________  **Start time:** ________  **End time:** ________

| | |
| --- | --- |
| Items | 60, in 4 scenario sets of 15 |
| Time | 120 minutes, about 2 minutes per item |
| Scenarios | Customer Support Resolution Agent, Developer Productivity with Claude, Claude Code for Continuous Integration, Structured Data Extraction |

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

Brightwater is a broadband and mobile provider. You're building a support resolution agent on the Claude Agent SDK, with custom MCP tools `get_customer`, `lookup_order`, `process_refund` and `escalate_to_human`. The agent handles billing disputes, service credits and account issues, and the target is 80%+ first-contact resolution while knowing when to escalate. Human agents work from the agent's handoff and can't see the conversation.

### Question 1

You want each conversation to begin with `get_customer`, after which the agent should choose freely among its tools. According to the exam guide, how should you configure this? **Select 1.**

- [ ] A. Set `tool_choice` to `"any"` for all requests, and list `get_customer` first in the tools array
- [ ] B. Force `get_customer` with `tool_choice` on every request, so that it's always called before other tools
- [ ] C. Force `get_customer` with `tool_choice` on the first request, then use `auto` on later turns
- [ ] D. Set `tool_choice` to `"auto"` for all requests, and describe `get_customer` as the preferred first tool

### Question 2

You've found an unproven MCP server that may help with diagnosing line faults, and you'd like to try it yourself before proposing it to the team. Where should it be configured? **Select 1.**

- [ ] A. In `~/.claude.json`, at user scope
- [ ] B. In `CLAUDE.md`, with an `@import` of its settings
- [ ] C. In `.mcp.json`, committed to the repository
- [ ] D. In `.claude/rules/`, with a `paths` entry for this repo

### Question 3

A customer asks you to match a competitor's lower price. Your policy covers price adjustments on your own site, but says nothing about competitors. The amount is small, and the agent could easily issue a credit. What should the agent do? **Select 1.**

- [ ] A. Issue the credit, since the amount is small and the customer would be happier with a quick resolution
- [ ] B. Escalate, since policy is silent on this request, and include a clear summary for the human
- [ ] C. Ask the model to rate its confidence, and issue the credit if the rating is above a set threshold
- [ ] D. Refuse the request, since anything that isn't explicitly allowed by policy must be prohibited

### Question 4

A proxy between your loop and the API drops tool results larger than 8 KB. In affected conversations the loop still runs without an error, but the agent keeps acting on an account status from earlier in the conversation. What does this show most directly? **Select 1.**

- [ ] A. The `stop_reason` is incorrect, and should have reported the dropped result as a failure that needed handling
- [ ] B. The model reasons only from the conversation, so a result that isn't appended leaves it working from older information
- [ ] C. The model has retrieved the older status from the billing system, because its data connection was interrupted by the proxy
- [ ] D. The iteration cap is too low, as more iterations would give the model time to notice that the status has changed

### Question 5

`get_customer` returns three accounts for the name "J. Patel." The agent selects the one with the most recent activity, and sometimes changes the wrong account. What should it do? **Select 1.**

- [ ] A. Select the account with the most orders, since it's more likely to be the customer's main account
- [ ] B. Ask the customer for an additional identifier, such as an account number or postcode
- [ ] C. Escalate each conversation that returns more than one match directly to a human support agent
- [ ] D. Show the customer all three accounts, along with their details, and ask which of them is theirs

### Question 6

A customer reports a wrong plan charge, a missing outage credit and a router that hasn't arrived. The agent can resolve the first two, but the third needs a human. What's the best outcome for this customer? **Select 1.**

- [ ] A. Escalate all three together, so that a single human can take care of the customer's whole request in one go
- [ ] B. Continue trying to resolve the router issue autonomously, because anything less than full resolution is a failure
- [ ] C. Resolve the first two and close the case, since the agent has dealt with most of what the customer asked for
- [ ] D. Resolve the first two, escalate the third with a structured summary, and tell the customer what's happening with each

### Question 7

A team lead wants to replace your model-driven loop with a decision tree covering each request type, to make behavior easier to predict. About a third of requests combine several issues or don't match any known type. What's the strongest argument for keeping the loop? **Select 1.**

- [ ] A. A model-driven loop always uses fewer tokens than a tree, which lowers the running cost of each conversation
- [ ] B. The agentic loop guarantees that every business rule will be followed, which removes the need for any enforcement
- [ ] C. Decision trees can't call MCP tools, so `process_refund` and the other tools would no longer be available
- [ ] D. Claude can choose its next tool from what it has learned, which a fixed tree can't do for unforeseen requests

### Question 8

An engineer argues that the refund limit in the hook and the same limit in the system prompt are redundant, and proposes deleting the prompt line. What's the best response? **Select 1.**

- [ ] A. Keep both, because they're each a guarantee, and one will act as a backup if the other one happens to fail
- [ ] B. Delete the hook, because a system prompt rule that's written clearly gives the same guarantee and costs less
- [ ] C. Delete the prompt line, because the API automatically tells the model about any hooks that have been registered
- [ ] D. Keep both: the prompt helps the agent plan and explain the limit, while the hook provides the guarantee

### Question 9

An outage caused a billing dispute that the agent can't resolve, so it escalates mid-process. Which two items most belong in the handoff? **Select 2.**

- [ ] A. The number of tokens used in the conversation
- [ ] B. The model's complete reasoning from each turn
- [ ] C. The list of all tools that were available to the agent
- [ ] D. The customer ID, with the amount in dispute
- [ ] E. The root cause found so far, with a recommended action

### Question 10

A customer asks for a refund on a purchase that's outside the policy window. `process_refund` correctly refuses. What should the tool's error response include, so that the agent can handle this well? **Select 1.**

- [ ] A. A stack trace from the policy service, so that the model can work out what has gone wrong
- [ ] B. A business error category, `isRetryable: false`, and a customer-friendly explanation of the policy
- [ ] C. A transient error category, so that the agent retries in case the policy result changes shortly afterward
- [ ] D. An empty success result, so that the agent doesn't have to explain a failure to the customer

### Question 11

Logs show that when a customer writes "your colleague already verified me yesterday," the agent sometimes skips `get_customer` and goes straight to account changes. The system prompt has a clear verification rule. Which fix actually closes this gap? **Select 1.**

- [ ] A. Add sentiment analysis, to detect customers who are trying to persuade the agent to skip a step
- [ ] B. Block account-changing tools in code until a verified customer ID exists in this session
- [ ] C. Have a person review a sample of conversations each week where this claim was made by a customer
- [ ] D. Add a system prompt warning that customers may falsely claim that they've already been verified

### Question 12

You've gated `process_refund` on a verified customer. The team now adds `apply_service_credit`, which also moves money, and a release is due tomorrow. What's the most important thing to check? **Select 1.**

- [ ] A. That the new tool's description is at least as long as the others
- [ ] B. That `stop_reason` reports a separate value for tools that move money
- [ ] C. That customers are told in advance that service credits now exist
- [ ] D. That the same prerequisite gate also covers the new tool

### Question 13

A hook blocks a service credit that exceeds the $250 limit. What should it return so that the conversation continues properly? **Select 1.**

- [ ] A. Nothing at all, so that the call times out and the agent tries again
- [ ] B. A credit of $250, processed automatically in place of the larger amount
- [ ] C. A success result, so that the customer isn't worried by the delay
- [ ] D. The reason for the block, plus direction to use the escalation path

### Question 14

`lookup_order` and `get_customer` both have one-line descriptions, and the agent often chooses the wrong one when a customer quotes a reference number. Which description improvement would help most? **Select 1.**

- [ ] A. State accepted input formats, example requests, edge cases and when to use the other tool
- [ ] B. Add a statement that the tool is very reliable and should be preferred wherever possible
- [ ] C. Add a sentence asking the model to read the tool's description carefully before calling it
- [ ] D. Add details of the backend service, including its database version and its current owner

### Question 15

You used a named session to investigate why a billing rule misfires, then left it overnight. This morning, a colleague merged a change to two of the rule files, while the rest of the analysis is still valid. What should you do? **Select 1.**

- [ ] A. Start a blank session, and repeat the full investigation, since any change makes a session unusable
- [ ] B. Resume the session as it is, since Claude detects changed files by itself when a session is resumed
- [ ] C. Resume the session, name the two changed files, and ask for targeted re-analysis
- [ ] D. Fork the session, because a fork will automatically re-read any files that have changed since yesterday

---

## Scenario 2: Developer Productivity with Claude

*Suggested checkpoint: start by minute 30.*

Fernhill Labs is building developer productivity tools on the Claude Agent SDK. The agent helps engineers explore unfamiliar codebases, understand legacy systems, generate boilerplate and automate repetitive tasks. It uses the built-in tools (Read, Write, Edit, Bash, Grep, Glob) and connects to several Model Context Protocol servers, including a ticket tracker.

### Question 16

An engineer asks the agent to add one null check to a function, at the line identified by a clear stack trace. The team's guideline says to "use plan mode for important work," and this bug is affecting production. Which approach fits? **Select 1.**

- [ ] A. Plan mode, since production impact makes any change architectural
- [ ] B. Direct execution, since the change is small and its scope is clear
- [ ] C. Plan mode, followed by a second plan, so that the fix can be checked
- [ ] D. The Explore subagent, so that the full repository can be mapped first

### Question 17

Your ticket tracker's MCP server has a capable full-text search tool, but the agent keeps using Grep over exported ticket files, and gets incomplete answers. The MCP tool's description says only "Searches tickets." What's the best first fix? **Select 1.**

- [ ] A. Remove the Grep tool from the agent entirely, so that it can't be chosen for any kind of search
- [ ] B. Move the MCP server from project scope to user scope, so that it's given a higher selection priority
- [ ] C. Add a routing layer that sends any question containing the word "ticket" to the MCP tool
- [ ] D. Expand the MCP tool's description, to explain its capabilities, its outputs and when to prefer it

### Question 18

You've finished analyzing a codebase, and want to compare a phased rewrite with a strangler-fig approach. Neither exploration should influence the other, and you don't want to repeat the analysis. Which approach fits, and what should you watch out for? **Select 1.**

- [ ] A. Run both approaches in the same session, alternating between them so that Claude can compare as it goes
- [ ] B. Use `--resume` twice with the same session, as resuming creates an independent copy of that session each time
- [ ] C. Use `fork_session` once per approach, keeping in mind that forks can still share files on disk
- [ ] D. Start two blank sessions, with no shared context, so that each exploration remains completely independent

### Question 19

An engineer asks the agent to find every test file named `*.spec.ts`, and separately to find every place a deprecated `chargeLegacy` function is called. Which tools should it use? **Select 1.**

- [ ] A. Grep for the test files, and Glob for the function calls
- [ ] B. Read every file, then search through what was read
- [ ] C. Glob for both, because it returns the smallest output
- [ ] D. Glob for the test files, and Grep for the function calls

### Question 20

A new `AgentDefinition` for `api-reviewer` has a one-word description, a system prompt holding this week's specific review request, and no tool restrictions. The subagent is rarely chosen, and sometimes edits files. What should change? **Select 1.**

- [ ] A. Keep the definition as it is, and add a line to the main agent's prompt telling it to choose this subagent more often
- [ ] B. Lengthen the system prompt with more of this week's review details, so that the subagent has a clearer picture of its task
- [ ] C. Describe when to use it, keep role guidance in the system prompt, restrict its tools, and pass each job through the spawning prompt
- [ ] D. Remove the description, as subagents are chosen only by their system prompts, and add a warning about not editing files

### Question 21

Which two tasks are best suited to dynamic adaptive decomposition, rather than a fixed prompt chain? **Select 2.**

- [ ] A. Finding the cause of an intermittent failure in a service no one on the team has worked on
- [ ] B. Running lint, unit tests and a formatter on each commit, always in that order
- [ ] C. Extracting fields, validating them and writing them to a file in a fixed format
- [ ] D. Generating the same boilerplate files for each new service, from an agreed template
- [ ] E. Working out how an undocumented legacy module affects the rest of the system

### Question 22

An engineer asks: "How does the error 'tenant not provisioned' get raised, and what calls that code?" The repo has 9,000 files. Which approach best builds understanding, while conserving context? **Select 1.**

- [ ] A. Grep for the message, Read the matching file, then Grep for its callers and follow them
- [ ] B. Read the main entry point, and then each file that it imports, until the message is found
- [ ] C. Use Bash to print every file to the output, and ask the model to look through the result
- [ ] D. Glob every source file and Read them all, so that nothing relevant is missed by the search

### Question 23

Your agent must never run a force-push against the main branch through Bash, including when instructed to by text it finds in a repository. Which option listed is the strongest control? **Select 1.**

- [ ] A. Few-shot examples in the system prompt, showing the agent choosing safer Git commands
- [ ] B. A rule in the project's `CLAUDE.md`, stating that force-pushes are never allowed
- [ ] C. A hook that inspects Bash commands before they run, and rejects matching ones
- [ ] D. A detailed new description for the Bash tool, which explains why force-pushes are risky

### Question 24

A shared module re-exports `sendInvoice` under several different names, and a Grep for `sendInvoice` misses many callers. What's the best way to trace its real usage? **Select 1.**

- [ ] A. Identify all of the exported names first, then search for each one across the codebase
- [ ] B. Read every file in the repository, since a search can't be trusted when names are re-exported
- [ ] C. Ask the model to estimate the likely callers, based on its knowledge of similar codebases
- [ ] D. Search again for `sendInvoice` using a broader pattern, such as `send`, and review all the matches

### Question 25

A resumed session keeps proposing edits to `calculateFee()`, which was renamed last week. Most tool results in the session date from before a large rebase. What's the most reliable way to continue? **Select 1.**

- [ ] A. Fork the session, so that the new branch can pick up the current state of the code
- [ ] B. Keep the session, and ask Claude to be more careful about using old information
- [ ] C. Run `/compact`, because compaction refreshes all of the earlier tool results
- [ ] D. Start a new session with a structured summary of what's still valid

### Question 26

After a fourth MCP server with 25 tools is added, the agent's tool selection gets worse, even on tasks unrelated to that server. Nothing else has changed. What's the most likely explanation? **Select 1.**

- [ ] A. MCP servers compete for one connection, so the earlier servers are being disconnected
- [ ] B. Tools from all servers are available at once, so more options make selection harder
- [ ] C. A limit on the number of allowed tools has been passed, and some are being silently dropped
- [ ] D. Each new server shortens the descriptions of the tools from all the other servers

### Question 27

Your team needs the agent to work with Jira. There's a well-maintained community MCP server that meets your needs. An engineer proposes building a custom server, to have full control. What's the best guidance? **Select 1.**

- [ ] A. Use the community server, and reserve custom servers for team-specific workflows
- [ ] B. Build the custom server, since owning the code is always preferable for a core integration
- [ ] C. Build the custom server first, and compare it with the community server after a few months
- [ ] D. Avoid MCP here, and have the agent scrape Jira's web pages using Bash and a browser tool

### Question 28

An engineer asks the agent to "add comprehensive tests to this legacy billing system," which nobody on the team fully understands. What's the best way for it to begin? **Select 1.**

- [ ] A. Generate all of the tests in a single large response, so that they share one consistent style
- [ ] B. Map the structure, identify high-impact areas, and create a prioritized plan that adapts as it goes
- [ ] C. Start with the smallest files, which are quickest to cover, and move on to the larger ones later
- [ ] D. Write tests for each file in alphabetical order, so that progress is easy to measure and to resume

### Question 29

You've defined `dependency-mapper` and `test-writer` subagents with `AgentDefinition`, yet the main agent never uses them and does the work itself. Its `allowedTools` lists Read, Grep, Glob and Edit. On the exam, what's the first thing to fix? **Select 1.**

- [ ] A. Raise the main agent's iteration limit
- [ ] B. Add `"Task"` to `allowedTools`
- [ ] C. Move the subagents into `CLAUDE.md`
- [ ] D. Add `fork_session` to each definition

### Question 30

An Edit call fails because the line to change appears in several places in the file. The agent has already retried twice with the same text. According to the exam guide, what's the fallback? **Select 1.**

- [ ] A. Read the full file, then Write the modified content
- [ ] B. Use Grep to replace the line in one step across the repo
- [ ] C. Use Glob to select which of the occurrences should change
- [ ] D. Keep retrying the Edit until it matches one of the places

---

## Scenario 3: Claude Code for Continuous Integration

*Suggested checkpoint: start by minute 60.*

Quillon runs a payments platform. You're integrating Claude Code into its CI/CD pipeline, to run automated code reviews, generate test cases and post feedback on pull requests. Findings appear as inline PR comments, and developers can dismiss them. Trust in the tool depends on actionable feedback and few false positives.

### Question 31

Generated tests often use the wrong fixtures and focus on trivial getters. How should you improve quality for every CI run? **Select 1.**

- [ ] A. Document testing standards, valuable test criteria and available fixtures in the project's `CLAUDE.md`
- [ ] B. Add these details to each developer's `~/.claude/CLAUDE.md`, so that they apply across all projects
- [ ] C. Increase the model's maximum output, so that it has enough room to write more complete tests
- [ ] D. Ask developers to leave comments on each low-value test, so that Claude learns from them over time

### Question 32

The same session that generates a feature also reviews it before opening the PR. The reviews rarely find problems, yet human reviewers often do. What's the most effective change? **Select 1.**

- [ ] A. Add an instruction asking the session to review its own code more critically than it did before
- [ ] B. Have the same session review the code three times, then report only the issues found each time
- [ ] C. Run the review in a separate, independent instance with no access to the generation context
- [ ] D. Turn on more extended thinking in the session, so that it can reconsider its earlier decisions

### Question 33

A review prompt says "check that comments are accurate, and be conservative." Developers report many findings on comments that are only slightly imprecise. Which change best improves precision? **Select 1.**

- [ ] A. Ask the model to attach a confidence score to each finding, then hide the scores under 80%
- [ ] B. Add "only report findings when you're highly confident" to the end of the prompt, in bold type
- [ ] C. Define the criterion: flag a comment only when its claimed behavior contradicts what the code actually does
- [ ] D. Increase the review's temperature, so that the model considers a wider range of possibilities

### Question 34

Your "naming" category has a very high false positive rate, and developers have started dismissing security findings as well, without reading them. You'll need some weeks to fix the naming prompt. What should you do now? **Select 1.**

- [ ] A. Lower the severity of naming findings, and keep posting them on each pull request
- [ ] B. Disable all categories until the naming prompt has been rewritten and tested
- [ ] C. Keep all the categories on, and ask developers to read each finding carefully
- [ ] D. Temporarily disable the naming category, while you improve its prompt

### Question 35

A CI runner's home directory has a `CLAUDE.md` that holds your review conventions. A new runner pool was added, and reviews there ignore the conventions. What's the best long-term fix? **Select 1.**

- [ ] A. Move the conventions into the repository's `CLAUDE.md`, so that they're versioned with the code
- [ ] B. Add a `--memory` flag to the job, pointing to the location of the file on the original runner pool
- [ ] C. Copy the file to the new runner pool's home directories, then add a monthly check that they match
- [ ] D. Paste the conventions into the job's prompt, using a variable that's stored in the CI system's settings

### Question 36

Each time a developer pushes a fix, the review runs again and reposts comments on issues that have already been raised, along with some that have already been fixed. Developers have started to ignore them. How should you change the re-run? **Select 1.**

- [ ] A. Run the review once per pull request, and don't run it again after the developer has pushed more commits
- [ ] B. Delete all the earlier comments before each run, then post whatever the new review happens to find
- [ ] C. Include prior findings in the context, and ask it to report only new or still-unaddressed issues
- [ ] D. Review only the most recent commit, and ignore all of the other changes that are in the pull request

### Question 37

Your job runs `claude "Review this diff for security issues"`. Locally it works fine, but in CI the step never finishes and is eventually killed by the runner's timeout. What's the correct fix? **Select 1.**

- [ ] A. Run it with the `-p` flag, so that it runs non-interactively
- [ ] B. Add `--batch`, so that the request is handled asynchronously
- [ ] C. Redirect standard input from the runner's terminal device
- [ ] D. Set the `CLAUDE_HEADLESS=true` environment variable in the job

### Question 38

A 22-file pull request gets shallow comments on later files, and contradictory findings about the same pattern. A larger context window wouldn't change how it's being reviewed. Which restructuring fits? **Select 1.**

- [ ] A. A review of only the largest files, because those are where most of the defects tend to be found
- [ ] B. A single pass with a longer prompt, asking for equal attention to be paid to each of the files
- [ ] C. Per-file passes for local issues, plus a separate integration pass for cross-file data flow
- [ ] D. Three complete reviews of the pull request, keeping only the findings that appear in all three

### Question 39

Test generation keeps proposing tests that already exist. What's the most direct way to reduce this? **Select 1.**

- [ ] A. Use `--output-format json` on the generation step
- [ ] B. Request twice as many tests, then remove duplicates
- [ ] C. Provide the existing test files in the context
- [ ] D. Run test generation overnight, using a batch

### Question 40

The security team has a standard review procedure, and it should run the same way in CI and for every developer on the team. Where should it be defined? **Select 1.**

- [ ] A. In a personal skill, under `~/.claude/skills/`
- [ ] B. In the description of a job, in the CI system
- [ ] C. In the team's wiki, with a link from the README
- [ ] D. In a checked-in project skill or command, under `.claude/`

### Question 41

You need to move CI from one review service to another, which touches 40 pipeline files and has two viable designs. Your first step in Claude Code should be which of these? **Select 1.**

- [ ] A. A `/compact` of the session, so that there's enough room before starting to edit any files
- [ ] B. Direct execution, starting with the simplest file, so that the team can see early progress
- [ ] C. Plan mode, to explore the pipeline and compare the designs before making any changes
- [ ] D. A skill with `context: fork`, so that all of the changes are made away from the main session

### Question 42

You want Claude to implement a new rate limiter in the pipeline's tooling, with defined behavior under bursts and acceptable overhead. Which approach gives the best guidance? **Select 1.**

- [ ] A. Provide a one-paragraph description, and tell Claude to keep trying until it reports that it's finished
- [ ] B. Write tests for expected behavior, edge cases and performance first, then iterate by sharing failures
- [ ] C. Write one happy-path test, as additional tests are likely to distract Claude from the main behavior
- [ ] D. Have Claude write the code first, and then have it write tests that confirm what the code does

### Question 43

Stricter review rules apply to anything under `payments/`, and to all database migration files, which live in several services. These rules shouldn't add context when other code is being reviewed. What's the best configuration? **Select 1.**

- [ ] A. A section at the top of the root `CLAUDE.md`, with instructions on when to apply it
- [ ] B. A `.claude/rules/` file with `paths: ["payments/**/*", "**/migrations/**/*"]`
- [ ] C. A rule file with `scope: critical` in its frontmatter, to mark it as a high priority
- [ ] D. A `CLAUDE.md` file in each directory that contains a database migration

### Question 44

Two CI workloads are being reviewed for cost: a blocking pre-merge check that developers wait on, and a nightly technical debt report. Which assignment is right? **Select 1.**

- [ ] A. Message Batches API for the pre-merge check, and the synchronous API for the nightly report
- [ ] B. Synchronous API for both, since batches don't support any structured output or code analysis
- [ ] C. Message Batches API for both, since it costs 50% less and normally finishes well within a day
- [ ] D. Synchronous API for the pre-merge check, and the Message Batches API for the nightly report

### Question 45

A script posts findings as inline PR comments, but it keeps breaking when Claude's output is worded differently. Which two flags address this? **Select 2.**

- [ ] A. `--strict-text`
- [ ] B. `--parse-comments`
- [ ] C. `--output-format json`
- [ ] D. `--ci-mode`
- [ ] E. `--json-schema`

---

## Scenario 4: Structured Data Extraction

*Suggested checkpoint: start by minute 90.*

Pinegrove Freight is building a system that extracts structured data from bills of lading, customs forms and carrier invoices using Claude. Output is validated with JSON schemas and sent to a downstream logistics system. Documents vary by carrier and country, and some are informal or incomplete. Accuracy matters more than speed.

### Question 46

A retry of a failed extraction keeps returning the same wrong result. Your retry message says "That was wrong. Please try again." Which change is most likely to help? **Select 1.**

- [ ] A. Include the specific validation error, along with the document and failed output
- [ ] B. Send the same message several more times, so that the model has more attempts at it
- [ ] C. Add a sentence to the message that says that the result is very important to the business
- [ ] D. Lower the maximum number of tokens, so that the model's next response must be more focused

### Question 47

A synthesis step finished with no data from one carrier region, because its source was unavailable. The output reads as though coverage were complete. What should the output include? **Select 1.**

- [ ] A. A statement that the unavailable region had nothing relevant to report during the period
- [ ] B. An estimate for the missing region, based on the patterns in the other carrier regions
- [ ] C. Coverage annotations showing which findings are well supported and which areas have gaps
- [ ] D. A note at the end stating that all of the available sources were checked successfully

### Question 48

You've defined an extraction tool with a JSON schema as its input parameters, and the model calls it correctly. Where does your code get the extracted data? **Select 1.**

- [ ] A. From the `input` of the `tool_use` block in the response
- [ ] B. From the `stop_reason` field, which holds the structured result
- [ ] C. From the `tool_result` block that the API sends to you automatically
- [ ] D. From the text blocks that follow the tool call in the response

### Question 49

Extraction from informal carrier notes sometimes invents a weight when a note says only "about two pallets." Detailed instructions haven't fixed this. What's the most effective prompt-level change? **Select 1.**

- [ ] A. Add a sentence in capital letters saying that the model must never invent any measurement values
- [ ] B. Make the weight field required, so that the model must look more carefully for an actual value
- [ ] C. Add few-shot examples showing how informal measurements are handled, including when to leave a value empty
- [ ] D. Add a validation rule that rejects any weight value below a minimum threshold for a shipment

### Question 50

A single prompt aggregates findings from 60 customs documents. The model reliably reports issues from the first and last few documents, but misses several in the middle. What's the best mitigation? **Select 1.**

- [ ] A. Put a key findings summary first, and organize the details with explicit section headers
- [ ] B. Increase the maximum output tokens, so that the model has room to report all of the issues
- [ ] C. Add a sentence to the prompt asking the model to pay equal attention to the entire input
- [ ] D. Move the most important documents to the end, and leave the rest of the input unchanged

### Question 51

Your rate-lookup step is called by an extraction subagent, and the service times out after one retry. The coordinator currently receives "lookup unavailable." What should the subagent return to allow good recovery? **Select 1.**

- [ ] A. A stack trace from the rate service, so that the coordinator can diagnose the cause
- [ ] B. A valid empty result, so that the rest of the pipeline can continue with no interruption
- [ ] C. A request to terminate the whole pipeline, as the data can no longer be trusted
- [ ] D. The failure type, the attempted query, any partial results and possible alternatives

### Question 52

You're triaging last night's failed customs extractions, to decide which ones are worth retrying with the validation error attached. Which two should you retry? **Select 2.**

- [ ] A. A date returned as "12 March" where the schema's format requires ISO 8601
- [ ] B. A carrier name placed in the consignee field, with the correct value also present in the document
- [ ] C. A shipper's address on a page of the document that wasn't included in the input
- [ ] D. A container number from a scan where the digits are completely unreadable
- [ ] E. A customs code that's only held in a separate manifest that wasn't provided

### Question 53

Overall accuracy is 96%, but when you measure by field, `hazard_class` is right only 71% of the time on customs forms. Leadership still wants to automate high-confidence extractions from all document types. What's the best recommendation? **Select 1.**

- [ ] A. Automate everything, since 96% is the number that matters most for a general business decision
- [ ] B. Keep review for weak segments such as this one, and automate only where accuracy holds by type and field
- [ ] C. Improve overall accuracy to 98% first, as this will necessarily correct the weak field as well
- [ ] D. Pause all of the automation, since one weak field means the entire system can't be trusted at all

### Question 54

Batch results come back, and you must match each one to its source document. Results don't necessarily arrive in the order submitted. What should you rely on? **Select 1.**

- [ ] A. The document name written in the response text
- [ ] B. The position of each result in the output file
- [ ] C. The time at which each result was completed
- [ ] D. The `custom_id` you assigned to each request

### Question 55

Dates and weights appear in many formats across documents, and your schema is strict on structure. Downstream systems still receive inconsistent values, such as "12/03/26" and "March 12." What should you add? **Select 1.**

- [ ] A. A second schema, used for all documents that have non-standard dates
- [ ] B. Format normalization rules in the prompt, alongside the strict schema
- [ ] C. A required flag on each of the date and weight fields in the schema
- [ ] D. A higher maximum token limit for each of the extraction requests

### Question 56

Your batch of carrier invoices needs a lookup tool, because the extraction has to query a rate table during processing and use the answer. What's the problem with this design? **Select 1.**

- [ ] A. The Batches API requires that each request completes in less than one hour
- [ ] B. The Batches API doesn't support executing tools mid-request and returning the results
- [ ] C. The Batches API costs more when a tool is defined in any one of the requests
- [ ] D. The Batches API can only be used on documents of a single type in each batch

### Question 57

A carrier's 2024 fuel surcharge of 8% and its 2026 surcharge of 11% were reported downstream as conflicting values. How could you have prevented this? **Select 1.**

- [ ] A. Require publication or collection dates in the structured output
- [ ] B. Have the model decide which value it believes is more likely to be true
- [ ] C. Average the values, since both come from the same carrier's documents
- [ ] D. Keep only the higher of the two values, and discard the other one

### Question 58

Your pipeline must run `extract_metadata` before any enrichment tool, because the enrichment steps depend on its output. The enrichment tools have to be available for later turns. According to the exam guide, what should you do? **Select 1.**

- [ ] A. Force `extract_metadata` with `{"type": "tool", "name": "extract_metadata"}`, then handle enrichment in follow-up turns
- [ ] B. Set `tool_choice: "any"`, and describe `extract_metadata` as the tool that should be selected first
- [ ] C. Set `tool_choice: "auto"`, and add a sentence to the prompt saying that metadata should come first
- [ ] D. Remove the enrichment tools from all requests, and ask the model to carry out the enrichment in text

### Question 59

Each carrier lookup returns 45 fields, of which your extraction step uses 6. Context fills quickly in multi-document runs, and quality falls late in each run. What's the best fix? **Select 1.**

- [ ] A. Use a model with a larger context window, and leave the lookup results as they are
- [ ] B. Ask the model in the prompt to ignore all the fields that it doesn't need for this task
- [ ] C. Remove the earlier tool results from the history, without replacing them with anything
- [ ] D. Trim tool outputs to the relevant fields before they accumulate in context

### Question 60

A freight report combines a table of rates, news on port delays and technical customs rules. Your synthesis step flattens them all into uniform paragraphs, and readers struggle to use the rates. What should you do? **Select 1.**

- [ ] A. Remove the news and technical sections, so that the report contains only the rate data readers need
- [ ] B. Attach the original source documents, and leave the synthesis output as uniform paragraphs
- [ ] C. Render each type appropriately: tables for financial data, prose for news, structured lists for technical findings
- [ ] D. Convert everything to bullet points, since a single consistent format is always easier for readers to follow

---

**End of simulation.** Save this file and ask Claude to evaluate it.
