import re

with open('app/src/main/assets/editor.html', 'r', encoding='utf-8') as f:
    html = f.read()

# 1. Remove the injected btn-import-html on tab-layers
html = re.sub(
    r"\$\('tab-layers'\)\.insertAdjacentHTML\('afterbegin'[\s\S]*?\);",
    "// Removed ghost button over tab-layers",
    html
)
html = re.sub(
    r"\$\('btn-import-html'\)\.onclick\s*=\s*\(\)\s*=>\s*inp\.click\(\);",
    "// Removed onclick for btn-import-html",
    html
)

# 2. Add explicit CSS to hide any ghost panels, stray panels, and btn-import-html
ghost_fix_css = """
/* REMOVE GHOST PANEL & STRAY BUTTONS ON TOP OF LAYER TAB */
#btn-import-html { display: none !important; }
#uinify-structure-panel { display: none !important; }
.ghost-panel { display: none !important; }

/* Hide HTML topbar if Compose native floating capsule is active */
.top-clean-bar {
  display: none !important;
}
.workspace {
  padding-top: 64px !important;
}
"""

html = html.replace('</head>', f'<style>{ghost_fix_css}</style></head>')

with open('app/src/main/assets/editor.html', 'w', encoding='utf-8') as f:
    f.write(html)

print("SUCCESS: Fixed ghost panel and removed stray buttons on tab-layers in editor.html")
