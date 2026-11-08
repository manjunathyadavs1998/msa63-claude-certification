# AI07: References and Verification

Versions and outputs were checked on 2026-11-05, by running `code/ai07-frameworks/`.

## Code status

| Lesson | File | Status |
| --- | --- | --- |
| L1 | `ModelPort.java` | Run, and output quoted. Scripted model |
| L2 | `python/langgraph_lab.py` | Run on **LangGraph 1.2.20**, and output quoted. Nodes are plain Python, with no model. In-memory saver in one process, so resuming from another process wasn't tested |
| L2 | `create_agent` example | **Not run.** Needs a provider key |
| L3 | `python/llamaindex_lab.py` | Run on **llama-index-core 0.14.25**, and output quoted. Mock LLM and embeddings, so retrieval order means nothing |
| L4 | `SpringAiLab.java` | Run on **Spring AI 2.0.2**, without Spring Boot. Scripted `ChatModel`. Output quoted with line breaks and trimming |
| L4 | `LangChain4jLab.java` | Run on **LangChain4j 1.23.0**. Scripted `ChatModel`. Output quoted with line breaks and trimming |
| Exercises | Python and Java fragments | **Not run.** The Exercise 3 rewrite wasn't compiled |

**Nothing in AI07 has called a real model.** Whether either Java framework works on `claude-opus-5-5` is untested. So are caching, thinking and streaming, and all framework store integrations.

To rerun the Python labs: `pip install langgraph llama-index-core`, in a virtual environment. The environment I used was temporary and outside the project.

## Findings, with their limits

| Finding | Limit |
| --- | --- |
| Both Java frameworks send tool exception messages to the model by default | Matches both projects' docs. Tested with `RuntimeException` |
| LangChain4j throws at 100 round trips | Observed with one tool. The tools page I read didn't document this limit, and the message names it |
| Spring AI returned its limit message as text | Observed with `ChatClient.create`, no Boot. **The docs list `THROW` as default**, so this differs from what I read. Auto-configuration may behave differently |
| Jar counts and sizes | Measured with Maven in throwaway projects. Runtime and compile dependencies, and they change between releases |
| `langchain4j-anthropic` has its own client | Its POM has no `com.anthropic` dependency |
| Spring AI pins `anthropic-java-core` 2.72.0 | From its POM |
| LangGraph's node restarted on resume | Matches the docs |

## Sources read

| Claim | Source | How it was checked |
| --- | --- | --- |
| Agent SDK vs CLI vs Client SDK vs Managed Agents, `-p` for other languages | [Agent SDK overview](https://code.claude.com/docs/en/agent-sdk/overview) | Read in full |
| Managed Agents model, versions, vaults, budgets | Claude API reference bundled with Claude Code | Overview read. Sub-pages only as summarized there |
| LangChain's definition, `create_agent`, family | [LangChain overview](https://docs.langchain.com/oss/python/langchain/overview) | Read through a fetch tool's outline |
| LangGraph definition and benefits | [LangGraph overview](https://docs.langchain.com/oss/python/langgraph/overview) | Same |
| Checkpointers, stores | [Persistence](https://docs.langchain.com/oss/python/langgraph/persistence) | Same |
| Interrupt rules and pitfalls | [Interrupts](https://docs.langchain.com/oss/python/langgraph/interrupts) | Same, and confirmed in the lab |
| LlamaIndex components | [Framework overview](https://developers.llamaindex.ai/python/framework/), [RAG intro](https://developers.llamaindex.ai/python/framework/understanding/rag/) | Same |
| Flows and Crews | [CrewAI intro](https://docs.crewai.com/en/introduction) | Same. Its "leading" claim is its own |
| AutoGen status | [AutoGen repository](https://github.com/microsoft/autogen) | README read. [Docs site](https://microsoft.github.io/autogen/stable/) read, with no such notice shown |
| Spring AI Anthropic module, options, limits | [Anthropic chat](https://docs.spring.io/spring-ai/reference/api/chat/anthropic-chat.html) | Read through a fetch tool's outline, showing 2.0.2 |
| ChatClient, advisors, `entity` | [ChatClient](https://docs.spring.io/spring-ai/reference/api/chatclient.html) | Same |
| `@Tool`, `ToolContext`, errors, limits | [Tools](https://docs.spring.io/spring-ai/reference/api/tools.html) | Same |
| Boot 4.0 and 4.1 support, BOM, version list | [Getting started](https://docs.spring.io/spring-ai/reference/getting-started.html) | Same. Boot 3 route not stated |
| AI Services, memory, return types | [AI Services](https://docs.langchain4j.dev/tutorials/ai-services) | Same |
| `@Tool`, handlers, `ToolErrorVisibleToLlm` | [Tools](https://docs.langchain4j.dev/tutorials/tools) | Same |
| Anthropic integration, caching, thinking | [Anthropic](https://docs.langchain4j.dev/integrations/language-models/anthropic) | Same |
| Agentic module | [Agents](https://docs.langchain4j.dev/tutorials/agents) | First 100,000 of 183,550 characters |

## Stated from general knowledge

Temporal and workflow engines as an alternative. LangGraph4j's existence. The names Haystack, Semantic Kernel, DSPy, Pydantic AI and the OpenAI Agents SDK, which aren't described. My expectation that Spring AI 1.1.x is the Boot 3 route, labeled in L4 as unconfirmed.

## Not used as sources

`LangChain Handbook.pdf` in the Notes folder, as it's a checklist only.

## Engineering recommendations, not facts

The port pattern, the evaluation checklist, the internal starter, all production architectures, and the choosing advice. **None describes a specific company's system.**

## Depth note

These four lessons ran shorter than their budget, since AI06 teaches the concepts, and this module concentrates on what's specific, and on what could be tested.
