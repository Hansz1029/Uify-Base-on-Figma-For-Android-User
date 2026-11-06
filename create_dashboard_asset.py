import re
import os

with open('public/assets/aistudio/Uinify_Studio_Homescreen_Dashboard__1__ac6da119.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Replace emojis with pure SVG icons
# 1. 👋 in Welcome Back
html = html.replace('Hanszx</span> 👋', 'Hanszx</span>')

# 2. 🌙 and ☀️ in theme buttons
moon_svg = '<svg class="w-4 h-4 inline-block mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"/></svg>'
sun_svg = '<svg class="w-4 h-4 inline-block mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><circle cx="12" cy="12" r="5" stroke-width="2"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 1v2m0 18v2M4.22 4.22l1.42 1.42m12.72 12.72l1.42 1.42M1 12h2m18 0h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>'

html = html.replace('<span>🌙 Dark AMOLED</span>', f'<span class="flex items-center justify-center">{moon_svg} Dark AMOLED</span>')
html = html.replace('<span>☀️ Light Neumorphic</span>', f'<span class="flex items-center justify-center">{sun_svg} Light Neumorphic</span>')

# 3. 📱 and 🖥️ in orientation buttons
phone_svg = '<svg class="w-3.5 h-3.5 inline-block mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><rect x="5" y="2" width="14" height="20" rx="2" stroke-width="2"/></svg>'
monitor_svg = '<svg class="w-3.5 h-3.5 inline-block mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><rect x="2" y="3" width="20" height="14" rx="2" stroke-width="2"/><line x1="8" y1="21" x2="16" y2="21" stroke-width="2"/><line x1="12" y1="17" x2="12" y2="21" stroke-width="2"/></svg>'

html = html.replace('Portrait 📱', f'Portrait')
html = html.replace('Landscape 🖥️', f'Landscape')
html = html.replace('Landscape 🖥', f'Landscape')

# Replace image placeholder 14776.png with clean Uinify SVG logo
uinify_svg_logo = '''<svg class="w-full h-full text-white" viewBox="0 0 32 32" fill="none">
  <rect width="32" height="32" rx="9" fill="#0D99FF"/>
  <path d="M10 9v7a6 6 0 0 0 12 0V9" stroke="#ffffff" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
  <circle cx="22" cy="9" r="2" fill="#ffffff"/>
</svg>'''

html = re.sub(r'<img src="14776\.png"[^>]*>', uinify_svg_logo, html)

# Hook into window.UinifyNative for Room DB integration
native_bridge_hook = """
<script>
// --- UINIFY NATIVE BRIDGE INTEGRATION ---
(function() {
  window.addEventListener('DOMContentLoaded', () => {
    loadProjectsFromNative();
  });

  window.loadProjectsFromNative = function() {
    if (window.UinifyNative && typeof window.UinifyNative.getProjectsJson === 'function') {
      try {
        const jsonStr = window.UinifyNative.getProjectsJson();
        if (jsonStr && jsonStr.length > 5) {
          const loaded = JSON.parse(jsonStr);
          if (Array.isArray(loaded) && loaded.length > 0) {
            projectsState = loaded.map(p => ({
              id: p.id,
              title: p.title,
              w: p.canvasWidth || 1080,
              h: p.canvasHeight || 1080,
              date: formatTimestamp(p.lastUpdated),
              count: (p.layerCount || 1) + ' Layer',
              size: (Math.round((p.uifyContent?.length || 2048) / 1024 * 10) / 10) + ' KB',
              fmt: 'UIFY',
              img: p.thumbnailUrl || 'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?q=80&w=400&auto=format&fit=crop'
            }));
            renderRecentProjects();
            updateStorageMetrics();
          }
        }
      } catch (err) {
        console.warn('Load native projects err:', err);
      }
    }
  };

  function formatTimestamp(ts) {
    if (!ts) return 'Hari ini';
    const d = new Date(ts);
    return d.toLocaleDateString('id-ID', { day: 'numeric', month: 'short' });
  }

  function updateStorageMetrics() {
    const totalBytes = projectsState.reduce((acc, p) => acc + (parseFloat(p.size) || 1) * 1024, 0);
    const mb = (totalBytes / (1024 * 1024)).toFixed(1);
    const valEl = document.getElementById('home-storage-val');
    if (valEl) valEl.innerHTML = `${mb > 0.1 ? mb : '2.4'} <span class="text-sm font-normal text-gray-500">MB</span>`;
  }

  // Override launchEditorFromModal to notify Native
  const originalLaunchEditor = window.launchEditorFromModal;
  window.launchEditorFromModal = function() {
    if (activeProjectTarget && window.UinifyNative && typeof window.UinifyNative.openProject === 'function') {
      window.UinifyNative.openProject(activeProjectTarget.id);
      closeModal('modal-project-details');
    } else {
      if (typeof originalLaunchEditor === 'function') originalLaunchEditor();
    }
  };

  // Override confirmCreateFromPreset to notify Native
  window.confirmCreateFromPreset = function() {
    if (!activePresetTarget) return;
    const w = parseInt(document.getElementById('preset-modal-w').value) || activePresetTarget.w;
    const h = parseInt(document.getElementById('preset-modal-h').value) || activePresetTarget.h;
    const title = `${activePresetTarget.title} Project`;

    if (window.UinifyNative && typeof window.UinifyNative.createNewProject === 'function') {
      window.UinifyNative.createNewProject(title, w, h, activePresetTarget.title);
      closeModal('modal-preset-config');
    } else {
      // Fallback
      window.location.href = 'editor.html';
    }
  };

  // Override handleCreateNewCanvasSubmit to notify Native
  window.handleCreateNewCanvasSubmit = function(e) {
    e.preventDefault();
    const title = document.getElementById('new-canvas-title').value;
    const w = parseInt(document.getElementById('new-canvas-w').value) || 375;
    const h = parseInt(document.getElementById('new-canvas-h').value) || 812;

    if (window.UinifyNative && typeof window.UinifyNative.createNewProject === 'function') {
      window.UinifyNative.createNewProject(title, w, h, 'Custom');
      closeModal('modal-new-canvas');
    } else {
      window.location.href = 'editor.html';
    }
  };

  // Override duplicate project
  const origDup = window.duplicateCurrentProjectFromModal;
  window.duplicateCurrentProjectFromModal = function() {
    if (activeProjectTarget && window.UinifyNative && typeof window.UinifyNative.duplicateProject === 'function') {
      window.UinifyNative.duplicateProject(activeProjectTarget.id);
      closeModal('modal-project-details');
      setTimeout(loadProjectsFromNative, 200);
    } else {
      if (typeof origDup === 'function') origDup();
    }
  };

  // Override delete project
  const origDel = window.deleteCurrentProjectFromModal;
  window.deleteCurrentProjectFromModal = function() {
    if (activeProjectTarget && window.UinifyNative && typeof window.UinifyNative.deleteProject === 'function') {
      window.UinifyNative.deleteProject(activeProjectTarget.id);
      closeModal('modal-project-details');
      setTimeout(loadProjectsFromNative, 200);
    } else {
      if (typeof origDel === 'function') origDel();
    }
  };

  // Universal export file
  window.exportProjectFile = function() {
    if (window.UinifyNative && typeof window.UinifyNative.exportAllBackup === 'function') {
      window.UinifyNative.exportAllBackup();
    } else {
      showToast('Cadangan proyek diekspor');
    }
  };
})();
</script>
"""

html = html.replace('</body>', f'{native_bridge_hook}</body>')

with open('app/src/main/assets/dashboard.html', 'w', encoding='utf-8') as f:
    f.write(html)

print("SUCCESS: Created app/src/main/assets/dashboard.html with zero emojis and Room DB hooks.")
