"""Recomputes hand-derived numbers in AI12, and checks lesson Java against the sources."""
import glob
import pathlib
import re

print("L1 read/grep", round(30440 / 490), "grep+read share", round(7320 / 30440, 2))
ROOT = pathlib.Path(__file__).resolve().parent.parent
sources = list((ROOT / "code/ai12-claude-code/src").rglob("*.java"))
lines = {line.strip() for p in sources for line in p.read_text(encoding="utf-8").splitlines()}
for md in sorted(glob.glob(str(ROOT / "02-modules/12-claude-code/notes-*.md"))):
    text = pathlib.Path(md).read_text(encoding="utf-8")
    for block in re.findall(r"```java\n(.*?)```", text, re.S):
        for line in block.splitlines():
            if line.strip() and line.strip() not in lines:
                print(pathlib.Path(md).name, "NOT IN SOURCE:", line.strip())
    print(pathlib.Path(md).name, "diagrams", len(re.findall(r"^```mermaid", text, re.M)),
          "questions", text.count('class="q"'))
print("code check done")
