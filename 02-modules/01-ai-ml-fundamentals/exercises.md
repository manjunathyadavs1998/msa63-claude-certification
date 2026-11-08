<div class="lesson">
<div class="lesson-meta">Exercises · AI and ML Fundamentals</div>

# AI01 Exercises

Work each one before reading its solution. Exercises 1 to 4 are arithmetic, so do them on paper. Practise Exercise 6 out loud. Prices are illustrative unless a source is given.

## Exercise 1: Is the model worth it?

A bank processes **1,000,000** transactions, and **0.5%** are fraud. A model has **80% recall** and flags **1%** of legitimate transactions. Each flagged item gets a **$5** review, and each missed fraud costs **$200**.

**(a)** Fill in the confusion matrix. **(b)** Compute precision and accuracy. **(c)** Compute the total cost. **(d)** Compare it with no model, and with reviewing everything. **(e)** What does accuracy hide here?

::: answer Solution
**(a)** Fraud = 5,000. TP = 4,000, FN = 1,000, FP = 1% of 995,000 = 9,950, and TN = 985,050.

**(b)** Precision = 4,000 / 13,950 = **28.7%**. Accuracy = 989,050 / 1,000,000 = **98.9%**.

**(c)** Reviews = 13,950 × $5 = $69,750. Misses = 1,000 × $200 = $200,000. Total = **$269,750**.

**(d)** No model = 5,000 × $200 = **$1,000,000**. Reviewing everything = 1,000,000 × $5 = **$5,000,000**. The model is clearly worthwhile.

**(e)** Saying "never fraud" scores 99.5% accuracy, which beats this model, and yet costs nearly four times as much. Accuracy points to the wrong decision, so compare using costs.
:::

## Exercise 2: Thresholds and segments

**(a)** A review costs $8, and undetected fraud costs $400. What probability threshold makes review worthwhile? **(b)** A model finds 80% of card fraud (900 cases) and 5% of wallet fraud (100 cases). What's overall recall? **(c)** What would you tell leadership?

::: answer Solution
**(a)** `p × 400 > 8`, so `p > 0.02`.

**(b)** (720 + 5) / 1,000 = **72.5%**.

**(c)** The 72.5% is true, but it hides a model that's almost blind on wallets. If fraudsters discover the gap, wallet fraud will grow, and the average will then fall. Report segments separately, and don't reduce wallet review until its recall meets the target.
:::

## Exercise 3: Choose a split

A node has 10 fraud and 10 legitimate cases. A split gives one child with 8 fraud and 2 legitimate, and another with 2 fraud and 8 legitimate. Compute the parent's Gini, the weighted Gini after the split, and the reduction.

::: answer Solution
Parent: `1 − 0.5² − 0.5² = 0.50`. Each child: `1 − 0.8² − 0.2² = 0.32`. Weighted average: `0.5 × 0.32 + 0.5 × 0.32 = 0.32`. Reduction: **0.18**. A tree would compare this reduction with that of other candidate splits, and choose the largest.
:::

## Exercise 4: One training step by hand

A neuron has `w = 0.4`, `b = 0.1`, input `x = 1.5`, target `y = 0` and learning rate `0.5`. With a sigmoid and cross-entropy, compute the prediction, loss, both gradients, new parameters, new prediction and new loss.

::: answer Solution
| Step | Calculation | Value |
| --- | --- | ---: |
| z | `0.4 × 1.5 + 0.1` | 0.7000 |
| p | `σ(0.7)` | 0.6682 |
| Loss | `−ln(1 − 0.6682)` | 1.1032 |
| Gradient for w | `(p − y) × x` | 1.0023 |
| Gradient for b | `p − y` | 0.6682 |
| New w | `0.4 − 0.5 × 1.0023` | −0.1011 |
| New b | `0.1 − 0.5 × 0.6682` | −0.2341 |
| New z | `−0.1011 × 1.5 − 0.2341` | −0.3858 |
| New p | `σ(−0.3858)` | 0.4047 |
| New loss | `−ln(1 − 0.4047)` | 0.5187 |

The target was 0, the prediction fell from 0.67 to 0.40, and the loss roughly halved. This is correct behavior.
:::

## Exercise 5: Tiering by cost

You route **100,000** tickets per day. Rules handle 20%, a classifier 60%, an LLM 18%, and humans 2%. Assume an LLM call costs $0.002 and a human triage costs $1.50. Sending all tickets to the LLM still leaves 2% for humans.

**(a)** Compute the daily cost of "all LLM" and of tiering. **(b)** What's the cost if tiers cut human triage to 1%? **(c)** What should you optimize first?

::: answer Solution
**(a)** All LLM: 100,000 × $0.002 + 2,000 × $1.50 = **$3,200**. Tiered: 18,000 × $0.002 + 2,000 × $1.50 = **$3,036**.

**(b)** 18,000 × $0.002 + 1,000 × $1.50 = **$1,536**.

**(c)** Under these prices, the LLM isn't the main cost at all. Tiering saves $164 a day, while halving human triage saves $1,500. **Find the biggest cost before optimizing.** If LLM calls cost more, or quality matters more, the conclusion changes, so run the numbers using your own prices.
:::

## Exercise 6: Defend a decision

A colleague says: *"We should replace our gradient-boosted fraud model with a deep neural network. Deep learning is state of the art."* Write the answer you'd give out loud.

::: answer Model answer
*"Deep learning is the state of the art for language and images, and we should use it there. For our transaction table, it's less clear. Gradient boosting usually performs strongly on tabular data, it's easier to explain to auditors, and it costs less to train and serve.*

*So the deciding question isn't which is newer, but whether a neural network beats our model on what we care about. I'd measure recall at our operating precision, on a time-based holdout, for each payment channel.*

*If it wins by enough to justify the extra cost and reduced explainability, I'll support it. I'd change my mind if we began using unstructured inputs like free-text notes or device sequences, where neural networks have a real structural advantage. Until then, I'd invest in better features, because that's probably where the gain is."*

**Why this works:** it accepts the valid part of the premise, gives the mechanism, names a measurable test, and states what would change your mind.
:::

::: notespace short Your notes
:::
</div>
