# AI12: References and Verification

Numbers are recomputed by `scripts/verify_ai12.py`, which also checks lesson Java against the sources. Outputs come from `code/ai12-claude-code/`. Last checked 2026-11-06, on Claude Code 2.1.318.

## How to run it

```bash
cd code/ai12-claude-code
javac --release 21 -d out src/course/cc/*.java
java -cp out course.cc.ExploreCost ../ai08-java/src
java -cp out course.cc.HookLab                 # run from this directory
java -cp out course.cc.ConfigLint ../..
java -cp out course.cc.CiFindings
```

## Code status

| Lesson | Code | Status |
| --- | --- | --- |
| L1 | `ExploreCost` | Run on this course's real AI08 code. Tokens are characters ÷ 4, an estimate |
| L2, L3 | `ConfigLint` | Run on a generated sample project, and on this repository. My own sketch, whose checks come from doc statements. Java's glob matcher approximates Claude Code's |
| L3 | `ProtectFiles`, `HookLab` | Run as real processes with payloads shaped from the docs. **Not run inside a live Claude Code session** |
| L5 | `CiFindings` | Run on hand-written JSON in the documented shape. Regex-parsed, and a real parser should replace it |

## What was really run against Claude Code

Only things that make no model call:

| Command | Result |
| --- | --- |
| `claude --version` | 2.1.318 |
| `claude --help` | Read for flags, including `--continue` |
| `claude --batch -p x`, `--ci`, `--headless` | `error: unknown option` |
| `claude -p x --output-format json --json-schema 'not-json'` | `Error: --json-schema is not valid JSON...` |
| `claude -p x --bg` | Reports a conflict with `--print` |

## What has never happened

- **No `claude -p` run reached a model.** I avoided spending your subscription, and there's no API key.
- No hook, skill, subagent, rule or `/memory` output was observed in a test session for this module. Behavior comes from the docs.
- `claude-code-action` and GitLab integration weren't run.
- The workflow and settings snippets in the lessons weren't executed.

## Findings, with their limits

| Finding | Limit |
| --- | --- |
| Reading all of AI08 cost about 62× a grep for one question | Character estimate, one question, one repo |
| A hook crashing with exit 1 lets the action proceed | Exit-code meaning is from the docs, and the crash itself is real |
| A hook on `Edit`/`Write` misses Bash, and text checks miss `cat .e*` | My hook's logic |
| Wrong frontmatter fields fail silently | From the docs. I didn't observe it in a live session |
| A missing `structured_output` must not pass CI | The field's absence is from the docs |
| `-p` runs project hooks and `.mcp.json` with no trust dialog | From the docs, and not tested |

## Defects in my own work, found before publishing

| Defect | How found |
| --- | --- |
| The CI parser's regex silently dropped a high-severity finding containing escaped quotes, so the job passed | Reading the output. L5 teaches this |
| L4 attributed interview wording to the docs that wasn't theirs | Checking the saved page |
| L4 claimed the docs said what I'd said about skipping plans | Checking the saved page. Removed |
| L5 said best practices described multi-pass review | Checking. The guide does, and best practices describes a second-session reviewer |
| L1 production figure had a back edge, against the diagram rules | Contact sheet |

## Differences between the guide and current products, taught in these lessons

| Topic | Guide (the exam's answer) | Current docs |
| --- | --- | --- |
| Grep, Glob | Built-in tools | Absent by default on macOS, Linux and WSL, unless named |
| Edit fallback | Read + Write | Also a longer anchor, or `replace_all` |
| Imports | `@import` | `@path` |
| Commands, skills | Separate | Merged |
| `allowed-tools` | Restricts tools | Pre-approves them for a turn |
| Spawning | Task tool | Agent, with `Task` as an alias |
| CI context | CLAUDE.md | Also true of plain `-p`. `--bare` skips it, and is recommended for scripts |

## Sources read

Read on 2026-11-06. Saved full pages were searched for the relevant sections.

| Source | Read how | Used for |
| --- | --- | --- |
| [Memory](https://code.claude.com/docs/en/memory.md) | Relevant sections | L2 |
| [Skills](https://code.claude.com/docs/en/skills.md) | Outline, first 100,000 characters | L3 |
| [Subagents](https://code.claude.com/docs/en/sub-agents.md) | Outline, first 100,000 characters | L3, L4 |
| [Hooks guide](https://code.claude.com/docs/en/hooks-guide.md) | Relevant sections | L3 |
| [Permissions](https://code.claude.com/docs/en/permissions.md) | In AI09 | L3, L5 |
| [Permission modes](https://code.claude.com/docs/en/permission-modes.md) | Outline, first 100,000 characters | L1, L4 |
| [Sessions](https://code.claude.com/docs/en/sessions.md) | In full | L4 |
| [Best practices](https://code.claude.com/docs/en/best-practices.md) | Relevant sections | All |
| [Tools reference](https://code.claude.com/docs/en/tools-reference.md) | Relevant sections | L1 |
| [Headless](https://code.claude.com/docs/en/headless.md) | In full | L5 |
| [GitHub Actions](https://code.claude.com/docs/en/github-actions.md) | In full | L5 |
| [Agent SDK overview](https://code.claude.com/docs/en/agent-sdk/overview.md) | In full | L1 |
| Exam guide v1.0 | In full | All |

**Not read:** the rest of the skills and subagents pages past 100,000 characters, the full hooks reference, and sandboxing and settings pages for this module. The guide note in `COURSE_PROGRESS.md` asked me to finish hooks and MCP, and I read the hooks guide but not the MCP page again, so MCP facts rest on AI06 and the exam guide.

## Guide version

The certification page shows no version number, so I couldn't confirm that v1.0 is the latest. Check it again before sitting the exam.
