"""validate_pdf.py - render a PDF to PNGs and run mechanical QA checks.

Visual inspection is still a human/model step; this script produces the images
and flags what it can detect automatically.

Usage: python scripts/validate_pdf.py <file.pdf> [outdir] [--scale 1.6]
"""
import sys, os, re, subprocess, json, glob

import pypdfium2 as pdfium
from pypdf import PdfReader

REPLACEMENT = chr(0xFFFD)
CTRL = re.compile('[' + ''.join(
    chr(c) for c in list(range(0, 9)) + [11, 14] + list(range(15, 32))) + ']')


def render(pdf_path, outdir, scale=1.6):
    os.makedirs(outdir, exist_ok=True)
    # Clear old renders first. A shorter rebuild otherwise leaves stale page-NNN.png
    # files behind, and inspecting one of those is indistinguishable from inspecting
    # the real thing -- the same trap as validating a PDF that failed to rebuild.
    for stale in glob.glob(os.path.join(outdir, 'page-*.png')):
        os.remove(stale)
    doc = pdfium.PdfDocument(pdf_path)
    paths = []
    for i in range(len(doc)):
        img = doc[i].render(scale=scale).to_pil()
        p = os.path.join(outdir, 'page-%03d.png' % (i + 1))
        img.save(p)
        paths.append((p, img.size))
    return doc, paths


def _decode(b):
    """This Windows poppler build emits Latin-1, not UTF-8. Decoding its output
    as UTF-8 turns every valid 0xB7 ('.') into U+FFFD and invents glyph bugs
    that are not in the PDF. Try UTF-8, fall back to Latin-1."""
    try:
        return b.decode('utf-8')
    except UnicodeDecodeError:
        return b.decode('latin-1')


def text_qa(pdf_path):
    """Extracted text proves the PDF is searchable and exposes bad glyphs.

    pypdf is authoritative for glyph fidelity (it reads the ToUnicode CMap);
    pdftotext is used for layout-aware text and per-page splitting.
    """
    issues = []
    try:
        reader = PdfReader(pdf_path)
        pytxt = '\n'.join((p.extract_text() or '') for p in reader.pages)
    except Exception as e:
        pytxt = ''
        issues.append('pypdf extraction failed: %s' % e)

    n = pytxt.count(REPLACEMENT)
    if n:
        issues.append('%d unmappable glyphs (U+FFFD) via pypdf - font subset '
                      'is missing a ToUnicode entry' % n)
    if CTRL.search(pytxt):
        issues.append('control characters in extracted text')

    try:
        out = subprocess.run(['pdftotext', '-q', '-layout', pdf_path, '-'],
                             capture_output=True, timeout=120)
        ptxt = _decode(out.stdout)
    except FileNotFoundError:
        ptxt = None
        issues.append('pdftotext not available - layout text QA skipped')

    return (pytxt or ptxt or ''), issues


def main():
    pdf = sys.argv[1]
    if len(sys.argv) > 2 and not sys.argv[2].startswith('--'):
        outdir = sys.argv[2]
    else:
        outdir = os.path.join(os.path.dirname(pdf), 'pages')
    scale = 1.6
    if '--scale' in sys.argv:
        scale = float(sys.argv[sys.argv.index('--scale') + 1])

    doc, pages = render(pdf, outdir, scale)
    reader = PdfReader(pdf)
    txt, issues = text_qa(pdf)

    nchars = len(re.sub(r'\s', '', txt)) if txt else 0
    try:
        n_outline = len(reader.outline) if reader.outline else 0
    except Exception:
        n_outline = 0

    report = {
        'pdf': pdf,
        'pages': len(pages),
        'page_size_pt': [round(float(reader.pages[0].mediabox.width), 1),
                         round(float(reader.pages[0].mediabox.height), 1)],
        'rendered_to': outdir,
        'outline_entries': n_outline,
        'extractable_chars': nchars,
        'chars_per_page': round(nchars / len(pages), 1) if pages else 0,
        'issues': issues,
    }

    w, h = report['page_size_pt']          # A4 = 595.3 x 841.9 pt
    if not (590 < w < 600 and 836 < h < 848):
        report['issues'].append('page size %sx%spt is not A4' % (w, h))
    if report['chars_per_page'] < 200:
        report['issues'].append(
            'only %s extractable chars/page - text may not be searchable'
            % report['chars_per_page'])
    if n_outline == 0:
        report['issues'].append('no PDF outline/bookmarks (run add_bookmarks.py)')

    print(json.dumps(report, indent=1))
    return report


if __name__ == '__main__':
    main()
