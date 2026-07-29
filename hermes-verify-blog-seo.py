#!/usr/bin/env python3
"""Ad-hoc verification: SEO/structural checks on blog HTML files."""
import os, re, sys

BLOG_DIR = "/opt/data/fazquepaga/frontend/public/blog"
FILES = [
    "gift-cards-como-incentivo-financeiro-para-criancas.html",
    "planejamento-financeiro-em-familia-como-envolver-criancas.html",
]

def check_file(path):
    base = os.path.basename(path)
    with open(path) as f:
        content = f.read()
    lines = content.split("\n")
    lc = len(lines)
    issues = []

    # DOCTYPE + closing
    issues.append("DOCTYPE" if "<!DOCTYPE html>" in content else "Missing DOCTYPE")
    issues.append("closing </html>" if "</html>" in content else "Missing </html>")

    # Title length
    tm = re.search(r"<title>(.*?)</title>", content, re.DOTALL)
    if tm:
        t = tm.group(1).replace(" | Blog TaskAndPay", "")
        l = len(t)
        issues.append(f"Title {l} chars {'✓' if 10<=l<=70 else '✗'}")
    else:
        issues.append("Missing <title> ✗")

    # Meta description length
    dm = re.search(r'<meta name="description" content="([^"]+)"', content)
    if dm:
        d = len(dm.group(1))
        issues.append(f"Meta desc {d} chars {'✓' if 140<=d<=160 else '✗'}")
    else:
        issues.append("Missing meta description ✗")

    # Keywords count
    km = re.search(r'<meta name="keywords" content="([^"]+)"', content)
    if km:
        kw = len(km.group(1).split(","))
        issues.append(f"Keywords: {kw} {'✓' if 5<=kw<=8 else '✗'}")
    else:
        issues.append("Missing meta keywords ✗")

    # H1 matches title
    hm = re.search(r"<h1[^>]*>(.*?)</h1>", content, re.DOTALL)
    if hm and tm:
        h1 = hm.group(1).strip()
        title = tm.group(1).replace(" | Blog TaskAndPay", "").strip()
        issues.append(f"H1==Title {'✓' if h1==title else '✗'}")
    else:
        issues.append("H1/title check failed ✗")

    # H2 count
    h2s = re.findall(r"<h2[^>]*>(.*?)</h2>", content, re.DOTALL)
    qh2 = sum(1 for h2 in h2s if h2.strip().endswith("?"))
    issues.append(f"H2s={len(h2s)} ({qh2} question) {'✓' if len(h2s)>=4 else '✗'}")

    # JSON-LD
    jl = "application/ld+json" in content
    jl_complete = all(k in content for k in ['"@type": "Article"', '"datePublished"', '"headline"'])
    issues.append(f"JSON-LD Article {'✓' if jl and jl_complete else '✗'}")

    # OG tags
    og_ok = all(t in content for t in ['property="og:title"', 'property="og:description"', 'property="og:image"', 'property="og:url"'])
    issues.append(f"Open Graph {'✓' if og_ok else '✗'}")
    tw_ok = 'name="twitter:card"' in content
    issues.append(f"Twitter Card {'✓' if tw_ok else '✗'}")

    # Unsplash
    issues.append(f"Unsplash image {'✓' if 'images.unsplash.com' in content else '✗'}")

    # TaskAndPay mentions
    m = content.count("TaskAndPay")
    issues.append(f"TaskAndPay {m}x {'✓' if m>=1 else '✗'}")

    # CTA + GTM
    issues.append(f"CTA {'✓' if 'Começar Gratuitamente' in content else '✗'}")
    issues.append(f"GTM {'✓' if 'GTM-K4R2DZJ8' in content else '✗'}")

    # Date
    dates = re.findall(r"(\d{1,2} [A-Z][a-z]{2} \d{4})", content)
    issues.append(f"Date: {dates[0] if dates else 'MISSING'} {'✓' if dates else '✗'}")

    # Slug in URL
    slug = base.replace(".html", "")
    issues.append(f"Slug in URL {'✓' if slug in content else '✗'}")

    sz = os.path.getsize(path)
    issues.append(f"Size: {sz:,} bytes {'✓' if sz>=5000 else '✗'}")

    return issues, lc, sz

all_pass = True
for fn in FILES:
    path = os.path.join(BLOG_DIR, fn)
    issues, lc, sz = check_file(path)
    fails = [i for i in issues if '✗' in i]
    ok = len(fails) == 0
    if not ok:
        all_pass = False
    print(f"\n{'='*55}")
    print(f"{'✅' if ok else '❌'} {fn}")
    print(f"   ({sz:,} bytes, {lc} lines)")
    for i in issues:
        print(f"   {'✅' if '✓' in i else '❌'} {i}")

print(f"\n{'='*55}")
print(f"OVERALL: {'✅ ALL CHECKS PASSED' if all_pass else '❌ SOME FAILED - review above'}")
sys.exit(0 if all_pass else 1)