# Validation Report: 500-Question Self-Evaluation Bank

Written at the user's request on 2026-11-06, ahead of AI12. It's a standalone bank, and AI13's lessons and mocks are still to be written.

Source: `02-modules/13-certification/bank/d1.txt`–`d5.txt`. Generated file: `question-bank-500.md`. Built by `scripts/build_question_bank.py --write`, with `--tells` to list length give-aways.

| Check | Result |
| --- | --- |
| Count | 500, with no duplicate stems |
| Domains | 135 / 90 / 100 / 100 / 75, matching the guide's 27 / 18 / 20 / 20 / 15% |
| Task statements | All 30 covered, from 9 (TS 5.6) to 22 (TS 1.3) |
| Formats | 443 single-answer and 57 select-2, the only two formats the exam uses |
| Scenarios | All 6: 101 / 101 / 89 / 72 / 72 / 65 |
| Difficulty | 81 easy, 237 medium, 182 hard, by my own labelling |
| Answer letters | About 25% each overall and in every domain |
| Key | A script asserts that each key entry points at the options marked correct |
| Explanations | Every question has one. 121 thin ones were rewritten to give reasons |
| PDF | `issues: []`, 0 bookmarks missed. Sample pages viewed |

## Defects found and fixed

| Defect | How found | Fix |
| --- | --- | --- |
| Correct option was the longest in 53% of items, a real give-away | Script | Shortened 61 options. Now 40% |
| Answer letters were uneven across domains | Script | Dealt from a shuffled deck per domain |
| 121 explanations only said things like "named in the guide" | Listing the shortest | Rewritten with reasons |
| A question's options could split across pages | Viewing pages | Kept together |
| Answer key was one table that left a blank page | Viewing pages | Split into tables that fit |
| Items echoed guide samples (85%, 55%, `/review`, a hanging CI job) | Rereading the samples | Reworded |

## Known limits

- **Questions are original, and written from the public guide. None is a real or leaked item, and no score predicts an exam result.**
- **No item has been tried on real candidates.** Difficulty labels are my judgment, and there's no statistical analysis of how each item performs.
- **Picking the longest option would still score about 40% on single-answer items,** against 25% by chance, though most of those margins are a few characters.
- Real exam items may be longer, and they come grouped by scenario.
- Answers follow guide v1.0 (July 2026). *Build today* notes mark known differences from current products. The certification page showed no version number, so I couldn't confirm that v1.0 is the latest.
- A few explanations rest on my prior knowledge of Claude Code rather than a page read for this course, such as the `@path` import form.
- AI12 isn't written yet, so some Domain 3 items cover material the course hasn't taught in depth.
