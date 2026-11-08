"""Contact sheets of every page holding a figure, so that all diagrams get looked at.

Usage: contact_sheets.py <pdf> <pages-dir> <out-prefix> [per_sheet]
"""
import pathlib
import re
import sys

from PIL import Image
from pypdf import PdfReader

pdf, pages_dir, prefix = sys.argv[1:4]
per_sheet = int(sys.argv[4]) if len(sys.argv) > 4 else 8
reader = PdfReader(pdf)
figure_pages = [i + 1 for i, page in enumerate(reader.pages)
                if re.search(r"Figure\s*\d", page.extract_text() or "")]
print(pathlib.Path(pdf).name, "figure pages:", figure_pages)
for start in range(0, len(figure_pages), per_sheet):
    batch = figure_pages[start:start + per_sheet]
    images = [Image.open(pathlib.Path(pages_dir) / f"page-{n:03d}.png") for n in batch]
    w, h = images[0].size
    cols = 4
    rows = (len(images) + cols - 1) // cols
    sheet = Image.new("RGB", (w * cols, h * rows), "white")
    for i, image in enumerate(images):
        sheet.paste(image, ((i % cols) * w, (i // cols) * h))
    sheet.thumbnail((2000, 2000))
    out = f"{prefix}-{start // per_sheet + 1}.png"
    sheet.save(out)
    print(" ", out)
