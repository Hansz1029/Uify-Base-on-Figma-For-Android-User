import re
import os

with open('public/assets/aistudio/Uinfy02-fixed-1_11611382.html', 'r', encoding='utf-8') as f:
    html = f.read()

# 1. CSS for Uinify Native features, Modals, Artboard, and Accents
css_to_add = """
/* === UINIFY ENHANCEMENTS: AUTO-SAVE, CANVAS MODE, ACCENT COLOR, AND FILE OPS === */
:root {
  --uinify-accent: #0D99FF;
}

.autosave-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 7px;
  border-radius: 12px;
  background: rgba(16, 185, 129, 0.15);
  border: 1px solid rgba(16, 185, 129, 0.35);
  color: #10B981;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.3px;
  transition: all 0.25s ease;
}
.autosave-pill.saving {
  background: rgba(245, 158, 11, 0.18);
  border-color: rgba(245, 158, 11, 0.4);
  color: #F59E0B;
}

.svg-icon-micro {
  width: 11px;
  height: 11px;
}

/* Modals for Project, Canvas Mode, Accent Color, Export, Import */
.uinify-modal-card {
  width: min(92vw, 380px);
  max-height: 85vh;
  overflow-y: auto;
  background: rgba(24, 24, 28, 0.96);
  backdrop-filter: blur(28px);
  -webkit-backdrop-filter: blur(28px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.65), 0 0 1px rgba(255, 255, 255, 0.2);
  color: #F3F4F6;
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 14px;
  z-index: 350;
  font-family: 'Inter', -apple-system, sans-serif;
}

.uinify-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding-bottom: 10px;
}
.uinify-modal-title {
  font-size: 14px;
  font-weight: 700;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  gap: 8px;
}
.uinify-modal-close-btn {
  background: transparent;
  border: none;
  color: #9CA3AF;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.uinify-modal-close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #FFFFFF;
}

.uinify-grid-presets {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.uinify-preset-btn {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  color: #E5E7EB;
  cursor: pointer;
  transition: all 0.15s ease;
  font-size: 11px;
}
.uinify-preset-btn:hover, .uinify-preset-btn.active {
  background: rgba(13, 153, 255, 0.15);
  border-color: var(--figma-blue);
  color: #FFFFFF;
}
.uinify-preset-btn .preset-label {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}
.uinify-preset-btn .preset-dim {
  font-size: 10px;
  color: #9CA3AF;
}

.uinify-action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.uinify-action-row:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.15);
}
.uinify-action-label {
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #FFFFFF;
}
.uinify-action-desc {
  font-size: 10px;
  color: #9CA3AF;
  text-align: right;
}

.accent-swatch-list {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 4px 0;
}
.accent-swatch {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 2px solid transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s, border-color 0.15s;
}
.accent-swatch:hover {
  transform: scale(1.12);
}
.accent-swatch.active {
  border-color: #FFFFFF;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.4);
}

/* Artboard boundary frame on canvas */
#artboard-bounds {
  position: absolute;
  pointer-events: none;
  border: 2px dashed rgba(13, 153, 255, 0.7);
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.4), 0 12px 48px rgba(0, 0, 0, 0.8);
  border-radius: 4px;
  transition: width 0.1s, height 0.1s;
  z-index: 1;
}
#artboard-badge {
  position: absolute;
  top: -26px;
  left: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: var(--figma-blue);
  color: #FFFFFF;
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 4px 4px 0 0;
  letter-spacing: 0.3px;
  text-transform: uppercase;
}
"""

html = html.replace('</head>', f'<style>{css_to_add}</style></head>')

# 2. Modify Header Brand & Center Tools
old_brand = """    <div class="top-left-brand" onclick="centerCanvas()">
      <svg style="width:16px;height:16px" viewBox="0 0 38 57" fill="none">
        <path d="M19 28.5A9.5 9.5 0 1 1 28.5 19 9.5 9.5 0 0 1 19 28.5z" fill="#1ABCFE"/>
        <path d="M0 47.5A9.5 9.5 0 0 1 9.5 38H19v9.5a9.5 9.5 0 1 1-19 0z" fill="#0ACF83"/>
        <path d="M19 0v19h9.5a9.5 9.5 0 1 0 0-19H19z" fill="#FF7262"/>
        <path d="M0 9.5A9.5 9.5 0 0 0 9.5 19H19V0H9.5A9.5 9.5 0 0 0 0 9.5z" fill="#F24E1E"/>
        <path d="M0 28.5A9.5 9.5 0 0 0 9.5 38H19V19H9.5A9.5 9.5 0 0 0 0 28.5z" fill="#A259FF"/>
      </svg>
      <span>Untitled</span>
      <svg class="svg-icon-sm" viewBox="0 0 24 24"><polyline points="6 9 12 15 18 9"/></svg>
    </div>"""

new_brand = """    <div class="top-left-brand" onclick="openProjectMenuModal()" title="Menu Proyek Uinify & Pengaturan">
      <svg style="width:16px;height:16px" viewBox="0 0 38 57" fill="none">
        <path d="M19 28.5A9.5 9.5 0 1 1 28.5 19 9.5 9.5 0 0 1 19 28.5z" fill="#1ABCFE"/>
        <path d="M0 47.5A9.5 9.5 0 0 1 9.5 38H19v9.5a9.5 9.5 0 1 1-19 0z" fill="#0ACF83"/>
        <path d="M19 0v19h9.5a9.5 9.5 0 1 0 0-19H19z" fill="#FF7262"/>
        <path d="M0 9.5A9.5 9.5 0 0 0 9.5 19H19V0H9.5A9.5 9.5 0 0 0 0 9.5z" fill="#F24E1E"/>
        <path d="M0 28.5A9.5 9.5 0 0 0 9.5 38H19V19H9.5A9.5 9.5 0 0 0 0 28.5z" fill="#A259FF"/>
      </svg>
      <span id="uinify-project-title">Uinify Studio</span>
      <div id="uinify-autosave-indicator" class="autosave-pill" title="Status Penyimpanan Otomatis">
        <svg class="svg-icon-micro" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
        <span id="uinify-autosave-text">Auto-Save</span>
      </div>
      <svg class="svg-icon-sm" viewBox="0 0 24 24"><polyline points="6 9 12 15 18 9"/></svg>
    </div>"""

if old_brand in html:
    html = html.replace(old_brand, new_brand)
    print("Replaced top brand with interactive Project Menu & Auto-Save pill.")
else:
    print("WARNING: old_brand not found verbatim, checking regex...")
    html = re.sub(r'<div class="top-left-brand" onclick="centerCanvas\(\)">[\s\S]*?<span>Untitled</span>[\s\S]*?</div>', new_brand, html, count=1)

# Center tools additions
mask_btn = """      <button class="top-icon-btn" id="btn-top-mask" onclick="toggleMask()" title="Toggle Mask (◐)">
        <svg class="svg-icon" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 2a10 10 0 0 0 0 20z" fill="currentColor"/></svg>
      </button>"""

tools_extension = """      <button class="top-icon-btn" id="btn-top-mask" onclick="toggleMask()" title="Toggle Mask">
        <svg class="svg-icon" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 2a10 10 0 0 0 0 20z" fill="currentColor"/></svg>
      </button>
      <!-- Mode Kanvas (Infinity / 1:1 / Custom / DPI) -->
      <button class="top-icon-btn" id="btn-canvas-mode" onclick="openCanvasModeModal()" title="Mode Kanvas & Ukuran (1:1, Infinity, Custom, DPI)">
        <svg class="svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8" cy="8" r="1.5" fill="currentColor"/><path d="M21 15l-5-5L5 21"/></svg>
      </button>
      <!-- Aksen Warna Tema (Bisa diganti sesuka hati) -->
      <button class="top-icon-btn" id="btn-accent-color" onclick="openAccentColorModal()" title="Aksen Warna Tema">
        <svg class="svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="7" r="2" fill="var(--figma-blue)"/><circle cx="16" cy="11" r="2" fill="#00D1B2"/><circle cx="14" cy="16" r="2" fill="#7038F4"/><circle cx="9" cy="15" r="2" fill="#F59E0B"/></svg>
      </button>
      <!-- Impor Seluruh Jenis File -->
      <button class="top-icon-btn" id="btn-import-all" onclick="openImportModal()" title="Impor File (.uify, .json, .svg, .png, .jpg, .html)">
        <svg class="svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
      </button>
      <!-- Ekspor Seluruh Jenis File (.uify, .svg, .png, .jpg, .json, .html) -->
      <button class="top-icon-btn" id="btn-export-all" onclick="openExportModal()" title="Ekspor File (.uify, .svg, .png, .jpg, .json, .html)">
        <svg class="svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
      </button>"""

if mask_btn in html:
    html = html.replace(mask_btn, tools_extension)
    print("Injected canvas mode, accent color, import, export into top center tools.")
else:
    print("WARNING: mask_btn not found verbatim, checking regex...")
    html = re.sub(r'<button class="top-icon-btn" id="btn-top-mask"[\s\S]*?</button>', tools_extension, html, count=1)

# 3. Add modals right before </body>
modals_html = """
  <!-- === 1. MODAL MENU PROYEK & AUTO-SAVE === -->
  <div class="figma-modal-backdrop" id="modal-project-backdrop" onclick="closeProjectMenuModal(event)">
    <div class="uinify-modal-card" id="modal-project-card" onclick="event.stopPropagation()">
      <div class="uinify-modal-header">
        <div class="uinify-modal-title">
          <svg style="width:16px;height:16px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
          <span>Menu Proyek</span>
        </div>
        <button class="uinify-modal-close-btn" onclick="closeProjectMenuModal()">
          <svg style="width:14px;height:14px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>

      <div style="display:flex; flex-direction:column; gap:6px;">
        <label style="font-size:11px; color:#9CA3AF; font-weight:600;">Nama Proyek</label>
        <input type="text" id="uinify-project-name-input" class="modern-input" value="Uinify Studio" style="width:100%; height:34px; padding:0 10px; font-size:12px; font-weight:600; color:#fff;" oninput="updateProjectTitle(this.value)">
      </div>

      <div style="display:flex; align-items:center; justify-content:space-between; padding:10px 12px; background:rgba(16,185,129,0.1); border:1px solid rgba(16,185,129,0.25); border-radius:8px;">
        <div style="display:flex; align-items:center; gap:8px;">
          <svg style="width:16px;height:16px; color:#10B981" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          <div>
            <div style="font-size:11px; font-weight:700; color:#10B981;">Auto-Save Aktif</div>
            <div id="autosave-last-time" style="font-size:10px; color:#9CA3AF;">Tersimpan otomatis ke .uify</div>
          </div>
        </div>
        <button class="pill-btn-sm" onclick="triggerManualSave()" style="background:var(--figma-blue); color:#fff; border:none; padding:4px 10px; font-size:10px; font-weight:700; border-radius:6px; cursor:pointer;">Simpan Sekarang</button>
      </div>

      <div style="display:flex; flex-direction:column; gap:6px;">
        <div class="uinify-action-row" onclick="openCanvasModeModal(); closeProjectMenuModal();">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:var(--figma-blue)" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/></svg>
            <span>Mode Kanvas & Ukuran</span>
          </div>
          <div class="uinify-action-desc" id="current-canvas-desc">Infinity</div>
        </div>

        <div class="uinify-action-row" onclick="openAccentColorModal(); closeProjectMenuModal();">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:var(--figma-blue)" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg>
            <span>Aksen Warna Tema</span>
          </div>
          <div class="uinify-action-desc" id="current-accent-desc">Figma Blue</div>
        </div>

        <div class="uinify-action-row" onclick="openExportModal(); closeProjectMenuModal();">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:#10B981" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            <span>Ekspor File (.uify, .svg, .png, dll)</span>
          </div>
          <div class="uinify-action-desc">Semua Format</div>
        </div>

        <div class="uinify-action-row" onclick="openImportModal(); closeProjectMenuModal();">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:#F59E0B" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            <span>Impor File Proyek / Aset</span>
          </div>
          <div class="uinify-action-desc">Semua Format</div>
        </div>

        <div class="uinify-action-row" onclick="resetCanvasConfirm()">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:#EF4444" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            <span>Reset Kanvas (Proyek Baru)</span>
          </div>
          <div class="uinify-action-desc">Bersihkan</div>
        </div>
      </div>
    </div>
  </div>

  <!-- === 2. MODAL MODE KANVAS, RESOLUSI & DPI === -->
  <div class="figma-modal-backdrop" id="modal-canvas-backdrop" onclick="closeCanvasModeModal(event)">
    <div class="uinify-modal-card" id="modal-canvas-card" onclick="event.stopPropagation()">
      <div class="uinify-modal-header">
        <div class="uinify-modal-title">
          <svg style="width:16px;height:16px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/></svg>
          <span>Mode Kanvas & Ukuran</span>
        </div>
        <button class="uinify-modal-close-btn" onclick="closeCanvasModeModal()">
          <svg style="width:14px;height:14px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>

      <!-- Mode Selector -->
      <div style="display:grid; grid-template-columns:1fr 1fr; gap:6px; background:rgba(255,255,255,0.06); padding:4px; border-radius:8px;">
        <button id="btn-mode-infinite" class="pill-btn-sm active" onclick="setCanvasMode('infinite')" style="height:32px; border-radius:6px; border:none; font-weight:700; cursor:pointer;">
          <svg style="width:12px;height:12px;margin-right:4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M8 12a4 4 0 1 0 8 0 4 4 0 1 0-8 0"/></svg>
          Infinity Canvas
        </button>
        <button id="btn-mode-fixed" class="pill-btn-sm" onclick="setCanvasMode('fixed')" style="height:32px; border-radius:6px; border:none; font-weight:700; cursor:pointer;">
          <svg style="width:12px;height:12px;margin-right:4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/></svg>
          Ukuran Tetap
        </button>
      </div>

      <!-- Preset Ukuran (1:1, 9:16, 16:9, dll) -->
      <div id="fixed-canvas-controls" style="display:none; flex-direction:column; gap:10px;">
        <label style="font-size:11px; color:#9CA3AF; font-weight:600;">Pilihan Preset Ukuran</label>
        <div class="uinify-grid-presets">
          <div class="uinify-preset-btn active" id="preset-1-1" onclick="applyCanvasPreset('1:1', 1080, 1080)">
            <div class="preset-label">
              <svg style="width:12px;height:12px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/></svg>
              <span>1:1 Square</span>
            </div>
            <div class="preset-dim">1080 × 1080 px (Feed)</div>
          </div>

          <div class="uinify-preset-btn" id="preset-9-16" onclick="applyCanvasPreset('9:16', 1080, 1920)">
            <div class="preset-label">
              <svg style="width:12px;height:12px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2"/></svg>
              <span>9:16 Story</span>
            </div>
            <div class="preset-dim">1080 × 1920 px (Reels)</div>
          </div>

          <div class="uinify-preset-btn" id="preset-16-9" onclick="applyCanvasPreset('16:9', 1920, 1080)">
            <div class="preset-label">
              <svg style="width:12px;height:12px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="14" rx="2"/></svg>
              <span>16:9 Landscape</span>
            </div>
            <div class="preset-dim">1920 × 1080 px (HD)</div>
          </div>

          <div class="uinify-preset-btn" id="preset-4-3" onclick="applyCanvasPreset('4:3', 1024, 768)">
            <div class="preset-label">
              <svg style="width:12px;height:12px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"/></svg>
              <span>4:3 Klasik</span>
            </div>
            <div class="preset-dim">1024 × 768 px</div>
          </div>

          <div class="uinify-preset-btn" id="preset-mobile" onclick="applyCanvasPreset('mobile', 412, 915)">
            <div class="preset-label">
              <svg style="width:12px;height:12px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2"/></svg>
              <span>Mobile UI</span>
            </div>
            <div class="preset-dim">412 × 915 px</div>
          </div>

          <div class="uinify-preset-btn" id="preset-tablet" onclick="applyCanvasPreset('tablet', 820, 1180)">
            <div class="preset-label">
              <svg style="width:12px;height:12px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="2" width="16" height="20" rx="2"/></svg>
              <span>Tablet UI</span>
            </div>
            <div class="preset-dim">820 × 1180 px</div>
          </div>
        </div>

        <!-- Custom Dimensions -->
        <div style="display:flex; flex-direction:column; gap:6px;">
          <label style="font-size:11px; color:#9CA3AF; font-weight:600;">Ukuran Kustom (Costume Width & Height)</label>
          <div style="display:grid; grid-template-columns:1fr 1fr; gap:8px;">
            <div>
              <span style="font-size:10px; color:#6B7280;">Lebar (px)</span>
              <input type="number" id="custom-canvas-w" class="modern-input" value="1080" style="width:100%; height:32px; padding:0 8px; font-size:12px; color:#fff;" oninput="applyCustomDimension()">
            </div>
            <div>
              <span style="font-size:10px; color:#6B7280;">Tinggi (px)</span>
              <input type="number" id="custom-canvas-h" class="modern-input" value="1080" style="width:100%; height:32px; padding:0 8px; font-size:12px; color:#fff;" oninput="applyCustomDimension()">
            </div>
          </div>
        </div>

        <!-- DPI & Resolusi -->
        <div style="display:flex; flex-direction:column; gap:6px;">
          <label style="font-size:11px; color:#9CA3AF; font-weight:600;">Resolusi & Kerapatan Piksel (DPI)</label>
          <div style="display:grid; grid-template-columns:repeat(3, 1fr); gap:6px;">
            <button class="pill-btn-sm active" id="btn-dpi-72" onclick="setCanvasDpi(72)" style="border-radius:6px; height:28px; font-size:10px; font-weight:600; cursor:pointer;">72 DPI (Web)</button>
            <button class="pill-btn-sm" id="btn-dpi-150" onclick="setCanvasDpi(150)" style="border-radius:6px; height:28px; font-size:10px; font-weight:600; cursor:pointer;">150 DPI (Med)</button>
            <button class="pill-btn-sm" id="btn-dpi-300" onclick="setCanvasDpi(300)" style="border-radius:6px; height:28px; font-size:10px; font-weight:600; cursor:pointer;">300 DPI (Cetak)</button>
          </div>
        </div>
      </div>

      <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:8px;">
        <button class="pill-btn-sm" onclick="closeCanvasModeModal()" style="background:var(--figma-blue); color:#fff; border:none; padding:8px 16px; font-weight:700; border-radius:6px; cursor:pointer;">Terapkan</button>
      </div>
    </div>
  </div>

  <!-- === 3. MODAL AKSEN WARNA TEMA (Bisa diganti sesuka hati) === -->
  <div class="figma-modal-backdrop" id="modal-accent-backdrop" onclick="closeAccentColorModal(event)">
    <div class="uinify-modal-card" id="modal-accent-card" onclick="event.stopPropagation()">
      <div class="uinify-modal-header">
        <div class="uinify-modal-title">
          <svg style="width:16px;height:16px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/></svg>
          <span>Aksen Warna Tema</span>
        </div>
        <button class="uinify-modal-close-btn" onclick="closeAccentColorModal()">
          <svg style="width:14px;height:14px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>

      <p style="font-size:11px; color:#9CA3AF; margin:0;">Pilih aksen warna antarmuka Figma UI3 Dark Pro sesuai preferensi Anda:</p>

      <div class="accent-swatch-list">
        <div class="accent-swatch active" style="background:#0D99FF" onclick="applyAccentColor('#0D99FF', 'Figma Blue')" title="Figma Neon Blue"></div>
        <div class="accent-swatch" style="background:#00D1B2" onclick="applyAccentColor('#00D1B2', 'Cyber Teal')" title="Cyber Teal"></div>
        <div class="accent-swatch" style="background:#7038F4" onclick="applyAccentColor('#7038F4', 'Electric Violet')" title="Electric Violet"></div>
        <div class="accent-swatch" style="background:#10B981" onclick="applyAccentColor('#10B981', 'Matrix Emerald')" title="Matrix Emerald"></div>
        <div class="accent-swatch" style="background:#F59E0B" onclick="applyAccentColor('#F59E0B', 'Amber Sunrise')" title="Amber Sunrise"></div>
        <div class="accent-swatch" style="background:#F43F5E" onclick="applyAccentColor('#F43F5E', 'Crimson Rose')" title="Crimson Rose"></div>
        <div class="accent-swatch" style="background:#E5E7EB" onclick="applyAccentColor('#E5E7EB', 'Monochrome')" title="Monochrome White"></div>
      </div>

      <div style="display:flex; flex-direction:column; gap:6px;">
        <label style="font-size:11px; color:#9CA3AF; font-weight:600;">Warna Kustom Hex</label>
        <div style="display:flex; align-items:center; gap:8px;">
          <input type="color" id="custom-accent-picker" value="#0D99FF" style="width:36px; height:34px; padding:0; border:none; background:transparent; cursor:pointer;" onchange="applyAccentColor(this.value, 'Custom')">
          <input type="text" id="custom-accent-text" class="modern-input" value="#0D99FF" style="flex:1; height:34px; padding:0 8px; font-size:12px; color:#fff;" oninput="if(/^#[0-9A-Fa-f]{6}$/.test(this.value)) applyAccentColor(this.value, 'Custom')">
        </div>
      </div>

      <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:8px;">
        <button class="pill-btn-sm" onclick="closeAccentColorModal()" style="background:var(--figma-blue); color:#fff; border:none; padding:8px 16px; font-weight:700; border-radius:6px; cursor:pointer;">Selesai</button>
      </div>
    </div>
  </div>

  <!-- === 4. MODAL EKSPOR SELURUH JENIS FILE === -->
  <div class="figma-modal-backdrop" id="modal-export-backdrop" onclick="closeExportModal(event)">
    <div class="uinify-modal-card" id="modal-export-card" onclick="event.stopPropagation()">
      <div class="uinify-modal-header">
        <div class="uinify-modal-title">
          <svg style="width:16px;height:16px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
          <span>Ekspor Seluruh Jenis File</span>
        </div>
        <button class="uinify-modal-close-btn" onclick="closeExportModal()">
          <svg style="width:14px;height:14px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>

      <p style="font-size:11px; color:#9CA3AF; margin:0;">Pilih format file ekspor yang diinginkan:</p>

      <div style="display:flex; flex-direction:column; gap:6px;">
        <!-- .uify -->
        <div class="uinify-action-row" onclick="exportFileAs('uify')">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:var(--figma-blue)" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            <div>
              <div style="font-weight:700; color:#fff;">Uinify Project (.uify)</div>
              <div style="font-size:10px; color:#9CA3AF;">Format native lengkap (layer, efek, kanvas)</div>
            </div>
          </div>
          <button class="pill-btn-sm" style="background:var(--figma-blue); color:#fff; border:none; padding:4px 8px; font-size:10px; border-radius:4px;">Ekspor</button>
        </div>

        <!-- .svg -->
        <div class="uinify-action-row" onclick="exportFileAs('svg')">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:#10B981" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 2 7 12 12 22 7 12 2"/></svg>
            <div>
              <div style="font-weight:700; color:#fff;">Vector Graphic (.svg)</div>
              <div style="font-size:10px; color:#9CA3AF;">Vektor murni resolusi tak terbatas</div>
            </div>
          </div>
          <button class="pill-btn-sm" style="background:#10B981; color:#fff; border:none; padding:4px 8px; font-size:10px; border-radius:4px;">Ekspor</button>
        </div>

        <!-- .png -->
        <div class="uinify-action-row" onclick="exportFileAs('png')">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:#00D1B2" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
            <div>
              <div style="font-weight:700; color:#fff;">Raster Image (.png)</div>
              <div style="font-size:10px; color:#9CA3AF;">Transparan tajam (1x, 2x, 3x)</div>
            </div>
          </div>
          <button class="pill-btn-sm" style="background:#00D1B2; color:#fff; border:none; padding:4px 8px; font-size:10px; border-radius:4px;">Ekspor</button>
        </div>

        <!-- .jpg -->
        <div class="uinify-action-row" onclick="exportFileAs('jpg')">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:#F59E0B" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/></svg>
            <div>
              <div style="font-weight:700; color:#fff;">Raster Image (.jpg)</div>
              <div style="font-size:10px; color:#9CA3AF;">Format foto dengan background</div>
            </div>
          </div>
          <button class="pill-btn-sm" style="background:#F59E0B; color:#fff; border:none; padding:4px 8px; font-size:10px; border-radius:4px;">Ekspor</button>
        </div>

        <!-- .json -->
        <div class="uinify-action-row" onclick="exportFileAs('json')">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:#7038F4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
            <div>
              <div style="font-weight:700; color:#fff;">Layer Schema (.json)</div>
              <div style="font-size:10px; color:#9CA3AF;">Data struktur layer dan bentuk</div>
            </div>
          </div>
          <button class="pill-btn-sm" style="background:#7038F4; color:#fff; border:none; padding:4px 8px; font-size:10px; border-radius:4px;">Ekspor</button>
        </div>

        <!-- .html -->
        <div class="uinify-action-row" onclick="exportFileAs('html')">
          <div class="uinify-action-label">
            <svg style="width:14px;height:14px; color:#F43F5E" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z"/></svg>
            <div>
              <div style="font-weight:700; color:#fff;">Web Canvas (.html)</div>
              <div style="font-size:10px; color:#9CA3AF;">Halaman web interaktif mandiri</div>
            </div>
          </div>
          <button class="pill-btn-sm" style="background:#F43F5E; color:#fff; border:none; padding:4px 8px; font-size:10px; border-radius:4px;">Ekspor</button>
        </div>
      </div>
    </div>
  </div>

  <!-- === 5. MODAL IMPOR SELURUH JENIS FILE === -->
  <div class="figma-modal-backdrop" id="modal-import-backdrop" onclick="closeImportModal(event)">
    <div class="uinify-modal-card" id="modal-import-card" onclick="event.stopPropagation()">
      <div class="uinify-modal-header">
        <div class="uinify-modal-title">
          <svg style="width:16px;height:16px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
          <span>Impor Seluruh Jenis File</span>
        </div>
        <button class="uinify-modal-close-btn" onclick="closeImportModal()">
          <svg style="width:14px;height:14px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>

      <p style="font-size:11px; color:#9CA3AF; margin:0;">Uinify mendukung impor seluruh jenis file desain dan gambar:</p>

      <div style="display:flex; flex-direction:column; gap:8px;">
        <div style="border: 2px dashed rgba(255,255,255,0.15); border-radius:10px; padding:20px 14px; text-align:center; background:rgba(255,255,255,0.02); cursor:pointer;" onclick="triggerUniversalImportPicker()">
          <svg style="width:32px;height:32px; color:var(--figma-blue); margin-bottom:8px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
          <div style="font-size:13px; font-weight:700; color:#fff;">Pilih File dari Perangkat</div>
          <div style="font-size:11px; color:#9CA3AF; margin-top:4px;">Mendukung .uify, .json, .svg, .png, .jpg, .webp, .html</div>
        </div>

        <input type="file" id="universal-file-input" style="display:none;" accept=".uify,.json,.svg,.png,.jpg,.jpeg,.webp,.html" onchange="handleUniversalFileSelect(this)">

        <div style="display:grid; grid-template-columns:1fr 1fr; gap:6px;">
          <button class="pill-btn-sm" onclick="triggerUniversalImportPicker('.uify')" style="height:32px; border-radius:6px; font-size:11px; font-weight:600; cursor:pointer;">
            <svg style="width:12px;height:12px;margin-right:4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/></svg>
            Impor .uify
          </button>
          <button class="pill-btn-sm" onclick="triggerUniversalImportPicker('image/*')" style="height:32px; border-radius:6px; font-size:11px; font-weight:600; cursor:pointer;">
            <svg style="width:12px;height:12px;margin-right:4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/></svg>
            Impor Gambar
          </button>
          <button class="pill-btn-sm" onclick="triggerUniversalImportPicker('.svg')" style="height:32px; border-radius:6px; font-size:11px; font-weight:600; cursor:pointer;">
            <svg style="width:12px;height:12px;margin-right:4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 2 7 12 12 22 7 12 2"/></svg>
            Impor SVG
          </button>
          <button class="pill-btn-sm" onclick="triggerUniversalImportPicker('.html')" style="height:32px; border-radius:6px; font-size:11px; font-weight:600; cursor:pointer;">
            <svg style="width:12px;height:12px;margin-right:4px;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
            Impor HTML
          </button>
        </div>
      </div>
    </div>
  </div>
"""

# Insert modals before </body>
html = html.replace('</body>', f'{modals_html}</body>')

# 4. Javascript logic for Auto-save, import, export, canvas mode, accent color
js_to_add = """
<script>
/* ====================================================================
   UINIFY STUDIO EXTENSION: AUTO-SAVE, FULL EXPORT/IMPORT, CANVAS MODE & ACCENT
   ==================================================================== */
(function() {
  window.currentProjectName = 'Uinify Studio';
  window.currentCanvasMode = 'infinite'; // 'infinite' or 'fixed'
  window.currentCanvasConfig = {
    preset: '1:1',
    width: 1080,
    height: 1080,
    dpi: 72,
    bg: '#1e1e1e'
  };
  window.currentAccentColor = '#0D99FF';

  // --- 1. MODAL OPEN / CLOSE HELPERS ---
  window.openProjectMenuModal = function() {
    const el = document.getElementById('modal-project-backdrop');
    if (el) el.classList.add('open');
    updateAutoSaveBadgeStatus();
  };
  window.closeProjectMenuModal = function(e) {
    if (e && e.target !== e.currentTarget) return;
    const el = document.getElementById('modal-project-backdrop');
    if (el) el.classList.remove('open');
  };

  window.openCanvasModeModal = function() {
    const el = document.getElementById('modal-canvas-backdrop');
    if (el) el.classList.add('open');
  };
  window.closeCanvasModeModal = function(e) {
    if (e && e.target !== e.currentTarget) return;
    const el = document.getElementById('modal-canvas-backdrop');
    if (el) el.classList.remove('open');
  };

  window.openAccentColorModal = function() {
    const el = document.getElementById('modal-accent-backdrop');
    if (el) el.classList.add('open');
  };
  window.closeAccentColorModal = function(e) {
    if (e && e.target !== e.currentTarget) return;
    const el = document.getElementById('modal-accent-backdrop');
    if (el) el.classList.remove('open');
  };

  window.openExportModal = function() {
    const el = document.getElementById('modal-export-backdrop');
    if (el) el.classList.add('open');
  };
  window.closeExportModal = function(e) {
    if (e && e.target !== e.currentTarget) return;
    const el = document.getElementById('modal-export-backdrop');
    if (el) el.classList.remove('open');
  };

  window.openImportModal = function() {
    const el = document.getElementById('modal-import-backdrop');
    if (el) el.classList.add('open');
  };
  window.closeImportModal = function(e) {
    if (e && e.target !== e.currentTarget) return;
    const el = document.getElementById('modal-import-backdrop');
    if (el) el.classList.remove('open');
  };

  window.updateProjectTitle = function(name) {
    window.currentProjectName = name || 'Untitled';
    const display = document.getElementById('uinify-project-title');
    if (display) display.textContent = window.currentProjectName;
    triggerAutoSave();
  };

  // --- 2. ACCENT COLOR CUSTOMIZATION ---
  window.applyAccentColor = function(colorHex, name) {
    window.currentAccentColor = colorHex;
    document.documentElement.style.setProperty('--figma-blue', colorHex);
    document.documentElement.style.setProperty('--accent-penpot', colorHex);
    document.documentElement.style.setProperty('--uinify-accent', colorHex);

    const desc = document.getElementById('current-accent-desc');
    if (desc) desc.textContent = name || colorHex;

    const picker = document.getElementById('custom-accent-picker');
    if (picker) picker.value = colorHex;
    const textInp = document.getElementById('custom-accent-text');
    if (textInp) textInp.value = colorHex;

    document.querySelectorAll('.accent-swatch').forEach(sw => {
      sw.classList.toggle('active', sw.style.backgroundColor === colorHex || sw.getAttribute('title')?.toLowerCase().includes(name?.toLowerCase() || ''));
    });

    if (window.UinifyNative && typeof window.UinifyNative.setAccentColor === 'function') {
      window.UinifyNative.setAccentColor(colorHex);
    }
    triggerAutoSave();
  };

  // --- 3. CANVAS MODE & RESOLUTION ENGINE ---
  window.setCanvasMode = function(mode) {
    window.currentCanvasMode = mode;
    const btnInf = document.getElementById('btn-mode-infinite');
    const btnFix = document.getElementById('btn-mode-fixed');
    const fixCtrls = document.getElementById('fixed-canvas-controls');
    const desc = document.getElementById('current-canvas-desc');

    if (mode === 'infinite') {
      if (btnInf) btnInf.classList.add('active');
      if (btnFix) btnFix.classList.remove('active');
      if (fixCtrls) fixCtrls.style.display = 'none';
      if (desc) desc.textContent = 'Infinity Canvas';
      removeArtboardVisual();
    } else {
      if (btnInf) btnInf.classList.remove('active');
      if (btnFix) btnFix.classList.add('active');
      if (fixCtrls) fixCtrls.style.display = 'flex';
      if (desc) desc.textContent = `${window.currentCanvasConfig.preset} (${window.currentCanvasConfig.width}x${window.currentCanvasConfig.height})`;
      renderArtboardVisual();
    }
    triggerAutoSave();
  };

  window.applyCanvasPreset = function(preset, w, h) {
    window.currentCanvasConfig.preset = preset;
    window.currentCanvasConfig.width = w;
    window.currentCanvasConfig.height = h;

    const inpW = document.getElementById('custom-canvas-w');
    const inpH = document.getElementById('custom-canvas-h');
    if (inpW) inpW.value = w;
    if (inpH) inpH.value = h;

    document.querySelectorAll('.uinify-preset-btn').forEach(btn => btn.classList.remove('active'));
    const activeBtn = document.getElementById(`preset-${preset.replace(':', '-')}`);
    if (activeBtn) activeBtn.classList.add('active');

    const desc = document.getElementById('current-canvas-desc');
    if (desc) desc.textContent = `${preset} (${w}x${h})`;

    renderArtboardVisual();
    triggerAutoSave();
  };

  window.applyCustomDimension = function() {
    const w = parseInt(document.getElementById('custom-canvas-w')?.value) || 1080;
    const h = parseInt(document.getElementById('custom-canvas-h')?.value) || 1080;
    window.currentCanvasConfig.preset = 'Costume';
    window.currentCanvasConfig.width = w;
    window.currentCanvasConfig.height = h;

    document.querySelectorAll('.uinify-preset-btn').forEach(btn => btn.classList.remove('active'));
    renderArtboardVisual();
    triggerAutoSave();
  };

  window.setCanvasDpi = function(dpi) {
    window.currentCanvasConfig.dpi = dpi;
    ['72', '150', '300'].forEach(d => {
      const b = document.getElementById(`btn-dpi-${d}`);
      if (b) b.classList.toggle('active', parseInt(d) === dpi);
    });
    renderArtboardVisual();
    triggerAutoSave();
  };

  function renderArtboardVisual() {
    if (window.currentCanvasMode !== 'fixed') {
      removeArtboardVisual();
      return;
    }
    const vp = document.getElementById('viewport-container');
    if (!vp) return;

    let art = document.getElementById('artboard-bounds');
    if (!art) {
      art = document.createElement('div');
      art.id = 'artboard-bounds';
      vp.prepend(art);
    }

    const w = window.currentCanvasConfig.width;
    const h = window.currentCanvasConfig.height;
    const dpi = window.currentCanvasConfig.dpi;
    const preset = window.currentCanvasConfig.preset;

    art.style.left = '0px';
    art.style.top = '0px';
    art.style.width = w + 'px';
    art.style.height = h + 'px';
    art.style.background = window.currentCanvasConfig.bg || '#1e1e1e';

    art.innerHTML = `<div id="artboard-badge"><svg style="width:10px;height:10px" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><rect x="3" y="3" width="18" height="18" rx="2"/></svg> <span>${preset} • ${w} × ${h} px • ${dpi} DPI</span></div>`;
  }

  function removeArtboardVisual() {
    const art = document.getElementById('artboard-bounds');
    if (art) art.remove();
  }

  // --- 4. REAL-TIME AUTO-SAVE ENGINE ---
  let autoSaveTimer = null;
  let lastSavedJson = '';

  window.triggerAutoSave = function() {
    if (autoSaveTimer) clearTimeout(autoSaveTimer);

    const ind = document.getElementById('uinify-autosave-indicator');
    const txt = document.getElementById('uinify-autosave-text');
    if (ind) ind.classList.add('saving');
    if (txt) txt.textContent = 'Menyimpan...';

    autoSaveTimer = setTimeout(() => {
      executeAutoSave();
    }, 280);
  };

  function executeAutoSave() {
    try {
      const payload = {
        uinify: true,
        version: '1.0',
        projectName: window.currentProjectName || 'Uinify Studio',
        canvasMode: window.currentCanvasMode || 'infinite',
        canvasConfig: window.currentCanvasConfig,
        accentColor: window.currentAccentColor || '#0D99FF',
        savedAt: new Date().toISOString(),
        shapes: window.state?.shapes || [],
        viewport: window.state?.viewport || { x: 80, y: 80, zoom: 0.9 }
      };

      const jsonStr = JSON.stringify(payload);
      if (jsonStr === lastSavedJson) {
        updateAutoSaveBadgeStatus();
        return;
      }
      lastSavedJson = jsonStr;

      // Persist to browser / WebView DOM Storage
      localStorage.setItem('uinify_autosave_project', jsonStr);

      // Persist to native Android private filesystem
      if (window.UinifyNative && typeof window.UinifyNative.autoSave === 'function') {
        window.UinifyNative.autoSave(jsonStr);
      }

      updateAutoSaveBadgeStatus();
    } catch (err) {
      console.warn('Auto-save error:', err);
    }
  }

  function updateAutoSaveBadgeStatus() {
    const ind = document.getElementById('uinify-autosave-indicator');
    const txt = document.getElementById('uinify-autosave-text');
    const timeEl = document.getElementById('autosave-last-time');

    if (ind) ind.classList.remove('saving');
    if (txt) txt.textContent = 'Auto-Save';

    const now = new Date();
    const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
    if (timeEl) timeEl.textContent = `Tersimpan otomatis ke .uify pukul ${timeStr}`;
  }

  window.triggerManualSave = function() {
    executeAutoSave();
    exportFileAs('uify');
    if (typeof showToast === 'function') showToast('Proyek .uify berhasil disimpan!');
  };

  window.resetCanvasConfirm = function() {
    if (confirm('Apakah Anda yakin ingin memulai proyek baru? Semua kanvas akan diatur ulang.')) {
      if (window.state) {
        window.state.shapes = [];
        window.state.selectedId = null;
        window.state.selectedIds = [];
      }
      if (typeof renderAllShapes === 'function') renderAllShapes();
      if (typeof renderLayers === 'function') renderLayers();
      if (typeof renderInspector === 'function') renderInspector();
      if (typeof saveHistory === 'function') saveHistory();
      triggerAutoSave();
      closeProjectMenuModal();
      if (typeof showToast === 'function') showToast('Kanvas telah diatur ulang');
    }
  };

  // Hook into history and events
  const originalSaveHistory = window.saveHistory;
  window.saveHistory = function() {
    const res = originalSaveHistory ? originalSaveHistory.apply(this, arguments) : undefined;
    triggerAutoSave();
    return res;
  };

  document.addEventListener('input', () => triggerAutoSave(), { passive: true });
  document.addEventListener('change', () => triggerAutoSave(), { passive: true });
  document.addEventListener('pointerup', () => triggerAutoSave(), { passive: true });

  // --- 5. UNIVERSAL EXPORT ENGINE (ALL FORMATS) ---
  window.exportFileAs = function(format) {
    closeExportModal();
    const baseName = (window.currentProjectName || 'uinify-project').toLowerCase().replace(/[^a-z0-9_-]+/g, '-');

    if (format === 'uify') {
      const payload = {
        uinify: true,
        version: '1.0',
        projectName: window.currentProjectName || 'Uinify Studio',
        canvasMode: window.currentCanvasMode || 'infinite',
        canvasConfig: window.currentCanvasConfig,
        accentColor: window.currentAccentColor || '#0D99FF',
        createdAt: new Date().toISOString(),
        shapes: window.state?.shapes || [],
        viewport: window.state?.viewport || { x: 80, y: 80, zoom: 0.9 }
      };
      const text = JSON.stringify(payload, null, 2);
      downloadOrNativeShare(baseName + '.uify', 'application/octet-stream', text, false);
      if (typeof showToast === 'function') showToast('Proyek diekspor: ' + baseName + '.uify');
    }
    else if (format === 'json') {
      const text = JSON.stringify(window.state?.shapes || [], null, 2);
      downloadOrNativeShare(baseName + '-shapes.json', 'application/json', text, false);
      if (typeof showToast === 'function') showToast('Layer diekspor: ' + baseName + '.json');
    }
    else if (format === 'svg') {
      if (typeof exportElementSvg === 'function' && window.state?.selectedId) {
        exportElementSvg();
      } else {
        // Export entire canvas SVG
        const shapes = window.state?.shapes || [];
        const svgContent = generateFullSvg(shapes);
        downloadOrNativeShare(baseName + '.svg', 'image/svg+xml', svgContent, false);
        if (typeof showToast === 'function') showToast('Vektor diekspor: ' + baseName + '.svg');
      }
    }
    else if (format === 'png' || format === 'jpg') {
      if (typeof exportSelectedRaster === 'function' && window.state?.selectedIds?.length) {
        exportSelectedRaster(format);
      } else {
        exportFullCanvasRaster(format);
      }
    }
    else if (format === 'html') {
      const htmlContent = generatePresentationHtml();
      downloadOrNativeShare(baseName + '.html', 'text/html', htmlContent, false);
      if (typeof showToast === 'function') showToast('HTML diekspor: ' + baseName + '.html');
    }
  };

  function downloadOrNativeShare(filename, mimeType, content, isBase64) {
    if (window.UinifyNative && typeof window.UinifyNative.exportFile === 'function') {
      window.UinifyNative.exportFile(filename, mimeType, content, isBase64);
    } else {
      // Web fallback
      let url;
      if (isBase64) {
        url = content;
      } else {
        const blob = new Blob([content], { type: mimeType });
        url = URL.createObjectURL(blob);
      }
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      setTimeout(() => {
        document.body.removeChild(a);
        if (!isBase64) URL.revokeObjectURL(url);
      }, 1200);
    }
  }

  function generateFullSvg(shapes) {
    let minX = 0, minY = 0, maxX = 1200, maxY = 800;
    if (shapes.length > 0) {
      minX = Math.min(...shapes.map(s => s.x || 0));
      minY = Math.min(...shapes.map(s => s.y || 0));
      maxX = Math.max(...shapes.map(s => (s.x || 0) + (s.w || 100)));
      maxY = Math.max(...shapes.map(s => (s.y || 0) + (s.h || 100)));
    }
    const width = Math.max(100, maxX - minX + 80);
    const height = Math.max(100, maxY - minY + 80);

    const vp = document.getElementById('viewport-container');
    const inner = vp ? vp.innerHTML : '';
    return `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="${minX - 40} ${minY - 40} ${width} ${height}">
  <style>text { font-family: 'Inter', sans-serif; }</style>
  <rect x="${minX - 40}" y="${minY - 40}" width="${width}" height="${height}" fill="#1e1e1e"/>
  ${inner}
</svg>`;
  }

  function exportFullCanvasRaster(format) {
    const scale = window.currentCanvasConfig?.dpi === 300 ? 3 : (window.currentCanvasConfig?.dpi === 150 ? 2 : 1);
    const canvas = document.createElement('canvas');
    const w = window.currentCanvasConfig?.width || 1200;
    const h = window.currentCanvasConfig?.height || 800;
    canvas.width = w * scale;
    canvas.height = h * scale;
    const ctx = canvas.getContext('2d');
    ctx.scale(scale, scale);
    ctx.fillStyle = format === 'jpg' ? '#1e1e1e' : 'transparent';
    ctx.fillRect(0, 0, w, h);

    const mime = format === 'jpg' ? 'image/jpeg' : 'image/png';
    canvas.toBlob(blob => {
      if (!blob) {
        if (typeof showToast === 'function') showToast('Ekspor raster gagal');
        return;
      }
      const filename = (window.currentProjectName || 'canvas') + '.' + format;
      const reader = new FileReader();
      reader.onloadend = function() {
        downloadOrNativeShare(filename, mime, reader.result, true);
        if (typeof showToast === 'function') showToast('Ekspor selesai: ' + filename);
      };
      reader.readAsDataURL(blob);
    }, mime, 0.92);
  }

  function generatePresentationHtml() {
    const vp = document.getElementById('viewport-container');
    return `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>${window.currentProjectName || 'Uinify Design'}</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { margin:0; padding:20px; background:#121212; color:#fff; font-family:'Inter',sans-serif; display:flex; justify-content:center; align-items:center; min-height:100vh; }
    .stage { position:relative; width:${window.currentCanvasConfig?.width || 1080}px; height:${window.currentCanvasConfig?.height || 1080}px; background:#1e1e1e; border-radius:12px; box-shadow:0 20px 60px rgba(0,0,0,0.8); overflow:hidden; }
  </style>
</head>
<body>
  <div class="stage">
    ${vp ? vp.innerHTML : ''}
  </div>
</body>
</html>`;
  }

  // --- 6. UNIVERSAL IMPORT ENGINE ---
  window.triggerUniversalImportPicker = function(filter) {
    closeImportModal();
    if (window.UinifyNative && typeof window.UinifyNative.requestImport === 'function') {
      window.UinifyNative.requestImport(filter || '*/*');
    } else {
      const inp = document.getElementById('universal-file-input');
      if (inp) {
        if (filter) inp.accept = filter;
        else inp.accept = '.uify,.json,.svg,.png,.jpg,.jpeg,.webp,.html';
        inp.click();
      }
    }
  };

  window.handleUniversalFileSelect = function(input) {
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;

    const name = file.name;
    const ext = name.split('.').pop()?.toLowerCase();

    if (ext === 'uify' || ext === 'json') {
      file.text().then(text => window.handleImportedFile(name, 'application/json', text));
    } else if (ext === 'svg' || ext === 'html' || ext === 'htm') {
      file.text().then(text => window.handleImportedFile(name, ext === 'svg' ? 'image/svg+xml' : 'text/html', text));
    } else if (['png', 'jpg', 'jpeg', 'webp', 'gif'].includes(ext)) {
      const reader = new FileReader();
      reader.onload = e => window.handleImportedFile(name, 'image/' + ext, e.target?.result);
      reader.readAsDataURL(file);
    }
  };

  window.handleImportedFile = function(filename, mimeType, content) {
    try {
      const ext = filename.split('.').pop()?.toLowerCase();

      // 1. .uify or JSON project file
      if (ext === 'uify' || (ext === 'json' && content.includes('"shapes"'))) {
        const data = JSON.parse(content);
        if (data.projectName) updateProjectTitle(data.projectName);
        if (data.accentColor) applyAccentColor(data.accentColor);
        if (data.canvasMode) setCanvasMode(data.canvasMode);
        if (data.canvasConfig) {
          window.currentCanvasConfig = data.canvasConfig;
          applyCanvasPreset(data.canvasConfig.preset || '1:1', data.canvasConfig.width || 1080, data.canvasConfig.height || 1080);
          if (data.canvasConfig.dpi) setCanvasDpi(data.canvasConfig.dpi);
        }
        if (Array.isArray(data.shapes)) {
          window.state.shapes = data.shapes;
        } else if (Array.isArray(data)) {
          window.state.shapes = data;
        }
        if (typeof renderAllShapes === 'function') renderAllShapes();
        if (typeof renderLayers === 'function') renderLayers();
        if (typeof renderInspector === 'function') renderInspector();
        if (typeof saveHistory === 'function') saveHistory();
        if (typeof showToast === 'function') showToast(`Proyek "${filename}" berhasil diimpor!`);
        return;
      }

      // 2. SVG file
      if (ext === 'svg' || mimeType?.includes('svg')) {
        const id = 'svg-' + Date.now();
        const shape = {
          id: id,
          name: filename.replace('.svg', ''),
          type: 'svg',
          x: 120,
          y: 120,
          w: 240,
          h: 240,
          svg: content,
          opacity: 100
        };
        window.state.shapes.push(shape);
        window.state.selectedId = id;
        window.state.selectedIds = [id];
        if (typeof renderAllShapes === 'function') renderAllShapes();
        if (typeof renderLayers === 'function') renderLayers();
        if (typeof saveHistory === 'function') saveHistory();
        if (typeof showToast === 'function') showToast(`SVG "${filename}" diimpor!`);
        return;
      }

      // 3. HTML file
      if (ext === 'html' || ext === 'htm') {
        if (typeof importHtml === 'function') {
          importHtml(content, filename.replace(/\.html?/i, ''));
        } else {
          if (typeof showToast === 'function') showToast('Engine impor HTML siap');
        }
        return;
      }

      // 4. Raster Image (.png, .jpg, .webp)
      if (['png', 'jpg', 'jpeg', 'webp'].includes(ext) || mimeType?.startsWith('image/')) {
        const id = 'img-' + Date.now();
        const img = new Image();
        img.onload = function() {
          const w = Math.min(600, img.naturalWidth || 300);
          const h = Math.round(w * ((img.naturalHeight || 300) / (img.naturalWidth || 300)));
          const shape = {
            id: id,
            name: filename,
            type: 'rectangle',
            x: 100,
            y: 100,
            w: w,
            h: h,
            fillType: 'image',
            imageUrl: content,
            fill: '#ffffff',
            opacity: 100
          };
          window.state.shapes.push(shape);
          window.state.selectedId = id;
          window.state.selectedIds = [id];
          if (typeof renderAllShapes === 'function') renderAllShapes();
          if (typeof renderLayers === 'function') renderLayers();
          if (typeof saveHistory === 'function') saveHistory();
          if (typeof showToast === 'function') showToast(`Gambar "${filename}" ditambahkan!`);
        };
        img.src = content;
        return;
      }
    } catch (err) {
      console.error('Import error:', err);
      if (typeof showToast === 'function') showToast('Gagal mengimpor file: ' + err.message);
    }
  };

  // --- 7. RESTORE SAVED STATE ON STARTUP ---
  window.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
      try {
        let savedData = null;
        if (window.UinifyNative && typeof window.UinifyNative.getSavedProject === 'function') {
          const nativeStr = window.UinifyNative.getSavedProject();
          if (nativeStr && nativeStr.length > 20) {
            savedData = JSON.parse(nativeStr);
          }
        }
        if (!savedData) {
          const localStr = localStorage.getItem('uinify_autosave_project');
          if (localStr && localStr.length > 20) {
            savedData = JSON.parse(localStr);
          }
        }

        if (savedData) {
          if (savedData.projectName) updateProjectTitle(savedData.projectName);
          if (savedData.accentColor) applyAccentColor(savedData.accentColor);
          if (savedData.canvasMode) setCanvasMode(savedData.canvasMode);
          if (savedData.canvasConfig) {
            window.currentCanvasConfig = savedData.canvasConfig;
            applyCanvasPreset(savedData.canvasConfig.preset || '1:1', savedData.canvasConfig.width || 1080, savedData.canvasConfig.height || 1080);
            if (savedData.canvasConfig.dpi) setCanvasDpi(savedData.canvasConfig.dpi);
          }
          if (Array.isArray(savedData.shapes) && savedData.shapes.length > 0) {
            window.state.shapes = savedData.shapes;
          }
          if (typeof renderAllShapes === 'function') renderAllShapes();
          if (typeof renderLayers === 'function') renderLayers();
          if (typeof renderInspector === 'function') renderInspector();
          updateAutoSaveBadgeStatus();
        }
      } catch (e) {
        console.warn('Initial project restore error:', e);
      }
    }, 150);
  });

})();
</script>
"""

# Insert JS before </body>
html = html.replace('</body>', f'{js_to_add}</body>')

# Write output to app/src/main/assets/editor.html
out_path = 'app/src/main/assets/editor.html'
with open(out_path, 'w', encoding='utf-8') as f:
    f.write(html)

print(f"SUCCESS: Created {out_path} ({os.path.getsize(out_path)} bytes).")
