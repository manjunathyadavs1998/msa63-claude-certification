<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Part I, Foundations</div>

# Generative AI and LLMs, Lesson 4

## How an LLM Is Trained

<div class="sub">Ask a freshly trained model "What's the capital of France?" It might answer with "What's the capital of Germany?" It isn't broken. It learned to continue text, and turning that into an assistant is a separate problem, and the place where Anthropic's most distinctive work begins.</div>

<table class="meta">
<tr><td>Module</td><td>AI02 of 13</td></tr>
<tr><td>Lesson</td><td>4 of 7</td></tr>
<tr><td>Prerequisites</td><td>AI01 L1, AI01 L3, AI02 L3</td></tr>
<tr><td>Exam relevance</td><td>RLHF, Constitutional AI and training are explicitly out of scope. Valuable for interviews and judgment</td></tr>
<tr><td>Reading time</td><td>~45 minutes</td></tr>
</table>
</div>

<div class="toc">

## Contents

1. [How an LLM Is Trained](#how-an-llm-is-trained)

</div>

<div class="lesson">
<div class="lesson-meta">Lesson 4 of 7 · Generative AI and LLM Fundamentals</div>

# How an LLM Is Trained

::: keyidea
==**Training happens in two very different stages. Pre-training teaches a model to predict text, and alignment teaches it to behave like a helpful assistant.**== Pre-training is self-supervised, using raw text. Alignment needs a signal for what "good" means. RLHF gets that from human rankings. ==**Anthropic's Constitutional AI replaces the harmlessness labels with AI feedback guided by written principles**==, reducing the need to scale up human labeling of harmful content, and making the goals more explicit.
:::

<div class="prereq"><b>Before:</b> self-supervised learning (AI01 L1), cross-entropy and backpropagation (AI01 L3), the transformer (AI02 L3). <b>Teaches:</b> pre-training, supervised fine-tuning, RLHF, Constitutional AI, Claude's constitution, sycophancy. <b>Used by:</b> AI02 L6 (why models fail), AI09 L5 (guardrails), AI11 L1 (fine-tuning), AI11 L3 (alignment research).</div>

## The hook: a model that won't answer

A model finished pre-training yesterday. It has read a colossal amount of text, and its next-token predictions are excellent. You ask:

> *"What's the capital of France?"*

It may well continue with:

> *"What's the capital of Germany? What's the capital of Spain?"*

That's a perfectly good continuation, since quiz pages look exactly like that. **It's predicting text, not helping you.** It also has no reason to avoid harmful content, since harmful content is part of the internet too.

::: key
==**Capability and behavior are separate problems.**== [InstructGPT](https://arxiv.org/abs/2203.02155) showed this dramatically: in human evaluations, people preferred a 1.3-billion-parameter aligned model to the 175-billion-parameter GPT-3, despite it having what the paper calls *"100x fewer parameters"*.
:::

## Intuition: education, then professional training

Pre-training is like a child reading every book in the library, gaining a broad knowledge of the world, good and bad. Alignment is the later professional training: *here's how we talk to customers, here's what we don't do, here's when you ask for help.*

A brilliant graduate with no training isn't yet someone you'd put on a support desk. Nor can professional training replace an education. You need both.

## Stage 1: Pre-training

### The objective

**Predict the next token.** It's self-supervised (AI01 L1), since the text supplies its own labels. Shift the text by one, and each token's target is the one after it.

| Input | The | refund | was | approved |
| --- | --- | --- | --- | --- |
| **Target** | refund | was | approved | today |

The loss is cross-entropy, computed at every position. There are no human labels, which is why training on such vast text is possible at all.

### The smallest possible example

This is a language model trained the same way, reduced to counting: for each word, how often does each next word follow it? Real output from `code/ai02-llm/TinyLm.java`, trained on 9 invented policy sentences:

```text
vocabulary = 28, guessing loss = ln(V) = 3.332
cross-entropy on training text = 2.379
cross-entropy on holdout text  = 2.421
```

Random guessing among 28 words gives a loss of `ln(28) = 3.332`. The model does better, on both training text and unseen text. It learned something that generalizes. An LLM does this using a transformer instead of a table, so its prediction can depend on all the context, and not only on the previous word.

```java
void train(List<String> sentences) {
    for (String sentence : sentences) {
        List<String> words = wrap(sentence);
        vocabulary.addAll(words);
        for (int i = 0; i + 1 < words.size(); i++) {
            counts.computeIfAbsent(words.get(i), w -> new HashMap<>())
                    .merge(words.get(i + 1), 1, Integer::sum);
        }
    }
}
```

### The real scale

```mermaid caption="The general shape of pre-training, as described across published work. Each lab's actual pipeline differs, and many details aren't public."
flowchart TB
  D[(Raw text)] --> C[Clean, filter and tokenize]
  C --> L[Distributed training on many GPUs]
  L --> K[(Checkpoints)]
  L --> E[Evaluate on held-out text]
  E --> M[Pre-trained model]
```

| Component | Why it exists |
| --- | --- |
| Data cleaning | Removes duplicates and low-quality text, because memorized repeats harm generalization |
| Distributed GPUs | No single machine can hold the model and data or finish in reasonable time |
| Checkpoints | Hardware fails during long runs (AI01 L3) |
| Evaluation | Tracks loss on held-out text, and tests for dangerous capabilities |

The [scaling laws work](https://arxiv.org/abs/2001.08361) explains why labs pay for this: loss falls predictably as size, data and compute grow.

::: warn What's public and what isn't
Anthropic doesn't publish its pre-training data details, cluster design or costs. ==**Any specific claim about Claude's training scale that you read in a handbook or blog is unverified unless it cites Anthropic.**== This lesson makes none.
:::

## Stage 2: Alignment

### Why it's hard

"Helpful and harmless" isn't a label sitting in the data. Somebody has to define it, in a form that a loss function can use.

| Option | Idea | Problem |
| --- | --- | --- |
| Hand-written filters on output | Block bad strings | Brittle, and don't teach helpfulness |
| Supervised fine-tuning on good examples | Show ideal chats | Writing them is costly, and "ideal" is hard to enumerate |
| **RLHF** | Learn from human preferences | Needs many human judgments, including on harmful content |
| **Constitutional AI** | Learn from AI judgments, guided by principles | Quality depends on the principles and on the model's judgment |

### RLHF

**Reinforcement learning from human feedback** has three steps.

```mermaid caption="RLHF. Humans rank pairs of responses, a preference model learns to predict their choices, and that model then acts as the reward signal for training."
flowchart TB
  P[Pre-trained model] --> A[Generate pairs of responses]
  A --> H[Humans pick the better one]
  H --> R[Train a preference model]
  R --> L[Use it as the reward in RL]
  L --> M[Aligned model]
  style H fill:#FFF3C4,stroke:#8A6D00,stroke-width:2px
```

Why build a preference model, rather than having humans judge everything? Because RL needs an enormous number of judgments, and a model can score responses all day, while humans can't.

Anthropic's 2022 paper [Training a Helpful and Harmless Assistant](https://arxiv.org/abs/2204.05862) applied this approach. It reports that alignment training improved results on nearly all NLP evaluations, without compromising training for specialized skills like coding and summarization.

### RLHF's problems

| Problem | Why it matters |
| --- | --- |
| Human labeling doesn't scale cheaply | Every new model and policy needs more judgments |
| Reviewers must read harmful content | There's a real human cost |
| Goals are implicit | The values live in thousands of individual rankings, which are hard to inspect or change |
| Tension with helpfulness | A model can learn that refusing is the safe choice |
| Raters prefer agreeable answers | This produces sycophancy, covered below |

## How Anthropic solved it: Constitutional AI

Anthropic's response is [Constitutional AI: Harmlessness from AI Feedback](https://arxiv.org/abs/2212.08073) (Bai and colleagues, 2022). The goal it states is *"a harmless but non-evasive AI assistant"*, one that engages with harmful queries by explaining its objections rather than simply refusing. The abstract says human oversight comes only through *"a list of rules or principles"*, and that's where "constitution" comes from.

It has two phases.

```mermaid caption="Constitutional AI's two phases. In the supervised phase the model critiques and revises its own responses. In the RL phase, AI preferences replace human labels as the reward signal."
flowchart TB
  C[Written principles] --> S1[SL phase: critique and revise own responses]
  C --> R1[RL phase: AI judges which response is better]
  S1 --> F[Fine-tune on the revised responses]
  F --> R1
  R1 --> P[Train a preference model on AI choices]
  P --> A[RL from AI feedback]
  style C fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
```

| Phase | What happens | What replaces human harm labels |
| --- | --- | --- |
| **Supervised (SL)** | Model answers, critiques its answer against a principle, revises, and is fine-tuned on the revision | Self-critique |
| **Reinforcement (RL)** | A model chooses which of two samples is better, these choices train a preference model, and that becomes the reward. The paper calls this RLAIF | AI preferences |

The paper also notes that both phases can use chain-of-thought-style reasoning, improving human-judged performance and the transparency of decisions.

### What this solves, and what it doesn't

| Solves or reduces | Doesn't solve |
| --- | --- |
| Need for human labels on harmful outputs | Choosing the right principles, which remains a human responsibility |
| Goals hidden in rankings: principles can be read and edited | Guarantees: it's still probabilistic behavior |
| Evasive refusals, by design | All failure modes. AI02 L6 and AI09 L5 cover what remains |

Note also that the abstract's scope is harmlessness labels. It doesn't claim to replace all human involvement, and helpfulness data can still come from people.

### From principles to Claude's constitution

Anthropic now publishes [Claude's constitution](https://www.anthropic.com/constitution), which it calls *"the final authority"* on its vision for Claude. The document says it *"plays a crucial role in our training process, and its content directly shapes Claude's behavior."*

Its priority order, for cases of conflict:

| Order | Property |
| ---: | --- |
| 1 | Broadly safe, meaning not undermining appropriate human oversight of AI |
| 2 | Broadly ethical |
| 3 | Compliant with Anthropic's guidelines |
| 4 | Genuinely helpful |

The document says that this prioritization is *"holistic rather than strict"*, and that Anthropic *"generally favor[s] cultivating good values and judgment over strict rules"*, while trying to explain the rules it does have.

::: real
That choice has an engineering parallel you'll see again. A rule tells a model what to do in the cases you predicted, and an explained reason helps it generalize. AI03 L2 recommends the same thing for your own prompts: explain why an instruction exists.
:::

### Sycophancy: a side effect of learning from preferences

Anthropic's [Towards Understanding Sycophancy](https://arxiv.org/abs/2310.13548) studied a specific failure: *"when a response matches a user's views, it is more likely to be preferred."* The paper finds that humans and preference models sometimes favor persuasive, agreeable answers over accurate ones, and that optimizing against those preferences can trade truthfulness for sycophancy.

**Why this matters to you:** asking a model *"are you sure?"* or stating your own opinion first can bias it. Design around that in AI03 L2 and AI12 L5.

## Other alignment methods

| Method | Idea | Note |
| --- | --- | --- |
| **DPO** (direct preference optimization) | Trains directly on preference pairs, without RL | Used to make aligned fine-tunes simpler |
| **Instruction tuning** | Supervised fine-tuning on instruction-response pairs | Common for open models |
| **Red-teaming** | Attacks the model to find failures | Used to guide training and evaluation |

These come up again in AI11 L1.

## What this means in production

You won't train Claude. But the production architecture you'll build rests on one consequence of this lesson.

**Baseline: trust the model's alignment.** Your system relies on Claude's training to prevent every bad outcome. Alignment is a strong default, and it isn't an access-control system.

```mermaid caption="Baseline. The model's training is the only safeguard, so any failure reaches the business system."
flowchart TB
  U[User] --> M(Claude)
  M --> B[Refund service]
  style M fill:#FBD5E0,stroke:#B3261E,stroke-width:2px
```

**Improved: add a strong system prompt.** This helps with your specific policies, which Claude's training can't know. New problem: ==**it's still probabilistic**==, and it can be overridden by hostile input.

```mermaid caption="Improved. The prompt adds your business policies, but it still can't guarantee them."
flowchart TB
  U[User] --> S[System prompt with policies]
  S --> M(Claude)
  M --> B[Refund service]
  M --> P[New problem: still no guarantee]
  style P fill:#FFF3C4,stroke:#8A6D00,stroke-width:2px
```

**Production: layered responsibility.** This is a recommended reference design, not a specific company's system.

```mermaid caption="Production. Each layer handles what it's good at. Model training gives good defaults, your prompt gives policy, and code gives guarantees."
flowchart TB
  U[User] --> G[Gateway: auth and limits]
  G --> M(Claude with system prompt)
  M --> V[Tool validation and hooks in code]
  V -->|allowed| B[Business services]
  V -->|needs approval| H[Human approval]
  B --> A[(Audit log)]
  style V fill:#CDEFD6,stroke:#1B6B4A,stroke-width:2px
```

| Layer | Who built it | What it provides | What it can't do |
| --- | --- | --- | --- |
| Model alignment | Anthropic | Sensible defaults, such as refusing some harmful requests | Know your policies, or guarantee anything |
| System prompt | You | Your policies and tone | Guarantee compliance |
| Hooks and validation | You | Deterministic rules | Exercise judgment |
| Human approval | Your organization | Accountability | Scale cheaply |

AI06 L8 and AI09 build this for real.

## Java

Training isn't a Java task. Here's where a Java team really touches this topic.

| Task | Approach |
| --- | --- |
| Pre-training or alignment | Not applicable, unless you work at a lab |
| Fine-tuning an open model | Python tools, usually (AI11 L1) |
| Enforcing policy around Claude | Java services, hooks and validation (AI08 L3) |
| Handling Claude's decision to decline | Check `stopReason()` for `refusal` before reading content (AI03 L1) |

## Trade-offs

| Decision | Gain | Cost |
| --- | --- | --- |
| RLHF | Reflects real human preferences | Labeling cost, implicit goals, sycophancy risk |
| Constitutional AI | Explicit, scalable harmlessness feedback | Depends on principles and on model judgment |
| Rules in a constitution | Predictable behavior | Rigid in unforeseen cases |
| Judgment and explained values | Generalizes to new cases | Less predictable, which is why you add your own controls |

**When alignment isn't the right lever for you:** nearly always, since you can't change Claude's training. Use prompts, tools and code.

::: mistake
1. **Treating alignment as a security boundary.** Enforce permissions in code.
2. **Saying Constitutional AI removes humans.** Humans write the principles, and the abstract's claim is limited to harm labels.
3. **Confusing a system prompt with the constitution.** Yours configures your deployment. The constitution shapes training.
4. **Quoting unsourced training costs or data sizes.** Anthropic hasn't published them.
5. **Leading with your own opinion when asking for a review.** That invites sycophancy.
:::

::: tip What to say in an interview
Compare instead of defining: *"Pre-training teaches prediction, and alignment teaches behavior. RLHF learns preferences from people, which is effective but hard to scale, and it can reward agreeableness. Anthropic's Constitutional AI uses written principles and AI feedback for harmlessness, so goals are explicit. In my own systems, none of that replaces code enforcement."*
:::

## Certification lens

::: key How this lesson maps to CCAR-F
==**Constitutional AI, RLHF, safety training methods and fine-tuning are all explicitly out of scope.**== You won't be asked to compare them on the exam. The tested consequence is TS 1.4 and TS 1.5: prompt-level behavior has a non-zero failure rate, so guaranteed rules need code.
:::

These are original practice questions for understanding, not exam simulations.

::: qa
<div class="q">Q1 · Match the following · Medium. Match each method to its signal for "good". Methods: (1) pre-training, (2) RLHF, (3) Constitutional AI's RL phase. Signals: (a) preferences from a model judging responses, (b) the next token in raw text, (c) preferences learned from human rankings.</div>
<div class="a">1–b, 2–c, 3–a.</div>
:::

::: qa
<div class="q">Q2 · True or false · Easy. Because Claude is trained with Constitutional AI, you don't need permission checks on its tool calls.</div>
<div class="a"><b>False.</b> Training shapes probabilistic behavior. It doesn't know your authorization rules, and it can't guarantee anything. Enforce permissions in code.</div>
:::

::: qa
<div class="q">Q3 · Scenario · Hard. A reviewer asks Claude: "I'm sure this design is correct, right?" and gets strong agreement. What's the risk, and how would you rephrase?</div>
<div class="a">Sycophancy: Anthropic's research found that preference-based training can reward agreement. Ask neutrally, without stating your view, for example: "List the three biggest risks in this design." For code, use an independent review session without the generator's context (AI12 L5).</div>
:::

::: revision Revision — Lesson 4
- Pre-training learns next-token prediction from raw text, with no human labels.
- A pre-trained model continues text. It isn't yet an assistant.
- InstructGPT: humans preferred a 1.3B aligned model to the 175B GPT-3.
- RLHF: humans rank pairs, a preference model learns the rankings, and RL uses it as reward.
- RLHF's limits: labeling cost, implicit goals and reviewers' exposure to harm.
- Constitutional AI: an SL phase (self-critique and revision), and an RL phase (AI preferences, called RLAIF).
- Its goal is a harmless but non-evasive assistant, with oversight through written principles.
- Claude's constitution prioritizes broad safety, then ethics, then guidelines, then helpfulness, holistically.
- Sycophancy: preference training can reward agreeable answers over accurate ones.
- Alignment gives defaults, not guarantees. Enforce rules in code.
:::

::: notespace short Your notes
:::
</div>
