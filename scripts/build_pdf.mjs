/**
 * build_pdf.mjs — Markdown -> styled HTML -> A4 PDF (headless Chrome).
 *
 * Why Chrome and not WeasyPrint: WeasyPrint cannot load GTK on this machine
 * (see 00-planning/COURSE_PROGRESS.md). Chrome gives us real page numbers via
 * header/footer templates; it cannot emit a PDF outline, so bookmarks are added
 * afterwards by scripts/add_bookmarks.py.
 *
 * Usage:
 *   node scripts/build_pdf.mjs --in <file.md> --out <file.pdf>
 *        [--title "..."] [--subtitle "..."] [--module "Module 01"]
 *        [--keep-html]
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import puppeteer from 'puppeteer-core';
import MarkdownIt from 'markdown-it';
import anchor from 'markdown-it-anchor';
import attrs from 'markdown-it-attrs';
import container from 'markdown-it-container';
import deflist from 'markdown-it-deflist';
import mark from 'markdown-it-mark';   // ==text== -> <mark>, with nested inline parsed correctly

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '..');

/* ---------------- args ---------------- */
const argv = process.argv.slice(2);
const arg = (k, d = null) => {
  const i = argv.indexOf(`--${k}`);
  return i >= 0 && argv[i + 1] && !argv[i + 1].startsWith('--') ? argv[i + 1] : d;
};
const flag = (k) => argv.includes(`--${k}`);

const IN = arg('in');
const OUT = arg('out');
if (!IN || !OUT) {
  console.error('usage: node scripts/build_pdf.mjs --in <md> --out <pdf> [--title T] [--subtitle S] [--module M]');
  process.exit(2);
}
const TITLE = arg('title', path.basename(IN, '.md'));
const SUBTITLE = arg('subtitle', '');
const MODULE = arg('module', '');

/* ---------------- chrome ---------------- */
function findChrome() {
  const candidates = [
    process.env.CHROME_PATH,
    'C:/Program Files/Google/Chrome/Application/chrome.exe',
    'C:/Program Files (x86)/Google/Chrome/Application/chrome.exe',
    'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
    'C:/Program Files/Microsoft/Edge/Application/msedge.exe',
  ].filter(Boolean);
  for (const c of candidates) if (fs.existsSync(c)) return c;
  throw new Error('No Chrome/Edge found. Set CHROME_PATH.');
}

/* ---------------- fonts (inlined so the PDF is self-contained) ---------------- */
/* latin-ext is included deliberately: the plain "latin" subsets omit U+00B7 (·)
   and U+00D7 (×), which made Chrome fall back to a system font with no
   ToUnicode map, so those glyphs rendered fine but extracted as U+FFFD. */
const FONTS = [
  ['Inter', 400, 'normal', '@fontsource/inter/files/inter-latin-400-normal.woff2'],
  ['Inter', 600, 'normal', '@fontsource/inter/files/inter-latin-600-normal.woff2'],
  ['Inter', 700, 'normal', '@fontsource/inter/files/inter-latin-700-normal.woff2'],
  ['Inter', 400, 'italic', '@fontsource/inter/files/inter-latin-400-italic.woff2'],
  ['Inter', 400, 'normal', '@fontsource/inter/files/inter-latin-ext-400-normal.woff2'],
  ['Inter', 600, 'normal', '@fontsource/inter/files/inter-latin-ext-600-normal.woff2'],
  ['Inter', 700, 'normal', '@fontsource/inter/files/inter-latin-ext-700-normal.woff2'],
  ['Caveat', 700, 'normal', '@fontsource/caveat/files/caveat-latin-700-normal.woff2'],
  ['Patrick Hand', 400, 'normal', '@fontsource/patrick-hand/files/patrick-hand-latin-400-normal.woff2'],
  ['Architects Daughter', 400, 'normal', '@fontsource/architects-daughter/files/architects-daughter-latin-400-normal.woff2'],
  ['Caveat', 700, 'normal', '@fontsource/caveat/files/caveat-latin-ext-700-normal.woff2'],
  ['JetBrains Mono', 400, 'normal', '@fontsource/jetbrains-mono/files/jetbrains-mono-latin-400-normal.woff2'],
  ['JetBrains Mono', 700, 'normal', '@fontsource/jetbrains-mono/files/jetbrains-mono-latin-700-normal.woff2'],
];
function fontFaceCSS() {
  const out = [];
  for (const [fam, wt, style, rel] of FONTS) {
    const p = path.join(ROOT, 'node_modules', rel);
    if (!fs.existsSync(p)) { console.warn(`  ! font missing, skipped: ${rel}`); continue; }
    const b64 = fs.readFileSync(p).toString('base64');
    out.push(`@font-face{font-family:"${fam}";font-weight:${wt};font-style:${style};` +
             `font-display:block;src:url(data:font/woff2;base64,${b64}) format("woff2");}`);
  }
  return out.join('\n');
}

/* ---------------- markdown ---------------- */
const md = new MarkdownIt({ html: true, linkify: false, typographer: true, breaks: false })
  .use(attrs)
  .use(deflist)
  .use(mark)
  .use(anchor, { level: [1, 2, 3], slugify: s =>
      s.toLowerCase().replace(/[^\w\s-]/g, '').trim().replace(/\s+/g, '-') });

// ::: key / tip / warn / mistake / answer / real  -> styled callouts
const CALLOUTS = {
  key:     ['callout key', 'Key idea'],
  tip:     ['callout tip', 'Tip'],
  warn:    ['callout warn', 'Watch out'],
  mistake: ['callout mistake', 'Common mistakes'],
  answer:  ['callout answer', 'The interview answer'],
  real:    ['callout real', 'In the real world'],
};
for (const [name, [cls, defLabel]] of Object.entries(CALLOUTS)) {
  md.use(container, name, {
    render(tokens, idx) {
      if (tokens[idx].nesting === 1) {
        const info = tokens[idx].info.trim().slice(name.length).trim();
        const label = info || defLabel;
        return `<div class="${cls}"><span class="label">${md.utils.escapeHtml(label)}</span>\n`;
      }
      return '</div>\n';
    },
  });
}
// ::: keyidea  -> the lesson thesis block
md.use(container, 'keyidea', {
  render: (t, i) => t[i].nesting === 1
    ? '<div class="key-idea"><span class="label">Key idea</span>\n' : '</div>\n',
});
// ::: revision
md.use(container, 'revision', {
  render(t, i) {
    if (t[i].nesting !== 1) return '</div>\n';
    const label = t[i].info.trim().slice('revision'.length).trim() || 'Revision sheet';
    return `<div class="revision"><h3>${md.utils.escapeHtml(label)}</h3>\n`;
  },
});
// ::: notespace [tall|short] [label]
md.use(container, 'notespace', {
  render(t, i) {
    if (t[i].nesting !== 1) return '</div>\n';
    const rest = t[i].info.trim().slice('notespace'.length).trim();
    const size = /^(tall|short)\b/.exec(rest);
    const cls = size ? size[1] : 'short';
    const label = (size ? rest.slice(size[1].length) : rest).trim() || 'Your notes';
    return `<div class="notespace ${cls}"><span class="label">${md.utils.escapeHtml(label)}</span>\n`;
  },
});
// ::: margin  -> handwritten margin note
md.use(container, 'margin', {
  render: (t, i) => t[i].nesting === 1 ? '<div class="marginnote">\n' : '</div>\n',
});
// ::: qa  -> interview question block
md.use(container, 'qa', {
  render: (t, i) => t[i].nesting === 1 ? '<div class="qa">\n' : '</div>\n',
});

/* ```mermaid caption="Figure 1. ..." -> figure + figcaption, rendered in-page.
   The caption lives in the fence info so the <figcaption> is always inside the
   <figure>; a caption written as a sibling block would break away from its
   diagram at a page boundary. */
let figureNo = 0;
const fence = md.renderer.rules.fence.bind(md.renderer.rules);
md.renderer.rules.fence = (tokens, idx, opts, env, self) => {
  const t = tokens[idx];
  const info = (t.info || '').trim();
  if (info === 'mermaid' || info.startsWith('mermaid ')) {
    figureNo += 1;
    const cap = /caption\s*=\s*"([^"]*)"/.exec(info);
    const caption = cap
      ? `<figcaption><span class="num">Figure ${figureNo}.</span> ${cap[1]}</figcaption>`
      : '';
    return `<figure class="diagram"><pre class="mermaid">${
      md.utils.escapeHtml(t.content)}</pre>${caption}</figure>\n`;
  }
  if (info === 'java' || info.startsWith('java ')) {
    return `<pre class="code java"><code>${highlightJava(t.content)}</code></pre>\n`;
  }
  return fence(tokens, idx, opts, env, self);
};

/* ---------------- Java syntax highlighting ----------------
   Hand-rolled rather than pulled from npm: node_modules is shared with the
   HLD course by a directory junction, so installing a package here would
   change that project's dependency tree and invalidate its built PDFs.

   One pass, longest-match-first, so a keyword inside a string or comment is
   never re-tokenised. Everything is escaped on the way out. */
const JAVA_KEYWORDS = new Set((
  'abstract assert break case catch class const continue default do else enum extends final finally ' +
  'for goto if implements import instanceof interface native new package private protected public ' +
  'return static strictfp super switch synchronized this throw throws transient try volatile while ' +
  'var record sealed permits non-sealed yield'
).split(' '));
const JAVA_TYPES = new Set((
  'boolean byte char double float int long short void String Object Integer Long Double Boolean ' +
  'List Map Set Optional Duration Instant Clock BigDecimal UUID Exception RuntimeException ' +
  'IllegalArgumentException IllegalStateException Comparator Runnable Thread'
).split(' '));
const JAVA_LITERALS = new Set('true false null'.split(' '));

function highlightJava(src) {
  const esc = (s) => md.utils.escapeHtml(s);
  const out = [];
  // order matters: comments and strings must win over identifiers
  const re = new RegExp([
    '(/\\*[\\s\\S]*?\\*/|//[^\\n]*)',          // 1 comment
    '("(?:\\\\.|[^"\\\\])*"|\'(?:\\\\.|[^\'\\\\])*\')', // 2 string / char
    '(@[A-Za-z_]\\w*)',                        // 3 annotation
    '\\b(\\d[\\d_]*\\.?[\\d_]*[LlDdFf]?)\\b',  // 4 number
    '\\b([A-Za-z_]\\w*)\\b',                   // 5 word
  ].join('|'), 'g');
  let last = 0, m;
  while ((m = re.exec(src)) !== null) {
    if (m.index > last) out.push(esc(src.slice(last, m.index)));
    const [all, comment, str, ann, num, word] = m;
    if (comment)      out.push(`<span class="c">${esc(comment)}</span>`);
    else if (str)     out.push(`<span class="s">${esc(str)}</span>`);
    else if (ann)     out.push(`<span class="a">${esc(ann)}</span>`);
    else if (num)     out.push(`<span class="n">${esc(num)}</span>`);
    else if (word) {
      if (JAVA_KEYWORDS.has(word))      out.push(`<span class="k">${esc(word)}</span>`);
      else if (JAVA_LITERALS.has(word)) out.push(`<span class="l">${esc(word)}</span>`);
      else if (JAVA_TYPES.has(word) || /^[A-Z][A-Za-z0-9]*$/.test(word))
                                        out.push(`<span class="t">${esc(word)}</span>`);
      else                              out.push(esc(word));
    }
    last = m.index + all.length;
  }
  if (last < src.length) out.push(esc(src.slice(last)));
  return out.join('');
}

/* ---------------- assemble ---------------- */
const source = fs.readFileSync(IN, 'utf8');
// typographer turns "(c)" into a copyright sign, which corrupts lettered exercise parts.
const bodyHtml = md.render(source).replace(/©/g, '(c)');
const STYLE = arg('style', 'clean');          // 'clean' | 'notebook'
const cssFile = STYLE === 'notebook'
  ? '04-shared-assets/styles/course-notebook.css'
  : '04-shared-assets/styles/course.css';
const css = fs.readFileSync(path.join(ROOT, cssFile), 'utf8');
const mermaidJs = fs.readFileSync(path.join(ROOT, 'node_modules/mermaid/dist/mermaid.min.js'), 'utf8');

const html = `<!doctype html>
<html lang="en"><head><meta charset="utf-8"><title>${TITLE}</title>
<style>${fontFaceCSS()}</style>
<style>${css}</style>
</head><body>
${bodyHtml}
<script>${mermaidJs}<\/script>
<script>
  mermaid.initialize({
    startOnLoad: false,
    theme: 'base',
    look: ${JSON.stringify(STYLE === 'notebook' ? 'handDrawn' : 'classic')},
    handDrawnSeed: 42,
    fontFamily: ${JSON.stringify(STYLE === 'notebook'
        ? '"Patrick Hand", cursive' : 'Inter, sans-serif')},
    themeVariables: {
      background: '#FFFEF8',
      primaryColor:  ${JSON.stringify(STYLE === 'notebook' ? '#CFE4F7' : '#EAF0F8')},
      primaryBorderColor: ${JSON.stringify(STYLE === 'notebook' ? '#1C3461' : '#2A4A7B')},
      primaryTextColor: ${JSON.stringify(STYLE === 'notebook' ? '#1C3461' : '#23262B')},
      lineColor: ${JSON.stringify(STYLE === 'notebook' ? '#1C3461' : '#55606E')},
      secondaryColor: '#CDEFD6', tertiaryColor: '#FFF2A8',
      fontSize: ${JSON.stringify(STYLE === 'notebook' ? '15px' : '13px')}
    },
    /* htmlLabels:false -> labels become real SVG <text>. foreignObject labels
       get clipped by Chrome's print path (observed: "GET /feed" cut to "GET /feec"). */
    flowchart: { useMaxWidth: true, htmlLabels: false, curve: 'basis', padding: 10, wrappingWidth: 340 },
    sequence: { useMaxWidth: true, wrap: true },
    er: { useMaxWidth: true }
  });
  window.__diagramsReady = (async () => {
    const nodes = document.querySelectorAll('pre.mermaid');
    try { await mermaid.run({ nodes }); }
    catch (e) { window.__mermaidError = String(e); }
    await document.fonts.ready;
    return document.querySelectorAll('pre.mermaid svg, figure svg').length;
  })();
<\/script>
</body></html>`;

const htmlPath = OUT.replace(/\.pdf$/i, '.html');
fs.mkdirSync(path.dirname(OUT), { recursive: true });
fs.writeFileSync(htmlPath, html, 'utf8');

/* ---------------- print ---------------- */
const furniture = (txt, align) => `
<div style="width:100%;font-family:Inter,sans-serif;font-size:7pt;color:#8A93A0;
     padding:0 14mm;display:flex;justify-content:${align};align-items:center;">${txt}</div>`;

/* Header/footer templates render in their own context and cannot see the
   page's @font-face, so keep them to plain ASCII — a "·" here extracts as
   U+FFFD even though it displays correctly. */
const ascii = s => String(s).replace(/[·•]/g, '-').replace(/[—–]/g, '-').replace(/×/g, 'x');

const headerTemplate = furniture(
  `<span style="letter-spacing:.07em;text-transform:uppercase;">${
    md.utils.escapeHtml(ascii(MODULE || TITLE))}</span>`, 'flex-end');

const footerTemplate = furniture(
  `<span style="flex:1">${md.utils.escapeHtml(ascii(TITLE))}</span>
   <span style="font-variant-numeric:tabular-nums;">
     <span class="pageNumber"></span> / <span class="totalPages"></span>
   </span>`, 'space-between');

const browser = await puppeteer.launch({
  executablePath: findChrome(),
  headless: 'new',
  args: ['--no-sandbox', '--disable-dev-shm-usage', '--font-render-hinting=none'],
});
try {
  const page = await browser.newPage();
  const errs = [];
  page.on('pageerror', e => errs.push(String(e)));
  page.on('console', m => { if (m.type() === 'error') errs.push(m.text()); });

  await page.setContent(html, { waitUntil: 'load', timeout: 120000 });
  const nSvg = await page.evaluate(() => window.__diagramsReady);
  const mErr = await page.evaluate(() => window.__mermaidError || null);
  if (mErr) console.warn(`  ! mermaid: ${mErr}`);

  await page.pdf({
    path: OUT,
    format: 'A4',
    printBackground: true,
    displayHeaderFooter: true,
    headerTemplate,
    footerTemplate,
    margin: STYLE === 'notebook'
      ? { top: '14mm', bottom: '14mm', left: '12mm', right: '12mm' }
      : { top: '16mm', bottom: '15mm', left: '17mm', right: '17mm' },
    preferCSSPageSize: false,
  });

  const kb = (fs.statSync(OUT).size / 1024).toFixed(0);
  console.log(`  built ${path.relative(ROOT, OUT)}  (${kb} KB, ${nSvg} diagram(s))`);
  if (errs.length) console.warn(`  ! ${errs.length} page error(s): ${errs.slice(0, 3).join(' | ')}`);
  if (!flag('keep-html')) fs.unlinkSync(htmlPath);
} finally {
  await browser.close();
}
