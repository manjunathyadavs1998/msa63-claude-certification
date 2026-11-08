# AI13: References and Verification

Last checked 2026-11-06.

## How to run the labs

The labs use a throwaway virtual environment with `claude-agent-sdk` 0.2.178 and `pydantic` 2.14.3. Neither calls a model.

```bash
python -m venv venv
venv/Scripts/python -m pip install claude-agent-sdk pydantic
cd code/ai13-exam
../../venv/Scripts/python sdk_lab.py
../../venv/Scripts/python validation_lab.py
```

Saved output is in `sdk_lab.out.txt` and `validation_lab.out.txt`.

## Code status

| Lesson | Code | Status |
| --- | --- | --- |
| L2 | `sdk_lab.py` | Run. Options built against the installed SDK, and hook callbacks called directly with shaped inputs. **Not run inside a live agent session** |
| L3 | `validation_lab.py` | Run with a scripted fake model. It tests the loop's decisions, not extraction quality |
| L4 | Guide exercises | **Not run end to end.** Parts are linked to this course's code, and the model-calling steps are left to the learner |
| L2 | CI command | Not run, to avoid spending the user's subscription. Flags checked against the CLI reference |

L3's printed output is lightly wrapped and shortened for the page, and says so. Its code sample shortens one error message.

## Primary sources read

| Source | Used for |
| --- | --- |
| `01-source-map/CCAR-F-Exam-Guide-v1.0.pdf`: sections 3–6, 8, 10, appendix and out-of-scope list | All exam facts |
| [Agent SDK: Subagents](https://code.claude.com/docs/en/agent-sdk/subagents.md) | `AgentDefinition`, inheritance table, Agent vs Task, fork subagents |
| [Agent SDK: Hooks](https://code.claude.com/docs/en/agent-sdk/hooks.md) | Events, matchers, outputs, `updatedToolOutput`, `on_failure`, timeouts |
| [Agent SDK: Sessions](https://code.claude.com/docs/en/agent-sdk/sessions.md) | Continue, resume, fork, and resuming across hosts |
| [CLI reference](https://code.claude.com/docs/en/cli-reference.md) | Flag sheet. The saved page was searched for the flags listed |
| [MCP: Resources](https://modelcontextprotocol.io/docs/concepts/resources) | Resources, templates, subscriptions, application-driven use, errors |
| [Pydantic: Validators](https://pydantic.dev/docs/validation/latest/concepts/validators/) | Validators and `ValidationError`, read through a summarizing fetch, then confirmed by running the code |

## Third-party sources: anecdotal or unverified

No exam content was taken from any of these. They inform study advice only, and the lessons label them as such.

| Source | What it says | Treatment |
| --- | --- | --- |
| [claudecertifiedarchitects.com](https://www.claudecertifiedarchitects.com/blog/claude-exam-update-december-2026/) | Three other exams update December 7, 2026, with guides on November 17. It doesn't name CCAR-F | Independent site, unconfirmed at the source. Recheck on or after November 17, 2026 |
| [nbh.co](https://www.nbh.co/learn/claude-certified-architect-foundations-exam/) | Agrees with the above | Independent |
| [passitexams.com](https://passitexams.com/articles/claude-certified-architect-foundations-exam-guide/) | Claims the exam covers more than six scenarios, including Agent Skills and long documents | **No source given, and it conflicts with the guide.** Not taught as fact. Skills and context management are covered anyway |
| [verygood.ventures](https://verygood.ventures/blog/passing-the-claude-certified-architect-exam/) | Practice exam reuses a pool and the real exam felt harder. Claude Code configuration was the author's weakest domain | One account. Its six-month validity and retake statements conflict with the guide and aren't used |
| [rogs.me](https://rogs.me/2026/07/how-i-got-claude-certified-and-how-you-can-too/) | CLI flags were hardest. Tie-break toward what lets Claude handle more. Flag and return | One account, used only as a tie-breaker and for pacing advice |

## What the audit found

`01-source-map/EXAM_COVERAGE_MATRIX.md` maps all 240 guide lines, with the method and its limits.

| Finding | Action |
| --- | --- |
| No reading-level coverage of the Agent SDK in its actual languages | New L2 |
| No full CLI flag reference | New L2 |
| Pydantic named by the appendix, with only one mention in the course | New L3, with a lab |
| MCP resources taught briefly | Deepened in L3 |
| Context degradation symptoms, and summarize-then-spawn, were thin | Patched AI06 L7 |
| Few-shot for hallucination and document structure was only a table row | Patched AI03 L2 |
| No exam technique, scenario maps or guide-exercise walkthroughs | New L1, L4 |
| No timed mocks or revision handbook | Three mocks, and L5 |

## Differences beyond the earlier tables

Added in L2 and L5, from the docs above: a fork subagent inherits context, a failed guard hook lets most actions proceed unless `on_failure="block"`, and `--continue` skips `-p` and SDK sessions.

## Mock exam checks

`scripts/build_mocks.py` checks every answer key against the options marked correct, domain counts of 16/11/12/12/9, 15 items per scenario, answer-letter balance, and stem similarity against the bank and the other mocks. The results are in the validation report. The questions are my own, reviewed by me, and they haven't been trialed on real candidates, so I can't say how hard they are compared with the exam.
