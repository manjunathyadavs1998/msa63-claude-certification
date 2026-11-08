"""
check_java.py -- compile the Java in a notes file.

The brief asks for Java correctness "where practical". javac 21 is on this
machine, so it is practical, and asserting that code compiles without compiling
it is exactly the kind of claim this course is not allowed to make.

Convention
----------
A fence that should compile declares its file name:

    ```java file=SeatHold.java
    public final class SeatHold { ... }
    ```

All `file=` blocks in one notes file are written to a temp directory and
compiled together, so classes may reference each other. A block may also opt
into a named group, compiled separately from the others -- needed when a lesson
shows a "before" and an "after" that declare the same class:

    ```java file=Order.java group=before
    ```java file=Order.java group=after

Blocks with no `file=` are illustrative fragments and are never compiled.
A block marked `nocompile` is skipped even if it names a file -- use it for
code that is deliberately broken and discussed as such.

Usage
-----
    python -I -X utf8 scripts/check_java.py 02-modules/22-oop/notes-a.md [...]
"""
import re
import shutil
import subprocess
import sys
import tempfile
from collections import defaultdict
from pathlib import Path

FENCE = re.compile(r'^```java([^\n]*)\n(.*?)^```', re.S | re.M)
RELEASE = '21'


def blocks(md_text):
    """Yield (group, filename, code) for every compilable fence."""
    for m in FENCE.finditer(md_text):
        info, code = m.group(1), m.group(2)
        if 'nocompile' in info:
            continue
        fn = re.search(r'file=([\w./$-]+\.java)', info)
        if not fn:
            continue
        grp = re.search(r'group=([\w,-]+)', info)
        # A file may belong to several groups: `group=main,lsp`. Shared types
        # such as Money are written into each group's directory.
        for g in (grp.group(1).split(',') if grp else ['main']):
            yield g.strip(), fn.group(1), code


def compile_group(group, files, verbose):
    tmp = Path(tempfile.mkdtemp(prefix=f'lldjava-{group}-'))
    try:
        written = []
        for name, code in files.items():
            p = tmp / name
            p.parent.mkdir(parents=True, exist_ok=True)
            p.write_text(code, encoding='utf-8')
            written.append(p)
        proc = subprocess.run(
            ['javac', '--release', RELEASE, '-nowarn', '-d', str(tmp / 'out'),
             *[str(p) for p in written]],
            capture_output=True, text=True,
        )
        if proc.returncode == 0:
            print(f'  OK    group {group!r}: {len(written)} file(s) compile')
            return 0
        print(f'  FAIL  group {group!r}: javac exit {proc.returncode}')
        for line in (proc.stderr or proc.stdout).splitlines():
            if line.strip():
                print(f'          {line}')
        return 1
    finally:
        shutil.rmtree(tmp, ignore_errors=True)


def main(argv):
    if not shutil.which('javac'):
        print('javac not found on PATH -- cannot verify Java. '
              'Say so in the notes rather than claiming it compiles.')
        return 2
    total_fail = 0
    for path in argv:
        text = Path(path).read_text(encoding='utf-8')
        groups = defaultdict(dict)
        for grp, name, code in blocks(text):
            if name in groups[grp]:
                print(f'  WARN  {path}: {name} declared twice in group {grp!r} '
                      f'-- second one wins; use group= to separate before/after')
            groups[grp][name] = code
        if not groups:
            print(f'{path}: no compilable java blocks (none declared file=)')
            continue
        print(f'{path}:')
        for grp in sorted(groups):
            total_fail += compile_group(grp, groups[grp], verbose=True)
    print(f'\n{total_fail} failing group(s)')
    return 1 if total_fail else 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
