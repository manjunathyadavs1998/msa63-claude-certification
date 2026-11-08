# Working rules: AI Engineering and Claude Architect course

**Read `00-planning/COURSE_PROGRESS.md` first, every session.** It is the resume point.

Follows the method of `../system-design-course/00-planning/AUTHORING_PLAYBOOK.md` and `../lld-course/CLAUDE.md`. Read the playbook's sections 3, 7 and 8 before authoring. This file records only what differs or was learned here.

`Idea.md` is the original brief and is read-only.

Module codes are **AI01–AI13**, so they can't collide with HLD M01–M21, LLD M22–M25, or the Java and AWS notes. Refer to other courses as "HLD M21A", "LLD M23", "Java 29".

## Hard rules

1. **Never invent** benchmarks, production architectures, citations or file paths. Label anything unverified.
2. **Read a source before citing it.** A link returning HTTP 200 proves it exists, not what it says. `00-planning/SOURCE_REGISTER.md` records what has really been read.
3. **Separate three kinds of claim:** *documented* (cite it), *engineering recommendation* (say so) and *illustrative figure* (say so).
4. **"How Anthropic solved it" must come from what Anthropic has published.** It publishes methods far more often than internal deployments. Never present a reference design as Anthropic's or any company's system.
5. **Markdown and diagram sources are truth; PDFs are generated.**
6. **Teach once, cross-reference everywhere.** Check `00-planning/CONCEPT_DEPENDENCY_MAP.md` first. HLD M21 already teaches embeddings, HNSW, LLM serving and recommendation systems. Deepen, don't restate.
7. **Recompute every number** in code before publishing, including your own.
8. **A module is never complete because a PDF exists.** It must pass the gates, including visual inspection.
9. Update `COURSE_PROGRESS.md` after every module.

## Source trust

- **The official exam guide** (`01-source-map/CCAR-F-Exam-Guide-v1.0.pdf`, SHA-256 prefix `9bac07c3e6671e55`) is the authority for exam content.
- **Current Anthropic docs** are the authority for real behavior. Where they disagree with the guide, teach both: *the exam's answer* and *what to build today*. See `01-source-map/CERTIFICATION_BLUEPRINT.md`, section 7. Re-check for a newer guide before writing AI12 and AI13.
- **The playlist** is supplementary, and only auto-captions were available.
- **The PDFs in `D:\MSA63\Notes\`** are read-only topic checklists, never sources of fact. The playbook found this kind of file contains hallucinated content. 15 of the 17 AI ones are image-only.
- **API facts:** load the `claude-api` skill first, and use exact model IDs from it. Never write SDK calls from memory.

## Every lesson's shape

The playbook's template, with three sections added for this course:

1. Cover block (for a module's first lesson), `lesson-meta`, `::: keyidea`, `.prereq`
2. **The hook:** a concrete failure, with a realistic domain
3. **Intuition:** an everyday analogy
4. **Mechanism:** how it actually works, from first principles
5. **Why the obvious fixes fall short:** a comparison table
6. **How Anthropic solved it** *(new)*: the published method, or "How the field solved it" when Anthropic hasn't published on this. Never force it
7. **The production architecture** *(new)*: baseline → improved, with its new problem → production. Component table saying why each part exists and how it communicates. Request flow. Failure table
8. **Java:** compiled, with lines ≤ 92 characters, and only the lines that carry a decision get explained
9. **Trade-offs**, and when *not* to use it
10. `::: real`, `::: mistake`, `::: tip`
11. **Certification lens** *(new)*: which task statements it supports, an honest statement if it's out of scope, and practice questions
12. `::: revision`, `::: notespace`

**This covers the 14-point format in `Idea.md`:** definition (keyidea), why introduced and problems solved (hook, obvious fixes), internals and lifecycle (mechanism), diagrams and enterprise example (architecture), Java, alternatives, advantages and limits (trade-offs), failures, security and scale (failure table, warnings), certification concepts and questions (lens).

**Diagrams:** at least 3 per concept lesson. Design problems use the ten-part template with **six** diagrams; count them, because the validator can't see a missing one.

**Module deliverables:** `notes-*.md`, `references.md`, `exercises.md` (6 worked, the last one "defend a decision"), and `07-validation/reports/AInn.md`.

## Java

Java 21 and Spring Boot 3, using `com.anthropic:anthropic-java`. Realistic domains only, with no Animal, Dog, Vehicle or Shape examples. Compile everything. Run what can be run, and quote real output.

**State honestly in the lesson:** any code calling the API that has only been compiled, because no API key is available. Gradle isn't installed, so verification uses Maven. The notes may show Gradle coordinates as well.

## Diagrams, learned here

Everything in the playbook section 7 applies, including no `subgraph`, chains run `TB`, and no spatial captions. Added:

- ==**Color nodes with `style X fill:#...,stroke:#...,stroke-width:2px`. Never use `classDef`.**== In the hand-drawn renderer, `classDef` turns fills into dark hatching with unreadable labels. The validator reports no issue. Found in the pilot, Figures 1–3.
- Never attach `:::class` to an edge target.

Colors: problem `#FBD5E0` / `#B3261E`, fix `#CDEFD6` / `#1B6B4A`, caveat `#FFF3C4` / `#8A6D00`.

## Markers

`==highlight==` for load-bearing claims. Never nested, an even count per paragraph, none inside `::: revision`, about 0.5–1 per page. Markdown doesn't parse inside HTML `<div class="q">`, so use `<code>` and `<b>` there.

## Questions

Follow `00-planning/CERTIFICATION_QUESTION_PLAN.md`. All questions are original. Never present any as real or leaked.

## Pipeline

Forked from `../lld-course`. **Don't run `npm install`**: `node_modules` is a junction to `../system-design-course/node_modules`, and changing it would affect those courses' builds. WeasyPrint doesn't work here.

```bash
python -I -X utf8 scripts/check_marks.py 02-modules/<dir>/notes-X.md
node scripts/build_pdf.mjs --in 02-modules/<dir>/notes-X.md \
     --out 05-pdf-output/modules/<NAME>.pdf \
     --title "<Title>" --module "AI05 - RAG" --style notebook
python -I -X utf8 scripts/add_bookmarks.py 05-pdf-output/modules/<NAME>.pdf 02-modules/<dir>/notes-X.md
python -I -X utf8 scripts/validate_pdf.py 05-pdf-output/modules/<NAME>.pdf 07-validation/<ainn>/pages
```

Never hide build output. A failed render leaves the old PDF in place, and validation then passes on a stale file. Then **open every diagram page and the densest code page**.

## Gates

| Gate | Check |
| --- | --- |
| Content | Every architectural change states the problem, what's added, the new bottleneck and the simpler alternative. Sources in `references.md` were read |
| Diagrams | Counted, rendered and inspected |
| Code | Compiled; real output only; lines ≤ 92 |
| PDF | `issues: []`, bookmarks 0 missed, pages looked at |
| Learning | Revision per lesson, module revision, self-assessment, 6 exercises with solutions |
| Questions | Answer letters balanced, distractor types varied, TS coverage updated |
