<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Extra Notes</div>

# How a Claude-Style Chat App Holds Context at Scale

## Reference Architecture for Millions of Requests

<div class="sub">One billion stored chats, about a hundred thousand replies being written, and a model that forgets everything between requests. Here is how those three facts fit together.</div>

<table class="meta">
<tr><td>Type</td><td>Extra design note, outside AI01–AI13</td></tr>
<tr><td>Prerequisites</td><td>AI06 L7, AI10 L2, HLD M21D (Design ChatGPT)</td></tr>
<tr><td>Exam relevance</td><td>Out of scope as a design. Context ideas support TS 5.1 and TS 5.4</td></tr>
<tr><td>Honesty note</td><td>Anthropic hasn't published claude.ai's internal architecture. This is a reference design, not its real system</td></tr>
<tr><td>Reading time</td><td>~25 minutes</td></tr>
</table>
</div>

<div class="toc">

## Contents

1. [How a Chat App Holds Context at Scale](#how-a-chat-app-holds-context-at-scale)

</div>

<div class="lesson">
<div class="lesson-meta">Extra note · Reference architecture</div>

# How a Chat App Holds Context at Scale

::: keyidea
==**The app stores every context in shared storage, and the model only sees the one it's working on right now.**== Stateless web servers load a conversation, build a prompt, and send it to any suitable model server. So millions of users don't mean millions of contexts sitting in model memory.
:::

<div class="prereq"><b>Before:</b> stateless Messages API, prompt caching, gateways, and HLD M21D for GPU serving. <b>Teaches:</b> where context lives at each layer, how one turn flows, context assembly, storage design, routing, and failure handling. <b>Doesn't reteach:</b> KV cache, batching and GPU scaling, which HLD M21D covers.</div>

## What's published, and what isn't

Keep these two columns apart, especially in an interview.

| Published by Anthropic | Not published, so not stated here as fact |
| --- | --- |
| The Messages API is stateless, so the client sends the conversation history. A beta message threads option can hold it briefly | claude.ai's databases, caches and service layout |
| Claude runs on TPUs and GPUs, through its own API, Bedrock and Vertex AI (2025 postmortem) | Number of servers, regions or users |
| Routing is sticky, and short and long context requests use separate server pools (same postmortem) | How claude.ai builds its prompts internally |
| Engineers' access to user interactions is limited by privacy and security controls | Its storage partitioning and retention pipeline |
| Prompt cache data is held in memory for its lifetime, and organizations never share caches | Whether it uses any product named in this note |
| App memory updates as you chat, each project has its own memory, and incognito chats aren't saved to history or memory | How the memory pipeline is built |
| Deleted chats leave back-end storage within 30 days. Memory entries made from them aren't removed automatically | |

Everything else below is an ==engineering recommendation for building this kind of product==. Product names such as Redis are examples.

## The hook: the server that remembered too much

An invented team launches a chatbot on one server. It keeps each conversation in a map in server memory, and the demo is perfect.

At launch they add a second server behind a load balancer. A user's second message lands on the other server, and the bot replies, "I don't have any context about your order." They add session affinity, and it works until a deploy restarts a server and thousands of chats vanish.

The bug isn't in the load balancer. ==**The server owns state that must outlive the server.**==

## Intuition: the hospital chart

A hospital has thousands of patients and a few doctors. No doctor remembers every patient.

1. The patient's chart lives in the records room.
2. A clerk pulls the chart and prepares a short summary for this visit.
3. Any available doctor reads it, treats the patient, and forgets the details afterward.
4. The clerk files the new notes in the chart.

The records room is the database, the clerk is the chat service, and the doctor is the model. You can add doctors freely, because no patient belongs to one doctor. Seeing the same doctor again only saves a little time, which is exactly what a warm prompt cache does.

## Mechanism: context has three lifetimes

| Lifetime | Where | Example | If lost |
| --- | --- | --- | --- |
| **Lasting** | Conversation database, file store, memory store | Every message, uploads, saved memories | Real data loss, so replicate and back up |
| **Warm** | Cache such as Redis | Recent messages of active chats | Slower. Reload from the database |
| **In flight** | App server RAM, model server memory, prompt cache | The prompt being processed | The reply may fail. Retry, because the source is safe |

==**Only the lasting layer is the source of truth. Everything else must be safe to lose.**==

### Sizing: why this works

All figures are illustrative assumptions, not Anthropic's numbers. They were recomputed in code.

| Quantity | How | Result |
| --- | --- | ---: |
| Daily users | Assumed | 10 million |
| Messages per day | 10 M × 20 | 200 million |
| Average rate | 200 M ÷ 86,400 s | ≈ 2,315 per second |
| Peak rate | × 3 | ≈ 6,944 per second |
| Replies being written at peak | 6,944 × 15 s each | ≈ 104,000 |
| Conversations stored | 10 M × 100 | 1 billion |
| Stored for each one in flight | 1 B ÷ 104,167 | ≈ 9,600 |
| New text per year | 200 M × 1 KB × 365 | 73 TB, or 219 TB with 3 copies |
| Warm cache | 1 M active chats × 50 KB | 50 GB |
| Input at peak | 6,944 × 8,000 tokens | ≈ 56 M tokens per second |
| Output at peak | 6,944 × 500 tokens | ≈ 3.5 M tokens per second |

So at peak, in this example, only about one conversation in 9,600 is on a model server. Storing text is cheap. The hard part is the GPU time for each reply, which HLD M21D sizes in depth.

## Why the obvious fixes fall short

| Fix | What it solves | Why it isn't enough |
| --- | --- | --- |
| Keep chats in server memory | Fast and simple | Lost on restart, and not shared between servers |
| Session affinity to one web server | Second-server bug | Deploys and failures still lose chats, and load gets uneven |
| Store history in the browser | No storage for you | No sync between devices, and the client can edit it |
| Keep every context loaded on GPUs | No reload | Wastes the scarcest memory on people who aren't typing |
| One big SQL database | Easy start | Writes and storage eventually outgrow one machine |
| Send the whole history forever | Nothing is forgotten | Cost grows each turn, and it eventually exceeds the context window |

## How the field solves it

There's no special trick. It's a known pattern: ==stateless services, partitioned durable storage, a cache, and queues==. What's unusual is the last hop, a scarce GPU fleet with long-running streaming requests, which needs token-based limits and context-aware routing. On the published side, Anthropic's postmortem confirms sticky routing and separate pools for short and long context.

## The production architecture

### Baseline: state on the web server

```mermaid caption="Baseline. Simple, but chats disappear on restart and can't move between servers."
flowchart TB
  U[Users] --> S[Single web server]
  S --> M[Conversations in server memory]
  S --> C[Claude model]
  style M fill:#FBD5E0,stroke:#B3261E,stroke-width:2px
```

### Improved: shared database

**Adds:** stateless servers and one database. **New problem:** ==every turn reads and writes one database, while long streams tie up connections and quota==.

```mermaid caption="Improved. Any server can handle any message, but the database and the provider quota become the bottlenecks."
flowchart TB
  U[Users] --> L[Load balancer]
  L --> A[Web server A]
  L --> B[Web server B]
  A --> D[One shared database]
  B --> D
  A --> C[Claude model]
  B --> C
  style A fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
  style B fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
  style D fill:#FBD5E0,stroke:#B3261E,stroke-width:2px
  style C fill:#FFF3C4,stroke:#8A6D00,stroke-width:2px
```

### Production: the full picture

<figure>
<svg viewBox="-10 0 810 1085" role="img" aria-label="Reference architecture. Clients connect through an edge layer to a stateless chat service. A context assembler loads context from durable stores and sends it through an inference router to model servers. Replies stream back and are saved. Background workers update summaries, memory and search." xmlns="http://www.w3.org/2000/svg" style="width:100%;height:auto;font-family:'Patrick Hand',cursive;color:#1C3461">
<defs><marker id="a" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse"><path d="M0 0 L10 5 L0 10 Z" fill="#1C3461"/></marker></defs>
<g fill="#FFFEF8" stroke="#1C3461" stroke-width="1.2" stroke-dasharray="5 4">
<rect x="14" y="14" width="772" height="122"/>
<rect x="14" y="166" width="772" height="104"/>
<rect x="14" y="306" width="350" height="304"/>
<rect x="14" y="646" width="350" height="425"/>
<rect x="450" y="306" width="336" height="520"/>
<rect x="450" y="876" width="336" height="195"/>
</g>
<g font-size="17" font-weight="700" fill="#1B6B4A">
<text x="26" y="38">1 · CLIENTS: display copy, session token, last message ID</text>
<text x="26" y="190">2 · EDGE: no conversation state</text>
<text x="34" y="330">3 · STATELESS APP: in-flight context only</text>
<text x="34" y="670">4 · INFERENCE: temporary context only</text>
<text x="462" y="330">5 · CONTEXT STORES: source of truth</text>
<text x="462" y="900">6 · BACKGROUND WORK</text>
</g>
<g fill="#CFE4F7" stroke="#1C3461" stroke-width="1.6">
<rect x="34" y="52" width="230" height="68"/><rect x="285" y="52" width="230" height="68"/><rect x="536" y="52" width="230" height="68"/>
<rect x="34" y="202" width="230" height="56"/><rect x="285" y="202" width="230" height="56"/><rect x="536" y="202" width="230" height="56"/>
<rect x="34" y="344" width="310" height="110"/><rect x="34" y="490" width="310" height="104"/>
<rect x="34" y="684" width="310" height="96"/>
<rect x="34" y="822" width="148" height="86"/><rect x="196" y="822" width="148" height="86"/>
<rect x="470" y="344" width="296" height="70"/>
<rect x="470" y="534" width="296" height="70"/><rect x="470" y="620" width="296" height="88"/><rect x="470" y="724" width="296" height="86"/>
<rect x="470" y="914" width="296" height="60"/><rect x="470" y="988" width="296" height="68"/>
</g>
<rect x="34" y="946" width="310" height="110" fill="#FFF3C4" stroke="#8A6D00" stroke-width="2"/>
<rect x="470" y="430" width="296" height="88" fill="#CDEFD6" stroke="#1B6B4A" stroke-width="3"/>
<g font-size="17" font-weight="700" fill="#1C3461">
<text x="149" y="80" text-anchor="middle">Browser</text><text x="400" y="80" text-anchor="middle">Desktop app</text><text x="651" y="80" text-anchor="middle">Mobile app</text>
<text x="149" y="236" text-anchor="middle">CDN and DDoS shield</text><text x="400" y="236" text-anchor="middle">Load balancer</text><text x="651" y="236" text-anchor="middle">API gateway</text>
<text x="50" y="372">Chat service</text><text x="50" y="518">Context assembler</text><text x="50" y="712">Inference router</text>
<text x="108" y="850" text-anchor="middle">Short pool</text><text x="270" y="850" text-anchor="middle">Long pool</text>
<text x="50" y="974">Model servers (GPU, TPU)</text>
<text x="486" y="372">Warm cache (Redis)</text><text x="486" y="458">Conversation database</text><text x="486" y="562">File store (S3-like)</text><text x="486" y="648">Memory and projects</text><text x="486" y="752">Search index</text>
<text x="486" y="942">Event queue</text><text x="486" y="1016">Workers</text>
</g>
<g font-size="15" fill="#23262B">
<text x="149" y="104" text-anchor="middle">streams the reply</text><text x="400" y="104" text-anchor="middle">streams the reply</text><text x="651" y="104" text-anchor="middle">streams the reply</text>
<text x="50" y="396">checks access to this chat</text><text x="50" y="418">saves turns, relays the stream</text><text x="50" y="440">cancels if the user leaves</text>
<text x="50" y="542">instructions + memory + summary</text><text x="50" y="564">+ recent turns + retrieved text</text><text x="50" y="584">fits the token budget</text>
<text x="50" y="736">token limits and priority</text><text x="50" y="758">picks pool, load and warm cache</text>
<text x="108" y="876" text-anchor="middle">most requests</text><text x="270" y="876" text-anchor="middle">very long chats</text>
<text x="50" y="998">holds only in-flight prompts</text><text x="50" y="1020">batches many users together</text><text x="50" y="1042">prompt cache: 5 minutes or 1 hour</text>
<text x="486" y="396">recent messages, safe to lose</text>
<text x="486" y="482">all messages, split by conversation</text><text x="486" y="504">replicated, lasting copy</text>
<text x="486" y="586">uploads, read when needed</text>
<text x="486" y="672">user facts, project documents</text><text x="486" y="694">checked per user and project</text>
<text x="486" y="776">past chats and project text</text><text x="486" y="798">found by meaning and keywords</text>
<text x="486" y="964">turn saved, chat deleted</text>
<text x="486" y="1040">summaries, memory, index, titles</text>
</g>
<g stroke="#1C3461" stroke-width="2.2" fill="none">
<line x1="400" y1="120" x2="400" y2="202" marker-start="url(#a)" marker-end="url(#a)"/>
<line x1="264" y1="230" x2="285" y2="230" marker-end="url(#a)"/><line x1="515" y1="230" x2="536" y2="230" marker-end="url(#a)"/>
<path d="M651 258 V288 H320 V344" marker-end="url(#a)"/>
<line x1="189" y1="454" x2="189" y2="490" marker-end="url(#a)"/>
<line x1="320" y1="594" x2="320" y2="684" marker-end="url(#a)"/>
<line x1="110" y1="780" x2="110" y2="822" marker-end="url(#a)"/><line x1="270" y1="780" x2="270" y2="822" marker-end="url(#a)"/>
<line x1="108" y1="908" x2="108" y2="946" marker-end="url(#a)"/><line x1="270" y1="908" x2="270" y2="946" marker-end="url(#a)"/>
<line x1="344" y1="400" x2="449" y2="400" marker-end="url(#a)"/>
<line x1="450" y1="540" x2="345" y2="540" marker-end="url(#a)"/>
<path d="M34 1000 H24 V400 H33" marker-end="url(#a)"/>
<line x1="590" y1="826" x2="590" y2="875" marker-end="url(#a)"/>
<line x1="690" y1="876" x2="690" y2="827" marker-end="url(#a)"/>
<line x1="618" y1="974" x2="618" y2="988" marker-end="url(#a)"/>
</g>
<g font-size="15" font-weight="700" fill="#1C3461">
<text x="660" y="280" text-anchor="start">after login</text>
<text x="372" y="390">saves turn</text>
<text x="370" y="530">loads context</text>
<text x="312" y="634" text-anchor="end">full prompt for this turn</text>
<text x="472" y="856">new-turn event</text>
<text x="700" y="856">results</text>
<text transform="translate(8 790) rotate(-90)" text-anchor="middle" font-size="14">tokens stream back</text>
</g>
</svg>
<figcaption>The full architecture. Reference design, not Anthropic's real system. Layers 1 to 4 can lose a machine without losing a conversation, because the lasting copy is in layer 5. Each model server sees only the requests running on it.</figcaption>
</figure>

| Component | Why it exists | What it holds | How it communicates |
| --- | --- | --- | --- |
| Clients | Show chats and receive the stream | Token, last message ID, on-screen copy | HTTPS, with server-sent events for replies |
| CDN, DDoS shield | Absorb static traffic and attacks | Static files only | HTTPS |
| Load balancer | Spread long-lived streams | Nothing lasting. Must not buffer events | HTTP with long timeouts |
| API gateway | Sign-in checks and plan limits | Limit counters, in a shared store | Calls the chat service |
| Chat service | Runs each turn | In-flight context only | Calls stores and router |
| Context assembler | Builds the prompt within a budget | Nothing lasting | Reads stores. May be a library inside the chat service |
| Warm cache | Avoids a database read on most turns | Recent messages | Read through, and safe to lose |
| Conversation database | Source of truth for messages | Every message | Partitioned, replicated writes |
| File store | Keeps large uploads out of the database | Files and extracted text | Accessed by reference |
| Memory, projects | Context beyond one chat | Facts and project documents | Queried by user and project |
| Search index | Finds relevant past text | Keywords and embeddings | Rebuildable from the source |
| Event queue | Keeps slow work off the reply path | Events | Publish and consume |
| Workers | Summaries, memory, indexing, titles | Nothing lasting | Write results back to the stores |
| Inference router | Spends scarce GPU time well | Load and cache hints | Forwards the stream |
| Model servers | Generate tokens | In-flight prompt and temporary caches | Stream tokens back |

### Request flow: one turn

```mermaid caption="One turn. The turn is saved before streaming ends, so a dropped connection doesn't lose it. Slow work happens after the reply."
sequenceDiagram
  participant B as Browser
  participant C as Chat service
  participant S as Stores and queue
  participant M as Router and model
  B->>C: Send message, with request ID
  C->>S: Check access, save message
  S-->>C: Memory, summary, recent turns
  C->>M: Full prompt for this turn
  M-->>C: Tokens stream
  C-->>B: Tokens stream
  C->>S: Save reply and usage, publish event
  S->>S: Workers update summary and memory
```

## Deep dive 1: assembling context

This is the most important step. Each turn builds a fresh prompt from several sources, then fits it to a budget.

```mermaid caption="Context assembly. Stable parts go first, so the prompt cache can reuse them. Old turns are trimmed before key facts are."
flowchart TB
  T["Stable:<br/>tools, system,<br/>project files"] --> A[Context assembler]
  P["This user:<br/>memory, facts,<br/>summary, turns"] --> A
  N["This turn:<br/>retrieved text,<br/>new message"] --> A
  A --> B[Token budget check]
  B -->|fits| O[One request to the model]
  B -->|too big| X[Trim, summarize or compact]
  style A fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
  style X fill:#FFF3C4,stroke:#8A6D00,stroke-width:2px
```

| Choice | Recommendation | Why |
| --- | --- | --- |
| Order | Tools, system, stable documents, history, new message | The prompt cache matches from the start, and a change early on invalidates what follows |
| Long chats | Recent turns word for word, plus a summary of older ones | Keeps nearby meaning at lower cost |
| Key facts | A structured block of IDs, amounts and dates | Summaries can lose exact values (TS 5.1) |
| Search results | Fetch only what this turn needs, and label it as data | Saves tokens and limits prompt injection |
| Compaction | Run it in the background where you can | The user shouldn't wait for it. The API offers compaction options, in beta |

### Java: the core trimming rule

This keeps fixed context and walks backward from the newest turn until the budget runs out.

```java
static Prompt build(Stored stored, String newMessage, int budget) {
    String system = stored.system() + "\n\nKnown about this user: " + stored.memory()
            + "\n\nEarlier in this chat: " + stored.summary();
    int used = tokens(system) + tokens(newMessage);
    if (used > budget) {
        throw new IllegalArgumentException("Fixed context exceeds the budget: " + used);
    }

    // Walk newest to oldest, so that the most recent turns survive.
    Deque<Turn> recent = new ArrayDeque<>();
    List<Turn> turns = stored.turns();
    int index = turns.size() - 1;
    while (index >= 0) {
        int cost = tokens(turns.get(index).text());
        if (used + cost > budget) {
            break;
        }
        recent.addFirst(turns.get(index));
        used += cost;
        index--;
    }

    List<Turn> messages = new ArrayList<>(recent);
    messages.add(new Turn("user", newMessage));
    return new Prompt(system, messages, recent.size(), index + 1, used);
}
```

Real output from `code/extras/ContextAssembler.java`, compiled and run without calling a model:

```text
budget=4000 used=2599 kept=40 dropped=0 last=Where is my refund?
budget=1000 used=999 kept=15 dropped=25 last=Where is my refund?
budget=300 used=295 kept=4 dropped=36 last=Where is my refund?
budget=20 rejected: Fixed context exceeds the budget: 39
```

The decision that matters is `break`, not `continue`. Skipping one long turn and keeping an older one would leave a gap in the conversation. The token count here is a rough estimate of length ÷ 4, so use the token counting API in production. A real version also needs to keep tool calls paired with their results.

## Deep dive 2: storing a billion conversations

```mermaid caption="Storage tiers. Reads try the cache first. Writes always reach the database, and the rest can be rebuilt or fetched by reference."
flowchart TB
  C[Chat service] --> R[Warm cache: recent messages]
  C --> D[Conversation database, split into shards]
  C --> F[File store: uploads]
  R -->|cache miss| D
  D --> E[Change events]
  E --> I[Search index and summaries]
  style D fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
  style R fill:#FFF3C4,stroke:#8A6D00,stroke-width:2px
```

| Decision | Recommendation | Reason |
| --- | --- | --- |
| Partition key | `conversation_id` for messages. A separate listing of chats by `user_id` | Messages are read as one conversation, and the list has its own access pattern |
| Message ID | Time-sortable | Lets each device fetch messages after the last ID it saw |
| Message shape | Written once, with `parent_id` | Supports edit and regenerate as branches |
| Large content | Store a reference, not the bytes | Keeps rows small |
| Access check | Compare the signed-in user with the chat's owner on every read | Stops one user reading another's chat |
| Deletion | Delete from every derived store too | Caches, search and summaries also hold copies |
| Encryption | At rest and in transit, with restricted staff access | Conversations are personal data |

## Deep dive 3: routing to model servers

```mermaid caption="Router decisions. Sticky routing is a preference for speed, not a requirement. Any suitable server can answer, because the request carries its context."
flowchart TB
  L[Is the user within their token limit?] -->|no| W[Ask the user to wait]
  L -->|yes| S[Choose short or long context pool]
  S --> H[Does a server with a warm cache have room?]
  H -->|yes| K[Use it: faster and cheaper]
  H -->|no| N[Use the least busy: slower, still correct]
  style W fill:#FBD5E0,stroke:#B3261E,stroke-width:2px
  style K fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
```

Anthropic's postmortem shows both the value and the risk of this. Requests sent to the wrong context pool degraded answers, and ==sticky routing meant affected users tended to keep hitting the same wrong server==. So routing is also a quality risk, and it needs continuous evaluation in production. For KV cache, batching and GPU scaling, see HLD M21D.

## Deep dive 4: streams that survive a dropped connection

| Situation | What should happen |
| --- | --- |
| User closes the tab | Stop generation and free the slot. Save the partial reply, marked as stopped |
| Mobile network drops | The client reconnects and asks for events after its last ID, or fetches the saved reply |
| User presses Send twice | The request ID makes the second press return the same turn |
| Chat server dies mid-stream | The client reconnects. Generation restarts or resumes, but must not create a second reply |
| Same chat on two devices | Devices sync by last message ID, and writes to a chat are ordered |

## Failure handling

| Failure | Effect | Response |
| --- | --- | --- |
| Web server crash | Its streams drop | Clients reconnect. No lasting data is lost |
| Cache down | Slower reads, more database load | Read from the database, with limits so that it isn't overwhelmed |
| Database shard down | Those chats are unavailable | Fail over to a replica, and return a clear error if writes can't be saved |
| Model pool overloaded | Waiting or errors | Queue with a limit, favor paid plans by policy, and shed load early |
| Worker backlog | Memory and search lag | Chat keeps working, because that path doesn't wait |
| Wrong chat loaded | Privacy incident | Check ownership at load time, test it, and alert on mismatches |
| Summary is wrong | Later answers are wrong | Keep original messages so that the summary can be rebuilt |
| Bad routing or model bug | Quality drops for some users | Continuous evaluation, and fast rollback |

**Security:** treat uploads, web pages and past chat text as untrusted data, since they can carry prompt injection. Keep user data out of shared cache keys, and don't write prompts to ordinary application logs. AI09 covers this.

## Trade-offs

| Choice | Gain | Cost | Don't use it when |
| --- | --- | --- | --- |
| Stateless app servers | Easy scaling and deploys | A store read on each turn | A small tool with one server and few users is fine |
| Cache for recent messages | Fewer database reads | Invalidation, and another copy to delete | The database already keeps up |
| Summaries | Lower token cost | Possible errors and lost detail | Exact wording matters, unless you keep key facts separately |
| Sticky routing | Better cache hits | Uneven load, and bad servers keep their users | Requests rarely share a prefix |
| Separate model pools | Protects short requests from long ones | Unused capacity in each pool | Traffic is small and uniform |
| Message threads (beta) | Less history to resend | Short expiry, so you still need your own store | You need lasting history |

**For a small app:** one Postgres database, stateless servers and the Claude API are enough. Add the cache, shards, queue and search only when measurements show that you need them.

::: real Connecting it to your earlier question
With 100 users, you can follow the same pattern with one database and two servers. At millions of users the idea doesn't change, and only the number of machines and partitions does. Bedrock, or any provider, simply replaces layer 4 if you don't run your own model servers.
:::

::: mistake Common mistakes
- Saying the model "stores" each user's context. It works from what each request carries.
- Treating sticky routing as a correctness feature. It's a speed and cost optimization.
- Treating the prompt cache as memory. It's temporary, and hits aren't guaranteed.
- Deleting a chat from the database only, while copies remain in caches, indexes and summaries.
- Trusting a conversation ID sent by the client without an ownership check.
:::

::: tip Interview line
"The context lives in partitioned storage, stateless services assemble it per turn, and model servers hold it only while the request runs. Then I'd cover token limits, routing, streaming and failure handling."
:::

## Certification lens

::: key How this note maps to CCAR-F
**Out of scope as a design:** the exam doesn't ask you to design claude.ai. Useful overlap: preserving key facts through long conversations (TS 5.1), context degradation and summaries (TS 5.4), and session handling (TS 1.7). Study AI03 L6 and AI06 L7 for those.
:::

These are original practice questions, not real or leaked.

::: qa
<div class="q">Q1 · Why can any suitable model server answer the next message in a chat?</div>
<div class="a">Because the request carries the context it needs. A warm cache on one server only makes that request faster and cheaper.</div>
:::

::: qa
<div class="q">Q2 · Using this note's assumptions, about how many replies are in flight at peak, and what does that tell you?</div>
<div class="a">6,944 per second × 15 seconds ≈ 104,000, against 1 billion stored chats. So model memory is sized by active replies, and not by total conversations.</div>
:::

::: qa
<div class="q">Q3 · A summary changes a refund amount from $84.20 to "about $80", and later answers are wrong. What's the fix?</div>
<div class="a">Keep exact values in a structured facts block that's included on every turn, outside the summary, and keep the original messages. This is the idea behind TS 5.1.</div>
:::

::: qa
<div class="q">Q4 · The search index is lost. Is that a data loss incident?</div>
<div class="a">Not if it's derived. Rebuild it from the conversation database and file store. Search is degraded in the meantime, and chat can keep working.</div>
:::

::: qa
<div class="q">Q5 · Defend a decision: why is the first message saved before generation starts?</div>
<div class="a">If the connection or model fails, the user's text is safe, and a retry with the same request ID can continue. The cost is a write on every turn, plus handling for messages that never got a reply.</div>
:::

::: revision Revision
- Claude's API is stateless by default, so the app owns context.
- Three lifetimes: lasting, warm, in flight. Only lasting is the source of truth.
- Illustrative sizing: 1 billion stored, about 104,000 in flight at peak.
- Each turn: check access, save, load, assemble, route, stream, save, publish.
- Put stable context first for caching. Keep key facts outside summaries.
- Partition messages by conversation, and check ownership on every read.
- Sticky routing saves work, and can also keep users on a bad server.
- Slow work goes through a queue, so chat keeps working during backlog.
- Delete from derived stores too.
- This is a reference design. Anthropic hasn't published claude.ai's internals.
:::

## Sources

Read on 2026-11-07. [A postmortem of three recent issues](https://www.anthropic.com/engineering/a-postmortem-of-three-recent-issues), [Using the Messages API](https://platform.claude.com/docs/en/build-with-claude/working-with-messages), [Prompt caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching), [Compaction](https://platform.claude.com/docs/en/build-with-claude/compaction), [Chat search and memory](https://support.claude.com/en/articles/11817273-use-claude-s-chat-search-and-memory-to-build-on-previous-context), [How long do you store my data](https://privacy.claude.com/en/articles/10023548-how-long-do-you-store-my-data), and [Effective context engineering](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents). Some pages were read through a summarizing fetch, so check the originals before quoting exact details. No model was called for this note.

::: notespace short Your notes
:::
</div>
