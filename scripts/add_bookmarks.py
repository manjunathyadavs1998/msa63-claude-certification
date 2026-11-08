"""add_bookmarks.py - add a PDF outline (bookmarks) to a Chrome-generated PDF.

Chrome's print-to-PDF produces links but no outline. We recover the heading
structure by locating each heading's text on the rendered pages, then write a
nested outline with pypdf.

Headings come from the module's Markdown (h1/h2), so the outline matches the
authored structure rather than whatever the text extractor happens to find.

Usage: python scripts/add_bookmarks.py <file.pdf> <source.md> [-o out.pdf]
"""
import sys, os, re, subprocess, argparse

from pypdf import PdfReader, PdfWriter


def headings_from_md(md_path):
    """Return [(level, text)] for ATX h1/h2 outside fenced code blocks."""
    out, fence = [], False
    for line in open(md_path, encoding='utf-8'):
        s = line.rstrip('\n')
        if s.lstrip().startswith('```'):
            fence = not fence
            continue
        if fence:
            continue
        m = re.match(r'^(#{1,2})\s+(.*\S)\s*$', s)
        if m:
            text = re.sub(r'\{[^}]*\}', '', m.group(2)).strip()      # strip {.attrs}
            text = re.sub(r'\[([^\]]+)\]\([^)]*\)', r'\1', text)     # strip links
            text = re.sub(r'[*`_]', '', text).strip()
            if text:
                out.append((len(m.group(1)), text))
    return out


def norm(s):
    """Fold text to a form that survives PDF extraction.

    Two independent hazards: this poppler build mixes UTF-8 with lone Latin-1
    bytes, and its dash handling is inconsistent -- an em dash comes back as
    "--" while an en dash between digits becomes whitespace. So fold every
    non-ASCII char to a space, then collapse all dash/space runs to one space,
    which makes the dash-vs-space distinction disappear on both sides.
    """
    for a, b in (("‘", "'"), ("’", "'"),
                 ("“", '"'), ("”", '"')):
        s = s.replace(a, b)
    s = ''.join(c if ord(c) < 128 else ' ' for c in s)
    s = re.sub(r'[-\s]+', ' ', s)
    return s.strip().lower()


def page_texts(pdf_path, npages):
    """Per-page extracted text, normalised for matching."""
    try:
        out = subprocess.run(['pdftotext', '-q', pdf_path, '-'],
                             capture_output=True, timeout=180).stdout
        try:                      # this poppler build emits Latin-1, not UTF-8
            raw = out.decode('utf-8')
        except UnicodeDecodeError:
            raw = out.decode('latin-1')
        pages = raw.split('\f')[:npages]
    except FileNotFoundError:
        reader = PdfReader(pdf_path)
        pages = [(p.extract_text() or '') for p in reader.pages]
    return [norm(p) for p in pages]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('pdf')
    ap.add_argument('md')
    ap.add_argument('-o', '--out')
    a = ap.parse_args()

    reader = PdfReader(a.pdf)
    n = len(reader.pages)
    texts = page_texts(a.pdf, n)
    heads = headings_from_md(a.md)

    writer = PdfWriter()
    for p in reader.pages:
        writer.add_page(p)

    cursor, parent, added, missed = 0, None, 0, []
    for level, text in heads:
        probe = norm(text)[:48]
        page = None
        for i in range(cursor, n):                 # search forward from last hit
            if probe and probe in texts[i]:
                page = i
                break
        if page is None:                           # fall back to a global search
            for i in range(n):
                if probe and probe in texts[i]:
                    page = i
                    break
        if page is None:
            missed.append(text)
            continue
        cursor = page
        if level == 1:
            parent = writer.add_outline_item(text, page)
        else:
            writer.add_outline_item(text, page, parent=parent)
        added += 1

    out = a.out or a.pdf
    with open(out, 'wb') as fh:
        writer.write(fh)

    print('bookmarks: %d added, %d not located, over %d pages -> %s'
          % (added, len(missed), n, os.path.basename(out)))
    for m in missed:
        print('  ! not located: %s' % m)


if __name__ == '__main__':
    main()
