"""Exact-term evidence per video for the exam's most specific objectives."""
import pathlib
import re
import sys

root = pathlib.Path(sys.argv[1]) / "transcripts"
TERMS = {
 "interview pattern": r"interview pattern", "test-driven": r"test[- ]driven", "stratified": r"stratified",
 "calibrat": r"calibrat", "lost in middle": r"lost in the middle", "case facts": r"case facts",
 "detected_pattern": r"detected[_ ]pattern", "custom_id": r"custom[_ ]id", "fork_session": r"fork[_ ]session",
 "isError": r"is[_ ]?error", "manifest": r"manifest", "scratchpad": r"scratch ?pad", "@import": r"@ ?import|at import",
 "argument-hint": r"argument[- ]hint", "json-schema flag": r"json[- ]schema", "output-format": r"output[- ]format",
 "explore subagent": r"explore (sub ?agent|agent)", "path rules": r"\.claude/rules|claude rules|rules directory|rules folder",
 "/memory": r"/memory|slash memory", "/compact": r"/compact|slash compact|\{slash\} compact",
 "attention dilution": r"attention dilution", "prompt chain": r"prompt chain", "mcp resources": r"mcp resource",
 "env expansion": r"environment variable|env var", "pydantic": r"pydantic", "conflict": r"conflict",
 "temporal": r"temporal|publication date", "nullable": r"nullable", "agent sdk": r"agent sdk",
 "prompt injection": r"prompt injection", "temperature": r"temperature", "max_tokens": r"max[_ ]tokens",
}
files = sorted(root.glob("*.txt"))
texts = {f.name[:2]: f.read_text(encoding="utf-8").lower() for f in files}
for term, rx in TERMS.items():
    hits = {k: len(re.findall(rx, t)) for k, t in texts.items()}
    hits = {k: v for k, v in hits.items() if v}
    print(f"{term:18} " + (" ".join(f"{k}:{v}" for k, v in hits.items()) or "NONE"))


def ctx(video, rx, n=2, width=38):
    words = texts[video]
    out = []
    for m in list(re.finditer(rx, words))[:n]:
        out.append(words[max(0, m.start() - width * 5):m.end() + width * 5].replace("\n", " "))
    return out


for video, rx in [("01", r"partner"), ("23", r"partner network|eligib"), ("03", r"99\.99"), ("08", r"forced|tool_choice|tool choice"), ("16", r"temperature")]:
    print(f"\n--- {video} /{rx}/")
    for c in ctx(video, rx):
        print(" *", c)
