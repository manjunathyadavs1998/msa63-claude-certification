# AI06: References and Verification

Numbers are recomputed by `scripts/verify_ai06.py`, or come from running `code/ai06-agents/`. Last checked 2026-11-05.

## Code status

| Lesson | File | Status |
| --- | --- | --- |
| L1 | `Workflows.java` | Run, and output quoted. The "model" is a scripted function |
| L2 | `AgentLoop.java` | Run, and output quoted with some line breaks added. Scripted model, so it tests control flow only |
| L2, L3 | `ClaudeAgentLoop.java` | **Compiled against `anthropic-java` 3.2.0, not run.** No API key |
| L2 | `RunnerAgent.java` | **Compiled, not run.** No API key |
| L3 | `ToolLint.java` | Run, and output quoted. Mechanical checks, which can't measure selection |
| L4 | `McpMiniServer.java` | Run, and output shortened with `...`. A sketch of message shapes, with no transport, and not a conforming server |
| L5 | `ToolErrors.java` | Run, and output quoted. Failures are scripted |
| L6, L10 | `ResearchCoordinator.java` | Run, and output quoted. Subagents are 300 ms sleeps with invented findings |
| L7 | `HarnessState.java` | Run, and output quoted. Work is simulated, and `hashCode` stands in for SHA-256 |
| L8 | `PolicyHooks.java` | Run, and output quoted. Hook-shaped logic in plain Java, not the Agent SDK's hook API |
| L9 | `SupportScenario.java` | Run, and output quoted. A fixed list of calls tests guardrails, not model behavior |

**Not run or validated:** every JSON config and hook example, the SQL schemas, the MCP connector shape, and the Python `AgentDefinition` example, which is adapted from the docs.

**Nothing in AI06 has called a real model or a real MCP server.**

## Hand-derived figures

All match `verify_ai06.py`: L1's reliability figures and Q4, L2's cache arithmetic, L4's integration counts, L5's call counts, L8's rates, L9's and L10's estimates, and Exercises 2, 5 and 6. Exam-style answer letters are balanced at A 11, B 9, C 10, D 10.

## Sources read

| Claim | Source | How it was checked |
| --- | --- | --- |
| Workflow and agent definitions, five patterns, principles, poka-yoke, absolute paths | [Building Effective Agents](https://www.anthropic.com/engineering/building-effective-agents) | Read through a fetch tool's outline. Only phrases the outline quoted are shown in quotation marks |
| Architecture, 90.2%, 4× and 15×, 95% and 80%, eight principles, evaluation, production | [Multi-agent research system](https://www.anthropic.com/engineering/multi-agent-research-system) | Same |
| Tool principles, `response_format`, 72 and 206 tokens, 25,000 limit | [Writing Tools for Agents](https://www.anthropic.com/engineering/writing-tools-for-agents) | Same |
| Tool search, programmatic calling and tool examples figures | [Advanced Tool Use](https://www.anthropic.com/engineering/advanced-tool-use) | Same. November 2025, on older models |
| 150,000 to 2,000 tokens | [Code Execution with MCP](https://www.anthropic.com/engineering/code-execution-with-mcp) | Same |
| Initializer, coding agent, artifacts, failure table | [Effective Harnesses](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents) | Same |
| Problem statement, launch components, adopters | [Introducing MCP](https://www.anthropic.com/news/model-context-protocol) | Same |
| Participants, layers, primitives, `server/discover`, statelessness, deprecations | [MCP architecture](https://modelcontextprotocol.io/docs/learn/architecture), [server concepts](https://modelcontextprotocol.io/docs/2026-07-28/learn/server-concepts.md) | Read in full, showing protocol version `2026-07-28` |
| `isError`, protocol vs execution errors, security guidance, deterministic order | [MCP tools spec](https://modelcontextprotocol.io/specification/2026-07-28/server/tools) | Read in full |
| Scopes, `.mcp.json`, expansion, transports, output limits, `-p` behavior | [Claude Code MCP docs](https://code.claude.com/docs/en/mcp) | First 100,000 of 155,688 characters, through a fetch tool's outline |
| Events, matchers, decisions, exit codes, `onFailure` | [Hooks reference](https://code.claude.com/docs/en/hooks) | First 100,000 of 286,092 characters, through a fetch tool's outline |
| Continue, resume, fork, what persists | [Agent SDK sessions](https://code.claude.com/docs/en/agent-sdk/sessions) | Read in full |
| `AgentDefinition`, inheritance, Agent and Task names, limits | [Agent SDK subagents](https://code.claude.com/docs/en/agent-sdk/subagents) | Read in full |
| Stop reasons, `tool_choice`, tool runner, connector, four approaches | Claude API reference bundled with Claude Code | Read |
| TS 1.1–1.7, TS 2.1–2.5 | Exam guide v1.0 | Re-read for this module |

## Stated from general knowledge

- MCP's earlier `initialize` handshake, labeled in L4.
- `--continue` and `--fork-session` CLI flags. AI12 should confirm these against the CLI reference.
- The rule of three in Exercise 6.
- HLD M12 reference for circuit breakers.

## Engineering recommendations, not facts

Every production architecture and both designs, thresholds including 30% staleness, $5 budgets and 3 attempts, the estimates in L9 and L10, and the $5 human contact cost. **None describes a specific company's system.** L6's first section reports Anthropic's published system. Everything after it is my design.

## Differences between the exam guide and current behavior

| Topic | Exam guide | Current |
| --- | --- | --- |
| Spawning | `Task` | `Agent`. Still `Task` in the `system:init` list |
| Forced `tool_choice` | Guarantees a call | 400 on Opus 5.5, Sonnet 5.5, Fable 5.1, Mythos 5.1 |
| `errorCategory`, `isRetryable` | Taught next to `isError` | Only `isError` is in the protocol |
| MCP handshake | Not detailed | Stateless, with `server/discover` |
| Sampling | Not mentioned | Deprecated |
