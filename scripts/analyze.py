"""Map playlist transcripts to CCAR-F task statements by keyword evidence."""
import json
import pathlib
import re
import sys

root = pathlib.Path(sys.argv[1])
TS = {
 "1.1": r"stop[_ ]reason|end[_ ]turn|agentic loop|iteration cap",
 "1.2": r"hub[- ]and[- ]spoke|coordinator|narrow decomposition|refinement loop",
 "1.3": r"task tool|allowed ?tools|agent ?definition|isolated context|inherit",
 "1.4": r"prerequisite|programmatic|handoff|hand off|deterministic|non-?zero failure",
 "1.5": r"post ?tool ?use|pre ?tool ?use|\bhooks?\b|normali[sz]",
 "1.6": r"prompt chain|dynamic(ally)? decompos|adaptive|task decomposition|attention dilution",
 "1.7": r"--?resume|resum(e|ption)|fork[_ ]session|stale|named session",
 "2.1": r"tool description|misrout|overlap|splitting tools|split(ting)? (the |a )?generic",
 "2.2": r"is ?error|transient|retry?i?able|error ?category|business error",
 "2.3": r"tool[_ ]choice|forced tool|scoped|too many tools|least privilege",
 "2.4": r"mcp\.json|claude\.json|expansion|mcp resource|\bresources?\b|community (mcp )?server",
 "2.5": r"\bgrep\b|\bglob\b|built[- ]in tool|edit tool|read tool|unique (text|match)",
 "3.1": r"(claude|cloud|clawed)\.md|@ ?import|/memory|slash memory|\.claude/rules|hierarchy",
 "3.2": r"slash command|skill\.md|context:? ?fork|allowed-tools|argument[- ]hint|\bskills?\b",
 "3.3": r"path[- ]specific|path[- ]scoped|front ?matter|glob pattern",
 "3.4": r"plan mode|direct execution|explore (sub ?agent|agent)",
 "3.5": r"interview pattern|test[- ]driven|input.{0,10}output example|iterative refinement",
 "3.6": r"-p flag|print flag|output[- ]format|json[- ]schema flag|ci/?cd|pipeline|headless",
 "4.1": r"explicit criteria|false positive|precision|be conservative",
 "4.2": r"few[- ]shot",
 "4.3": r"json schema|nullable|structured output|\benum\b|strict",
 "4.4": r"validation|retry with|error feedback|detected[_ ]pattern|semantic error|calculated[_ ]total",
 "4.5": r"batch|custom[_ ]id|50 ?(%|percent)",
 "4.6": r"independent (review|instance)|multi[- ]pass|self[- ]review|attention",
 "5.1": r"case facts|lost in the middle|progressive summar|trim|position",
 "5.2": r"escalat|sentiment|multiple (customer )?match|policy gap",
 "5.3": r"error propagat|access failure|empty result|partial result|coverage (gap|annotation)",
 "5.4": r"scratch ?pad|/compact|slash compact|manifest|context degradation|crash recovery",
 "5.5": r"stratified|calibrat|field[- ]level|aggregate (accuracy|metric)|labeled validation",
 "5.6": r"provenance|claim[- ]source|attribution|temporal|conflicting",
}
OTHER = {
 "java": r"\bjava\b|spring boot", "python": r"\bpython\b|pydantic", "typescript": r"typescript",
 "rag/vector": r"\brag\b|vector (db|database|store)|embedding",
 "security": r"prompt injection|\bpii\b|\brbac\b|guardrail",
 "evals": r"\bevals?\b|evaluation|observability|tracing",
 "k8s/cloud": r"kubernetes|\bkafka\b|\baws\b|docker",
 "streaming": r"streaming|server[- ]sent", "caching": r"prompt cach",
 "transport": r"\bstdio\b|\bsse\b|streamable http",
}
rows, totals, other_total = [], {k: 0 for k in TS}, {k: 0 for k in OTHER}
titles = {}
for line in (root / "playlist.txt").read_text(encoding="utf-8", errors="replace").splitlines():
    p = line.split("|", 3)
    if len(p) == 4 and p[0].isdigit():
        titles[p[0]] = (p[1], p[2], p[3])
for idx, (vid, dur, title) in titles.items():
    text = (root / "transcripts" / f"{idx}_{vid}.txt").read_text(encoding="utf-8").lower()
    words = text.split()
    counts = {k: len(re.findall(v, text)) for k, v in TS.items()}
    for k, c in counts.items():
        totals[k] += c
    other = {k: len(re.findall(v, text)) for k, v in OTHER.items()}
    for k, c in other.items():
        other_total[k] += c
    top = sorted(counts.items(), key=lambda kv: -kv[1])[:6]
    rows.append({"idx": idx, "vid": vid, "min": round(float(dur) / 60), "title": title, "words": len(words),
                 "top": [(k, c) for k, c in top if c >= 3], "other": {k: c for k, c in other.items() if c}})
    n = len(words)
    samples = [" ".join(words[int(n * f):int(n * f) + 45]) for f in (0.2, 0.55, 0.85)]
    print(f"\n#{idx} {title[:70]} [{rows[-1]['min']}m {n}w]")
    print(" TS:", " ".join(f"{k}={c}" for k, c in rows[-1]["top"]))
    print(" other:", rows[-1]["other"])
    for s in samples:
        print("  ~", s)
print("\nTOTALS", json.dumps(totals))
print("OTHER", json.dumps(other_total))
(root / "matrix.json").write_text(json.dumps({"rows": rows, "totals": totals, "other": other_total}), encoding="utf-8")
