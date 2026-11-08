# Validation Report: Format Pilot (AI05 L3, Contextual Retrieval)

| Gate | Result |
| --- | --- |
| Content | All figures come from the Anthropic post, which was read. Percentages match its failure rates (35.1%, 49.1% and 66.7%, rounded to 35, 49 and 67). The production design is labeled as a recommendation |
| Diagrams | 5 rendered. Pages 6, 8, 9 and 10 inspected |
| Code | `ChunkContextualizer` compiles against `anthropic-java` 3.2.0 and **has not been run**, since no API key is available. `RankFusion` was run, and the quoted output is real. Lines ≤ 92 |
| PDF | A4, 16 pages, `issues: []`, 11 bookmarks with 0 missed. Pages 1, 6, 8, 9, 10, 12 and 15 inspected |
| Questions | 3 samples. Not a full question set |

## Defects found by looking, not by the validator

| Defect | Fix |
| --- | --- |
| Figures 1–3 had dark, unreadable nodes, caused by `classDef` in the hand-drawn renderer | Use `style` lines. Now a rule in `CLAUDE.md` |
| Figure 3's edge label was crossed by its own line | Replaced with a separate node stating the new problem |
| Q3 showed literal backticks, because Markdown isn't parsed inside the HTML question block | Use `<code>`. Now a rule in `CLAUDE.md` |

## Known issues left for the real AI05 build

- The "Failures and how they're contained" heading sits alone at the bottom of page 10.
- Pages 8 and 9 have large blank areas, because each large figure moves to a new page.
- This pilot is shorter than a full lesson. It has no cover contents page, `references.md` or exercises.

## Reused from the HLD and LLD courses

Notebook CSS, build script, bookmarks, validator and marker checker. Copied, so no sibling course's files were changed. `node_modules` is a junction to `../system-design-course/node_modules`.
