<div class="cover">
<div class="eyebrow">AI Engineering and Claude Architect · Part V, Certification</div>

# Certification Exercises

## Six Worked Problems for the Final Stretch

<div class="sub">Each one practises a decision the exam tests, in a form that makes you reason instead of recognize. Try each before reading its solution.</div>

<table class="meta">
<tr><td>Module</td><td>AI13 of 13</td></tr>
<tr><td>Covers</td><td>AI13 L1–L5, across all domains</td></tr>
</table>
</div>

<div class="lesson">

# Exercise 1: Find the constraint

For each stem, name the deciding word, and the kind of answer it points to.

a) "Refunds above $500 must never be processed by the agent."

b) "What's the most effective first step to improve tool selection?"

c) "The audit runs every weekend, and results are read on Monday."

d) "A new team member doesn't receive the conventions."

::: answer Solution
a) "Never" points to code, such as a hook or a gate.

b) "First" points to the simplest root-cause fix: better tool descriptions, ahead of classifiers or new agents.

c) "Weekend" and "Monday" are latency-tolerant, so use the Batch API.

d) "New team member" points to scope, so the instructions belong in project configuration under version control, and not in `~/.claude/`.
:::

# Exercise 2: Read the SDK configuration

```python
options = ClaudeAgentOptions(
    allowed_tools=["Read", "Grep"],
    agents={"tester": AgentDefinition(
        description="Runs tests",
        prompt="Run tests and report.",
    )},
)
```

The coordinator never delegates. When it's told to delegate, the `tester` can edit files, which it shouldn't. Find two problems, in terms of what the exam guide teaches.

::: answer Solution
**1.** The spawning tool isn't in `allowed_tools`. The guide's answer is to add `"Task"`, while current docs name it `"Agent"`.

**2.** `tester` has no `tools` list, so it inherits all the tools available to subagents. Add `tools=["Bash", "Read", "Grep"]`, as the guide's scoped access requires. A better `description` would also say when to use it, and that's the first thing to fix if delegation still misfires.
:::

# Exercise 3: Classify the failure

Your pipeline failed on these. For each, retry with feedback or not, and what next?

a) A date is written as "04-03-2026", but the rule says ISO 8601.

b) The insurer's name appears only in an attachment you didn't receive.

c) Line items sum to 300, and the stated total on the document says 310.

d) `calculated_total` is 280, although the line items sum to 300.

::: answer Solution
a) Retry with the specific error. It's a format problem. The date could be ambiguous, so the normalization rule should say which order applies, or the field should be flagged.

b) Don't retry. Request the attachment, or return null for review.

c) Don't retry. The document itself disagrees, so keep both values, and set `conflict_detected`.

d) Retry with the specific error. The model's own computed value is wrong, and it can be corrected from the source.
:::

# Exercise 4: Compute the batch plan

Each document must be finished within 40 hours, and batches take up to 24 hours. Failed documents get one resubmission, as a batch.

a) What's the longest window with no resubmission?

b) With one resubmission as a full batch, is any window safe?

c) What would you change?

::: answer Solution
a) 40 − 24 = 16 hours.

b) No. Two worst-case batches take 48 hours, which exceeds 40 hours before you've even added a window.

c) Resubmit failures through the synchronous API, or accept that the guarantee holds only for the first pass. Measured batch times may be well under 24 hours, but only the bound is a guarantee. The arithmetic is simple. The point is to use the worst case.
:::

# Exercise 5: Repair the scenario answer

A colleague marked these answers on a practice S3 item, in which a report silently lacks a subtopic after a search timeout. Say why each is wrong, and give the guide's answer.

a) "Have the subagent return an empty list, so the workflow continues."

b) "Abort the workflow, because the report can't be complete."

c) "Return 'search unavailable' to the coordinator."

::: answer Solution
a) That's silent suppression. A valid empty result and a failure look identical, so the coordinator can't react.

b) That's the opposite anti-pattern: it discards all the other work.

c) It's a generic status that hides what's useful.

**The guide's answer:** retry locally for the transient failure, then propagate failure type, attempted query, partial results and alternatives. The coordinator can re-delegate, or continue, and the synthesis annotates the coverage gap.
:::

# Exercise 6: Defend a decision

Your manager says: "You score 85% on the mocks. Skip the remaining study and book the exam tomorrow." Do you agree? Write a short reply that uses evidence from your own results, and that shows you understand what a mock score does and doesn't show.

::: answer Model answer
*"I'd like to check three things first. The guide doesn't publish how raw scores map to the scaled 720, so 85% on my own mocks doesn't predict a pass. These items are also my study material, and candidates describe the real exam as harder and wordier. So I'm looking at my by-domain results and my timing. If every domain is above about 70% on a first attempt, if I finished within time, and if my error log is no longer showing the same distractor families, I'll book it. If Claude Code configuration is still weak, I'll spend a few days there first, because that's a 20% domain, and the exam is criterion-referenced, so I can't assume strong domains will carry a weak one. A failed attempt costs \$125 and a retake wait, so a short delay is the cheaper risk."*

**Why it's strong:** it makes a decision with conditions, it states what the evidence can't show, and it uses the guide's facts. The thresholds are personal study guidance, and the answer doesn't present them as official. Disagreeing or agreeing is fine, as long as it's on this reasoning.
:::

::: notespace short Your notes
:::

</div>
