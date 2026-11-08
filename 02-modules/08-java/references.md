# AI08: References and Verification

Numbers are recomputed by `scripts/verify_ai08.py`, which also checks lesson Java against the sources. Outputs come from `code/ai08-java/`. Last checked 2026-11-05.

## How to run it

```bash
cd code/ai08-java
mvn -B test < /dev/null
java -cp "target/classes;target/test-classes" course.ai.StreamLoadLab
```

**Close stdin for Maven here.** Maven hung repeatedly in this environment with stdin open, and has been reliable with `< /dev/null`. I can't say that's the only cause.

## Versions

| Component | Version | How chosen |
| --- | --- | --- |
| Spring Boot | 3.5.16 | Latest 3.5, per the course's Boot 3 rule. **Open-source support ended 30 June 2026** |
| `anthropic-java` | 3.2.0 | Claude API reference |
| MCP Java SDK | 2.1.0 | Latest non-milestone on Maven Central |
| Jackson | 2.21.4, and 3.1.4 through MCP | Resolved by Maven |
| Kafka client | 3.9.2 | Boot-managed |

## Code status

| Lesson | Code | Status |
| --- | --- | --- |
| L1 | `ClaudeModel`, config, `ApiErrors` | **Run through the real SDK against a local fake API.** Never against the real API |
| L2 | `ChatController` | Run on real Tomcat, with a scripted model |
| L2 | `StreamLoadLab` | Run. Tasks only sleep, on one laptop. Timings vary |
| L3 | `AgentService`, policy, ledger, tools | Run with a scripted model and H2 |
| L4 | `OrdersMcpServer` | Run with the official SDK's client, over HTTP on localhost. Not tested with Claude Code |
| L5 | `RagService`, `InMemorySearch` | Run with a scripted model. The search is a stand-in |
| L5 | `PgVectorSearch` | **Compiled, not run.** No Postgres here |
| L6 | `ReviewWorker`, `KafkaErrors` | Run on an embedded Kafka broker, with a scripted model |
| L6 | `BatchJob` | Run through the real SDK against the fake API |
| L8 | `SpendTool`, `PiiTokenizer` | Run. The tokenizer is a regex sketch |
| All | SQL schemas in the lessons, JSON configs, resume and heartbeat design | **Not run** |

Test result: `Tests run: 26, Failures: 0, Errors: 0`. AI09 later added `SecurityLabTest` here, making 31, and it documents defects in `RefundPolicy` and `ToolLedger` (AI09 L3). AI10 added `SpendCapTest`, making 32, which documents a mis-mapped spend-cap 429 (AI10 L1). **Correction, 2026-11-05:** L8 used $0.20 for Sonnet 5.5 cache reads, from the bundled API reference. The pricing page says $0.10 (0.05x input), so L8 now uses that. The two sources disagree, and the pricing page is what I treat as authoritative.

**The fake API's replies were written by me from the API reference.** So these tests show that the SDK sends what I expect, and handles what I expect. They can't show that the real API agrees. Thinking block handling, in particular, is tested only against my own JSON.

## Findings, with their limits

| Finding | Limit |
| --- | --- |
| SDK retries 429 and 529, and not 400, 401 or 404 | Matches the reference. Observed with a fake |
| SDK 2.1.0 negotiated MCP 2025-11-25 | With its own client. Other clients may differ |
| The unknown-tool error said `invalid_tool_name` | Seen in SDK 2.1.0 |
| 2,000 sleeping tasks: 20.4s on 200 platform threads, 2.1s on virtual threads | Sleep only, on one machine |
| Generation stopped at 3 of 200 words after a disconnect | Scripted model. Real API cancellation and billing weren't tested |
| Spring Boot 3.5 support dates | From endoflife.date, and not from Spring's own page |

## Bugs found in my own code

| Bug | How found |
| --- | --- |
| Ledger threw `NullPointerException` on an unfinished call | Writing a crash-recovery test |
| A test left queued replies, so later cases got the wrong status | The test failed |
| A wrap script looped forever, and a shell heredoc damaged a backslash | Observed, then fixed. Use the Write tool for Java containing backslashes |

## Sources read

| Claim | Source | How it was checked |
| --- | --- | --- |
| Java SDK types, streaming, errors, retries, effort, caching | Claude API reference bundled with Claude Code | Read. There's no Java batch page there, so `BatchJob` was written from the shared reference and compile errors |
| Error codes and classes | Same, `error-codes` | Read |
| SDK tiers, and "on their own schedules" | [MCP SDKs](https://modelcontextprotocol.io/docs/sdk) | Read |
| Server, transports, `isError`, Origin validation | [MCP Java SDK server docs](https://java.sdk.modelcontextprotocol.io/latest/server/) | Read through a fetch tool's outline. Those docs are for a later version than 2.1.0, and the code compiled and ran on 2.1.0 |
| Security duties | [MCP tools spec](https://modelcontextprotocol.io/specification/2026-07-28/server/tools) | Read |
| Boot support dates | [endoflife.date](https://endoflife.date/spring-boot) | Read |
| Virtual thread property | [Spring Boot task execution](https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html) | Read through an outline. It doesn't discuss pinning |
| Spring AI 1.1.x supports Boot 3.4 and 3.5 | [Spring AI 1.1 getting started](https://docs.spring.io/spring-ai/reference/1.1/getting-started.html) | Read through an outline |

## Stated from general knowledge

JDK 21 pinning and JFR's `jdk.VirtualThreadPinned`. Proxy buffering, heartbeats and `Last-Event-ID`. Kafka's at-least-once behavior. Temporal as a Java workflow engine. Reconciliation patterns. Banking concerns generally: **this isn't legal or compliance advice.**

## Engineering recommendations, not facts

All production architectures, the banking design, thresholds, limits, targets, and cost estimates. **None describes a specific company's or bank's system.**

## Depth note

Lessons ran shorter than budget, since AI03–AI06 teach the concepts. This module adds running code rather than repeating them.
