# AI01: References and Verification

Numbers are recomputed by `scripts/verify_ai01.py`, or come from running `code/ai01-ml/`. Last checked 2026-11-04.

## Demo output

All demos use **synthetic, invented data** to show a mechanism. Their numbers say nothing about real fraud systems. Each is deterministic, using fixed seeds.

| Lesson | File | Status |
| --- | --- | --- |
| L1 | `FraudThreshold.java` | Run, and output quoted exactly |
| L2 | `FraudMetrics.java` | Run, and output quoted exactly |
| L3 | `TinyNeuralNet.java` | Run, and output quoted exactly |
| L4 | `TicketVectors.java` | Run, and output quoted exactly. Corpus written to make the mechanism visible, so 1.000 isn't a realistic similarity |

Run any of them using `java <File>.java` with JDK 21. No dependencies.

## Hand-derived figures

| Claim | Recomputed | Verdict |
| --- | --- | --- |
| L1: 0.1% of a million = 1,000 | 1,000 | ✓ |
| L2: "never fraud" accuracy | 1 − 412/20,000 = 0.9794 | ✓ |
| L2: Gini of 8/2 and 50/50 | 0.32, 0.50 | ✓ |
| L2: threshold $5 / $200 | 0.025 | ✓ |
| L2 Q2: accuracy, precision, recall | 0.93, 0.80, 0.40 | ✓ |
| L3: worked example | p 0.7311, loss 0.3133, gradient −0.5379, w 0.5538, then p 0.7517, loss 0.2854 | ✓ |
| L3: cross-entropy at 0.9 and 0.01 | 0.105, 4.605 | ✓ |
| L3: 17 parameters | 4 × 3 + 5 | ✓ |
| L3 Q1 | 1.1345 | ✓ |
| L4: IDF for "refund" and "the" | 1.386, 0.182, ratio 7.6 | ✓ |
| L4 Q2 | 4.605, 0.693, ratio 6.64 | ✓ |
| Exercises 1–5 | All match the script's output | ✓ |

## Sources

| Claim | Source | How it was checked |
| --- | --- | --- |
| Google's Rules 1 and 3 | [Rules of Machine Learning](https://developers.google.com/machine-learning/guides/rules-of-ml) | Read those rules |
| Anthropic's guidance on evals | [Develop tests](https://platform.claude.com/docs/en/test-and-evaluate/develop-tests) | Read the relevant sections |
| No Anthropic embedding model; Voyage AI | [Embeddings guide](https://platform.claude.com/docs/en/build-with-claude/embeddings) | Read |
| word2vec: two architectures, 1.6B words in under a day | [Mikolov et al. 2013](https://arxiv.org/abs/1301.3781) | Read the abstract |
| Fixed-length vector bottleneck | [Bahdanau et al. 2014](https://arxiv.org/abs/1409.0473) | Read the abstract |
| Backpropagation, 1986 | [Rumelhart, Hinton, Williams](https://www.nature.com/articles/323533a0) | Title confirmed; paper not read |
| AlexNet as a turning point | [Krizhevsky et al. 2012](https://papers.nips.cc/paper/2012/hash/c399862d3b9d6b76c8436e924a68c45b-Abstract.html) | Abstract read. "Turning point" is the common view, not the paper's claim |
| Adam | [Kingma, Ba](https://arxiv.org/abs/1412.6980) | Title confirmed |
| Metric definitions | [scikit-learn guide](https://scikit-learn.org/stable/modules/model_evaluation.html) | Link checked |

## Stated from general knowledge

These are standard textbook facts, not checked against a specific source here:

- Definitions of activations, losses, optimizers and regularization techniques.
- The LSTM's authors and date (Hochreiter and Schmidhuber, 1997). The publisher's page blocked the automated check.
- The tezgüino example, a classic in the distributional semantics literature.
- Negative sampling following later word2vec work.
- ONNX Runtime having a Java API, XGBoost having JVM bindings, and DJL's existence. **Check the current versions before relying on them.**

## Engineering recommendations, not facts

- Starting tabular work with logistic regression, then gradient boosting.
- Every production architecture in this module. **None describes a particular company's system.**
- The release ladder of offline, shadow, canary and monitoring.
- The tiered ticket-routing design.

## Topic check against your PDFs

Checked against `ML Algorithms .pdf` and `Deep learning MasterNote.pdf`, using page thumbnails as topic checklists only.

| Their topic | Covered in |
| --- | --- |
| Linear and logistic regression, trees, random forest, KNN, SVM, K-means, Naive Bayes, gradient boosting, AdaBoost | L2 |
| PCA (listed in the PDF's contents, though no page was spotted for it) | L2 |
| Perceptron, activations, forward and backward propagation, loss functions, optimizers, overfitting, regularization | L3 |
| CNNs | L3, briefly. Image work isn't this course's focus |
| RNN, LSTM, GRU, autoencoders | L4 |
| Attention, transformers | AI02 L2–L3 |
| Generative models (GAN, VAE, diffusion) | AI02 L7 |

Their PDF repeats decision trees and random forests, and its cover says 11 algorithms while it has 12 pages of them. No facts were taken from them.

## Version-sensitive items

None of significance. API behavior starts in AI03.
