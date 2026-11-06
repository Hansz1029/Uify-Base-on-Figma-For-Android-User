import re

with open('app/src/main/assets/editor.html', 'r', encoding='utf-8') as f:
    html = f.read()

# 1. Update .top-clean-bar CSS to Floating Capsule ala iOS AMOLED
old_css = """    .top-clean-bar {
      position: fixed;
      top: 0; left: 0; right: 0;
      height: calc(var(--topbar-height) + var(--safe-area-top));
      padding-top: var(--safe-area-top);
      background: rgba(26, 26, 30, 0.96);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      border-bottom: 1px solid var(--border-panel);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-left: calc(12px * var(--ui-scale));
      padding-right: calc(12px * var(--ui-scale));
      z-index: 60;
    }"""

new_css = """    .top-clean-bar {
      position: fixed;
      top: 12px;
      left: 50%;
      transform: translateX(-50%);
      width: calc(100% - 24px);
      max-width: 980px;
      height: 48px;
      background: rgba(0, 0, 0, 0.88);
      backdrop-filter: blur(28px);
      -webkit-backdrop-filter: blur(28px);
      border: 1px solid rgba(255, 255, 255, 0.14);
      border-radius: 9999px;
      box-shadow: 0 16px 40px rgba(0, 0, 0, 0.8), 0 0 1px rgba(255, 255, 255, 0.25);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-left: 14px;
      padding-right: 14px;
      z-index: 90;
      transition: all 0.2s ease;
    }"""

if old_css in html:
    html = html.replace(old_css, new_css)
    print("Replaced .top-clean-bar with floating capsule CSS.")
else:
    # Try regex
    html = re.sub(
        r'\.top-clean-bar\s*\{[\s\S]*?z-index:\s*60;\s*\}',
        new_css.strip(),
        html,
        count=1
    )
    print("Replaced via regex.")

# 2. Hide autosave indicator and manual save buttons in CSS
hide_autosave_css = """
/* HIDE AUTOSAVE & SAVE CONTROLS IN UI */
#uinify-autosave-indicator { display: none !important; }
.autosave-pill { display: none !important; }
button[onclick*="triggerManualSave"] { display: none !important; }
"""
html = html.replace('</head>', f'<style>{hide_autosave_css}</style></head>')

# 3. Replace brand logo with Uinify Monochrome White SVG Logo + Return to Dashboard button
old_brand_pattern = r'<div class="top-left-brand"[^>]*>[\s\S]*?</div>'
new_brand_html = """<div class="top-left-brand" onclick="openProjectMenuModal()" title="Menu Proyek Uinify" style="display: flex; align-items: center; gap: 8px;">
      <!-- Tombol Kembali ke Dashboard (SVG) -->
      <button class="top-icon-btn" onclick="event.stopPropagation(); returnToDashboard();" title="Kembali ke Dashboard" style="width: 30px; height: 30px; border-radius: 50%; background: rgba(255,255,255,0.08); display: flex; align-items: center; justify-content: center;">
        <svg class="svg-icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"/></svg>
      </button>
      <!-- Logo Uinify Monokrom Putih Minimalis -->
      <svg style="width: 22px; height: 22px; filter: drop-shadow(0 0 4px rgba(255,255,255,0.3));" viewBox="0 0 32 32" fill="none">
        <rect width="32" height="32" rx="9" fill="#FFFFFF"/>
        <path d="M10 9v7a6 6 0 0 0 12 0V9" stroke="#000000" stroke-width="3.2" stroke-linecap="round" stroke-linejoin="round"/>
        <circle cx="22" cy="9" r="2.2" fill="#000000"/>
      </svg>
      <span id="uinify-project-title" style="font-weight: 800; font-size: 13px; color: #FFFFFF; letter-spacing: -0.2px;">Uinify Studio</span>
      <svg class="svg-icon-sm" viewBox="0 0 24 24" style="color: #9CA3AF;"><polyline points="6 9 12 15 18 9"/></svg>
    </div>"""

# Replace in header
header_start = html.find('<header class="top-clean-bar">')
if header_start != -1:
    header_end = html.find('</header>', header_start)
    header_chunk = html[header_start:header_end]
    header_chunk_new = re.sub(r'<div class="top-left-brand"[\s\S]*?</div>\s*<!--', new_brand_html + '\n    <!--', header_chunk, count=1)
    if header_chunk != header_chunk_new:
        html = html[:header_start] + header_chunk_new + html[header_end:]
        print("Updated top-left-brand with Uinify monochrome white logo and back button.")
    else:
        print("Warning: header_chunk replacement didn't change.")

# 4. Add returnToDashboard JS function
js_nav = """
<script>
window.returnToDashboard = function() {
  // Trigger silent save before leaving
  if (typeof triggerAutoSave === 'function') triggerAutoSave();
  if (window.UinifyNative && typeof window.UinifyNative.returnToDashboard === 'function') {
    window.UinifyNative.returnToDashboard();
  } else {
    window.location.href = 'dashboard.html';
  }
};
</script>
"""
html = html.replace('</body>', f'{js_nav}</body>')

with open('app/src/main/assets/editor.html', 'w', encoding='utf-8') as f:
    f.write(html)

print("SUCCESS: Updated editor.html with floating capsule iOS AMOLED topbar, monochrome Uinify logo, and hidden autosave.")
