import re
import os

with open('public/assets/aistudio/Uinfy02-fixed-1_11611382.html', 'r', encoding='utf-8') as f:
    html = f.read()

# 1. CSS Additions for Auto-Save, Canvas Mode Artboard, Accent Color, and Modals
css_additions = """
/* === UINIFY NATIVE INTEGRATION & ENHANCEMENTS CSS === */
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
  padding: 8px 10px;
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
}

.accent-swatch-list {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 4px 0;
}
.accent-swatch {
  width: 32px;
  height: 32px;
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

/* Artboard boundary frame on infinite canvas */
#artboard-bounds {
  position: absolute;
  pointer-events: none;
  border: 2px dashed rgba(13, 153, 255, 0.7);
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.35), 0 10px 40px rgba(0, 0, 0, 0.7);
  border-radius: 4px;
  transition: all 0.2s ease;
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

# Insert CSS into <head>
html = html.replace('</head>', f'<style>{css_additions}</style></head>')

print('CSS additions injected.')
