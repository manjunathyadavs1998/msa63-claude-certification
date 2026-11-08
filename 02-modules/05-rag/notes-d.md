<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Part II, Retrieval</div>

# RAG, Lesson 4

## Query Understanding, Context Assembly and Citations

<div class="sub">One analyst report says the market is $4.1B, and another says $5.6B. Your assistant picked the larger one, dropped the sources, and stated it as fact. Both numbers were correct, two years apart. Provenance is what would have shown that.</div>

<table class="meta">
<tr><td>Module</td><td>AI05 of 13</td></tr>
<tr><td>Lesson</td><td>4 of 7</td></tr>
<tr><td>Prerequisites</td><td>AI03 L2, AI03 L6, AI04 L3</td></tr>
<tr><td>Exam relevance</td><td><b>Direct for provenance.</b> TS 5.6 in full, and TS 1.3's structured context. This is the one exam-heavy lesson in the module</td></tr>
<tr><td>Reading time</td><td>~45 minutes</td></tr>
</table>
</div>

<div class="toc">

## Contents

1. [Query Understanding, Context Assembly and Citations](#query-understanding-context-assembly-and-citations)

</div>

<div class="lesson">
<div class="lesson-meta">Lesson 4 of 7 · RAG</div>

# Query Understanding, Context Assembly and Citations

::: keyidea
==**What happens around retrieval matters as much as the search itself.**== Rewrite the question so that it can be searched, assemble the results in a deliberate order, and keep every claim tied to its source. ==**Sources disappear silently during summarization unless your data structures force them to survive, and conflicts must be annotated, not resolved by picking a number.**==
:::

<div class="prereq"><b>Before:</b> context layout (AI03 L6), fusion and rerank (AI04 L3). <b>Teaches:</b> query rewriting, decomposition, HyDE, context assembly, the Citations API, claim-source mappings, conflict and date handling, coverage reporting. <b>Used by:</b> AI05 L7, AI06 L6, AI06 L10, AI13 L6.</div>

## The hook: two true numbers, one wrong answer

Your research assistant retrieves two good chunks:

| Source | Says | Published |
| --- | --- | --- |
| Report A | The market reached $4.1B | 2023 |
| Report B | The market is estimated at $5.6B | 2025 |

A summarizing step condenses them, and the final answer reads: *"The market is $5.6B."* Three things have gone wrong:

1. The source is gone, so nobody can check it.
2. The date is gone, so growth now looks like a contradiction, or vanishes.
3. If the model had picked $4.1B, the answer would be just as confident, and two years stale.

::: key
==**TS 5.6 tests exactly this**: attribution is lost when findings are compressed without claim-source mappings, conflicts need annotating with attribution rather than arbitrary selection, and dates must be required so that temporal differences aren't misread as contradictions.==
:::

## Intuition: footnotes in a due-diligence report

A good analyst's report doesn't just state figures. Each one has a footnote: who said it, where and when. When sources disagree, the analyst doesn't quietly choose. They write: *"Report A says X in 2023, and Report B says Y in 2025. The difference reflects two years of growth."* Their reader can then trust it, or check it.

Your pipeline should behave like that analyst. And the footnotes have to travel with the facts through every step.

## Part 1: Understanding the query

Users don't write search queries. The question as asked is often a poor match for your documents.

| Problem | Example | Technique |
| --- | --- | --- |
| Depends on the conversation | "What about for contractors?" | **Rewrite** using history, into a standalone question |
| Several questions in one | "Compare card and wallet refund times" | **Decompose**, then search each |
| Different vocabulary | "money back" | Hybrid search handles much of it (AI04 L3) |
| Very short, or abstract | "failover" | **Expansion**, or HyDE |
| Implied filters | "last year's policy for India" | **Extract filters** using structured output |
| Wrong source | "How many tickets?" | **Route** (AI05 L1) |

### Rewriting

This is the highest-value technique for conversational RAG. Without it, follow-up questions search for almost nothing.

| Turn | Text |
| --- | --- |
| User, turn 1 | "What's our parental leave policy in India?" |
| User, turn 2 | "What about for contractors?" |
| **Rewritten query** | "Parental leave policy for contractors in India" |

Use a small model at low effort, with a few examples, and **log both forms**, or you won't be able to debug it.

### HyDE

[HyDE](https://arxiv.org/abs/2212.10496) (Gao and colleagues, 2022) has a model write a *hypothetical* answer, embeds that, and searches for real documents like it. The idea is that an answer looks more like a document than a question does. Its cost is an LLM call per query, and the abstract relies on the encoder filtering out incorrect details. So measure it, and it's rarely a first step.

### Should you use any of these?

Each adds latency and cost. **Add one only if your eval, segmented by query type, shows the need.**

## Part 2: Assembling the context

Retrieved chunks aren't a prompt yet.

```mermaid caption="From retrieved chunks to prompt. Deduplication and budgeting come first, then an order chosen deliberately, with long documents above the question."
flowchart TB
  R[Reranked chunks] --> D[Remove near-duplicates]
  D --> P[Check permissions again if sensitive]
  P --> B[Fit the token budget]
  B --> O[Order, with key material first]
  O --> A[Documents, then instructions, then question]
  style O fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
```

| Step | Why | Source |
| --- | --- | --- |
| Deduplicate | Overlap and copies waste budget | AI05 L2 |
| Fit a budget | Fewer, better chunks beat many | HLD M21B |
| Put documents first, question last | Anthropic reports up to 30% improvement in tests, on complex inputs | AI03 L2 |
| Put best material at the edges of long inputs | Position effects | [Lost in the Middle](https://arxiv.org/abs/2307.03172), TS 5.1 |
| Wrap in tags, with source and date | Lets the model cite and compare | AI03 L2 |
| State the rules | Answer only from these. Say so if they're silent | AI02 L6 |

```xml
<documents>
  <document index="1">
    <source>refund-policy.md, Refund timing</source>
    <published>2026-03-01</published>
    <document_content>Card refunds take 5 to 10 business days...</document_content>
  </document>
</documents>

Answer using only these documents. If they don't contain the answer, say so.
Where documents disagree, report both, with their sources and dates.

Question: How long do card refunds take?
```

Remember that retrieved text is untrusted, and AI09 L2 covers why.

## Part 3: Citations on the Claude API

### Why not simply ask for citations in the prompt?

Models can invent plausible citations. Anthropic's [Citations feature](https://platform.claude.com/docs/en/build-with-claude/citations) changes the mechanism. According to its documentation, compared with prompt-based citation:

| Benefit | Stated reason |
| --- | --- |
| **Reliability** | The API extracts `cited_text` directly, so citations *"are guaranteed to contain valid pointers to the provided documents"* |
| **Quality** | In Anthropic's evaluations, significantly more likely to cite the most relevant quotes |
| **Cost** | `cited_text` doesn't count toward output tokens, or toward input tokens when passed back |

Note precisely what's guaranteed: **valid pointers to your documents.** That doesn't guarantee that the cited passage supports the claim. You still need evals for that (AI05 L6).

### How it works

| Document type | Chunking for citation | Location format |
| --- | --- | --- |
| Plain text | Automatically into sentences | Character indices, 0-indexed |
| PDF | Text extracted, then sentences. Image citations aren't supported | Page numbers, 1-indexed |
| Custom content | Your blocks, as provided | Block indices, 0-indexed |

From `code/ai05-rag/.../CitedAnswers.java`, which compiles against `anthropic-java` 3.2.0, and hasn't been run:

```java
for (Chunk chunk : chunks) {
    // Citations must be enabled on all documents in a request, or on none.
    blocks.add(ContentBlockParam.ofDocument(DocumentBlockParam.builder()
            .source(PlainTextSource.builder().data(chunk.text()).build())
            .title(chunk.title())
            .citations(CitationsConfigParam.builder().enabled(true).build())
            .build()));
}
```

::: warn Citations and structured outputs don't combine
The documentation states these are **incompatible**, because citation blocks interleave with text, which conflicts with strict JSON schemas. For values with citations, it suggests asking Claude to answer with one line per field, then parsing the lines in your own code. It also warns that a prompt asking for only JSON can result in few or no citations.
:::

## Part 4: Provenance through a multi-step pipeline

Citations help at the final answer. In a multi-agent pipeline, attribution has to survive **every step before that**.

### Rule 1: Separate content from metadata

Structured findings make it impossible to summarize away the source by accident. From `Provenance.java`:

```java
public record Finding(String claimKey, String value, String excerpt, String sourceUrl,
        String published) {
}
```

This is TS 1.3 and TS 5.6 together: use structured data formats to separate content from metadata, including source URLs, document names and page numbers. Downstream agents **must preserve and merge these mappings**.

### Rule 2: Annotate conflicts, and don't resolve them silently

```java
long distinctValues = evidence.stream()
        .map(Finding::value).distinct().count();
long distinctDates = evidence.stream()
        .map(Finding::published).distinct().count();
String status;
if (distinctValues == 1) {
    status = evidence.size() > 1 ? "WELL_SUPPORTED" : "SINGLE_SOURCE";
} else {
    status = distinctDates > 1 ? "DIFFERS_BY_DATE" : "CONTESTED";
}
```

Real output, on invented data:

```text
adoption_growth  CONTESTED        [31% (2025), 18% (2025)]
market_size      DIFFERS_BY_DATE  [$4.1B (2023), $5.6B (2025)]
survey_value     WELL_SUPPORTED   [$5.6B (2025), $5.6B (2025)]
```

| Status | What the report does |
| --- | --- |
| `WELL_SUPPORTED` | State it, citing both |
| `DIFFERS_BY_DATE` | Show the trend, with dates. **It's not a contradiction** |
| `CONTESTED` | Show both, with sources. Don't pick one |
| `SINGLE_SOURCE` | State it, attributed to that one source |

A real system also has to check that sources are actually independent. Two articles quoting one survey aren't two sources.

### Rule 3: Verify citations in code

If you're using prompt-based citations, verify them yourself. Real output:

```text
real excerpt verified: true
invented excerpt verified: false
```

```java
static boolean excerptVerified(Finding finding, Map<String, String> corpus) {
    String source = corpus.get(finding.sourceUrl());
    return source != null && source.contains(finding.excerpt());
}
```

It's a small check that catches invented quotes. Normalize whitespace in real code, since an exact match is brittle. It can't tell whether the excerpt really supports the claim.

### Rule 4: Report coverage gaps

From TS 5.3 and the guide's exercises: if a source was unavailable, the report must say so.

| Section | Contents |
| --- | --- |
| Well-established findings | Multiple independent sources |
| Contested findings | Conflicts, with attribution |
| Coverage gaps | Topics with missing or failed sources |

**Silence looks like an absence of evidence, when it may be an outage.**

### Rule 5: Render by content type

TS 5.6 adds a final point: render financial data as tables, news as prose, and technical findings as structured lists, rather than forcing everything into one format.

```mermaid caption="Provenance surviving a pipeline. Each agent passes structured claims forward, and synthesis merges them without dropping sources, dates or conflicts."
flowchart TB
  S[Search agent: findings with source and date] --> C[Coordinator]
  A[Analysis agent: findings with source and date] --> C
  C --> Y[Synthesis merges and annotates conflicts]
  Y --> R[Report: established, contested, gaps]
  style Y fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
```

## How Anthropic solved it

| Problem | Anthropic's approach |
| --- | --- |
| Hand-rolled citations can be invented | The Citations feature |
| Long inputs lose focus | Long-document ordering guidance |
| Synthesis loses sources | Anthropic's multi-agent research system is covered in AI06 L6, once that post has been read |
| Exam-level expectations | TS 5.6 |

## The production architecture

==**This is a recommended reference design, not a specific company's system.**==

**Baseline: raw question, raw chunks.** Follow-up questions fail, and sources vanish.

```mermaid caption="Baseline. The raw question searches badly, and the answer can't be checked."
flowchart TB
  U[Question] --> R[Search with raw text]
  R --> C(Claude)
  C --> A[Answer with no sources]
  style A fill:#FBD5E0,stroke:#B3261E,stroke-width:2px
```

**Improved: rewriting and API citations.** Answers can now be checked. New problem: ==**citations prove that a pointer is valid, not that the claim is supported**, and conflicts can still be flattened.==

```mermaid caption="Improved. Better queries and checkable sources, but no check that sources support the claims, or that conflicts survive."
flowchart TB
  U[Question] --> W[Rewrite]
  W --> R[Hybrid search]
  R --> C(Claude with citations)
  C --> X[New problem: support and conflicts unchecked]
  style X fill:#FFF3C4,stroke:#8A6D00,stroke-width:2px
```

**Production: structured evidence, with checks and sampled review.**

```mermaid caption="Production. Evidence stays structured throughout. Code merges conflicts before generation, the model cites, and sampled evaluation checks that citations really support the claims."
flowchart TB
  U[Question] --> W[Rewrite and extract filters]
  W --> R[Hybrid search with permissions]
  R --> M[Merge evidence and flag conflicts]
  M --> C(Claude with citations)
  C --> E[Sampled support checks]
  style M fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
```

### Failures

| Failure | Containment |
| --- | --- |
| Rewrite changes the question's meaning | Log both, and show the interpreted query to the user |
| Citation points to a retrieved chunk that doesn't support the claim | Sampled evaluation (AI05 L6) |
| All sources failed | Say so, and report the gap |
| Old document outranks a new one | Date metadata, filtered or boosted |
| Two sources repeat one origin | Dedupe by origin, where it's known |

## Trade-offs

| Decision | Gain | Cost |
| --- | --- | --- |
| Query rewriting | Follow-ups work | A model call, and possible distortion |
| API citations | Valid pointers, efficient output | Can't combine with structured outputs |
| Structured findings | Provenance survives | More schema and tokens |
| Showing conflicts | Honest, and auditable | Longer answers, and users must read them |

**When you can skip most of this:** a single-document assistant that isn't conversational.

::: mistake
1. **Searching with the raw follow-up question.** "What about contractors?" finds little.
2. **Summarizing without mappings.** Sources disappear.
3. **Picking one number when sources disagree.** Annotate instead.
4. **Treating date differences as contradictions.** Require dates.
5. **Assuming a valid citation proves the claim.** It proves the pointer.
6. **Combining citations and `output_config.format`.** They're incompatible.
7. **Hiding failed sources.** Report coverage gaps.
:::

::: tip What to say in an interview
*"Provenance has to be structural. Each finding carries its excerpt, source and date, and synthesis merges those rather than summarizing them away. Conflicts get annotated. Different dates become a trend, and same-date disagreements are reported as contested. Claude's citations guarantee valid pointers, though not support, so I'd sample claims in evaluation."*
:::

## Certification lens

::: key How this lesson maps to CCAR-F
Direct: all of **TS 5.6**, plus parts of **TS 1.3**, **TS 5.1** and **TS 5.3**. Scenario 3 (research system) uses these heavily. Wrong answers tend to pick one value, average them, drop the source, or hide a failure.
:::

These are original practice questions, not real or leaked exam questions.

::: qa
<div class="q">Q1 · Exam style, scenario: Multi-Agent Research System · Medium. Final reports read well, but claims can no longer be traced to sources. Subagents return prose summaries. What's the most effective fix?<br>A. Ask the synthesis agent to add citations from its own knowledge.<br>B. Run a final agent that searches the web for a source for each claim.<br>C. Require subagents to output structured claim-source mappings that downstream agents must preserve.<br>D. Increase the synthesis agent's context window.</div>
<div class="a"><b>C</b> (TS 5.6). A invites invented citations. B may attach a source that didn't support the original finding. D doesn't change what the summaries contain.</div>
:::

::: qa
<div class="q">Q2 · Exam style, scenario: Multi-Agent Research System · Medium. Two credible sources give different figures for the same metric, in the same year. What should the document analysis subagent do?<br>A. Keep the more recent publication's figure.<br>B. Average the two figures.<br>C. Keep the figure from the larger organization.<br>D. Include both values with attribution, and let the coordinator decide how to reconcile them.</div>
<div class="a"><b>D</b> (TS 5.6). A, B and C all silently discard or invent information, and the guide says to annotate conflicts rather than select arbitrarily.</div>
:::

::: qa
<div class="q">Q3 · Exam style, scenario: Multi-Agent Research System · Hard. Choose the <b>two</b> changes that best prevent temporal differences from being misread as contradictions, and preserve context.<br>A. Require publication or collection dates in subagent outputs.<br>B. Include methodological context alongside findings.<br>C. Convert all numbers to the most recent year's estimates.<br>D. Drop findings older than two years.<br>E. Keep only the highest number for each metric.</div>
<div class="a"><b>A and B</b> (TS 5.1, TS 5.6). C invents data. D loses history. E misrepresents evidence.</div>
:::

::: qa
<div class="q">Q4 · Exam style, scenario: Multi-Agent Research System · Medium. One search subagent timed out, so a section has less evidence. How should the final report handle this?<br>A. Omit the topic without comment.<br>B. Fill the section using the model's general knowledge.<br>C. Mark the section with a coverage annotation stating which sources were unavailable.<br>D. Fail the whole report.</div>
<div class="a"><b>C</b> (TS 5.3). A hides the gap. B risks fabrication. D discards the useful work already done.</div>
:::

::: qa
<div class="q">Q5 · Configuration · Medium. A request enables citations on one document and not on another, and also sets <code>output_config.format</code>. What are the two problems?</div>
<div class="a">(1) Citations must be enabled on all documents or on none. (2) Citations are incompatible with structured outputs. For cited structured values, ask for one line per field, and parse it in your own code.</div>
:::

::: revision Revision — Lesson 4
- Rewrite follow-up questions into standalone queries, and log both forms.
- Decompose, use HyDE or extract filters only if evals by query type show the need.
- Assemble deliberately: dedupe, budget, then documents first and the question last.
- API citations guarantee valid pointers. They don't prove support.
- cited_text doesn't count toward output tokens.
- Citations must be on all documents or none, and they don't combine with structured outputs.
- Provenance needs structure: claim, excerpt, source and date travel together.
- Annotate conflicts with attribution. Don't pick, average or drop.
- Require dates, so that growth isn't misread as contradiction.
- Report coverage gaps. Silence may be an outage.
- Render by content type: tables, prose, lists.
:::

::: notespace short Your notes
:::
</div>
