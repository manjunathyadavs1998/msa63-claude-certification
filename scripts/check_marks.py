import re, sys

def flatten(s):
    # join soft line breaks; keep blank lines so a highlight cannot span paragraphs
    return re.sub(r'\n(?!\s*\n)', ' ', s)

def strip_code(s):
    # Fenced blocks go entirely. Inline code becomes a PLACEHOLDER rather than
    # being deleted: ==`code`== is a valid highlighted code span, and deleting
    # the span leaves ==== adjacent, which then looks like a nested marker.
    s = re.sub(r'```.*?```', '', s, flags=re.S)
    s = re.sub(r'`[^`\n]*`', 'CODE', s)
    return s

issues = 0
for path in sys.argv[1:]:
    raw = open(path, encoding='utf-8').read()
    body = strip_code(raw)
    flat = flatten(body)
    n = flat.count('==')
    pairs = re.findall(r'==(.+?)==', flat)
    if n % 2:
        print(f"UNBALANCED  {path}: {n} '==' markers (odd)")
        issues += 1
    # PER-PARAGRAPH parity. The file-level count above is not enough: two
    # paragraphs each holding an odd number of markers cancel out and the file
    # passes while BOTH render a literal '==' on the page. That is exactly how
    # six of them reached built PDFs in M23 L2 and L7 before this check existed.
    # A highlight never spans a blank line, so every paragraph must be even.
    for para in re.split(r'\n\s*\n', body):
        c = flatten(para).count('==')
        if c % 2:
            first = ' '.join(para.split())[:80]
            print(f"ODD-PARA    {path}: {c} marker(s) in one paragraph -- {first}...")
            issues += 1
    # nested: an opening == inside a pair's text
    for p in pairs:
        if '==' in p:
            print(f"NESTED      {path}: ...{p[:90]}...")
            issues += 1
    # revision blocks
    for m in re.finditer(r'^::: *revision(.*?)^:::', body, flags=re.S | re.M):
        c = len(re.findall(r'==(.+?)==', flatten(m.group(1))))
        if c:
            print(f"IN-REVISION {path}: {c} highlight(s) inside a revision block")
            issues += 1
    # Banned patterns. Checked on `body`, which has code spans and fenced blocks
    # removed, so a rule that merely NAMES `subgraph` in prose is not a hit.
    # Mermaid fences are scanned separately -- that is where it actually matters.
    for m in re.finditer(r'Figure \d+\.', body):
        print(f"BANNED      {path}: Figure N. in a caption or prose")
        issues += 1
    for m in re.finditer(r'```mermaid.*?```', raw, flags=re.S):
        block = m.group(0)
        if re.search(r'\bsubgraph\b', block):
            print(f"BANNED      {path}: subgraph inside a mermaid block")
            issues += 1
        if re.search(r'^\s*(flowchart|graph)\s+LR', block, flags=re.M):
            print(f"BANNED      {path}: flowchart LR inside a mermaid block")
            issues += 1
    # Java line length -- a wrapped line returns to column 0 and reads as a new
    # statement. Measured ceiling is 95 chars; 92 leaves margin.
    for m in re.finditer(r'```java.*?\n(.*?)```', raw, flags=re.S):
        for i, line in enumerate(m.group(1).split('\n'), 1):
            if len(line) > 92:
                print(f"LONG-JAVA   {path}: {len(line)} chars -- {line.strip()[:60]}...")
                issues += 1
    print(f"ok          {path}: {len(pairs)} highlights")
print(f"\n{issues} issue(s)")
