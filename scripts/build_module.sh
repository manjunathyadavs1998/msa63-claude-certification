#!/usr/bin/env bash
# Usage: scripts/build_module.sh <module-dir> <validation-dir> <"NAME|file|Title|Module"> ...
# Stops at the first failure and never hides build output.
set -u
dir="$1"; val="$2"; shift 2
fail=0
for spec in "$@"; do
  IFS='|' read -r name file title module <<< "$spec"
  md="02-modules/$dir/$file"
  pdf="05-pdf-output/modules/$name.pdf"
  echo "=== $name"
  python -I -X utf8 scripts/check_marks.py "$md" | tail -2
  before=$(stat -c %Y "$pdf" 2>/dev/null || echo 0)
  node scripts/build_pdf.mjs --in "$md" --out "$pdf" --title "$title" --module "$module" --style notebook || { echo "BUILD FAILED: $name"; fail=1; continue; }
  after=$(stat -c %Y "$pdf" 2>/dev/null || echo 0)
  [ "$after" -ge "$before" ] || { echo "STALE PDF: $name"; fail=1; }
  python -I -X utf8 scripts/add_bookmarks.py "$pdf" "$md" | tail -1
  python -I -X utf8 scripts/validate_pdf.py "$pdf" "07-validation/$val/$name" | python -I -X utf8 -c "import json,sys; d=json.load(sys.stdin); print('pages', d['pages'], 'chars/page', d['chars_per_page'], 'issues', d['issues'])"
done
exit $fail
