import re

with open('app/src/main/assets/editor.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Replace .top-clean-bar styling to enable horizontal swipe/scroll
old_css_pattern = r'\.top-clean-bar\s*\{[\s\S]*?z-index:\s*90;\s*transition:\s*all\s*0\.2s\s*ease;\s*\}'
new_capsule_css = """    .top-clean-bar {
      position: fixed;
      top: 10px;
      left: 10px;
      right: 10px;
      height: 52px;
      background: rgba(0, 0, 0, 0.92);
      backdrop-filter: blur(28px);
      -webkit-backdrop-filter: blur(28px);
      border: 1px solid rgba(255, 255, 255, 0.16);
      border-radius: 9999px;
      box-shadow: 0 16px 40px rgba(0, 0, 0, 0.85), 0 0 1px rgba(255, 255, 255, 0.3);
      display: flex;
      align-items: center;
      justify-content: flex-start;
      gap: 12px;
      padding: 0 16px;
      z-index: 100;
      overflow-x: auto;
      overflow-y: hidden;
      white-space: nowrap;
      -webkit-overflow-scrolling: touch;
      scrollbar-width: none;
      touch-action: pan-x;
    }
    .top-clean-bar::-webkit-scrollbar {
      display: none;
    }
    .top-clean-bar > * {
      flex-shrink: 0;
    }
    .top-left-brand {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .top-center-tools {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .top-right-group {
      flex-shrink: 0;
      display: flex;
      align-items: center;
      gap: 6px;
    }"""

html = re.sub(old_css_pattern, new_capsule_css, html, count=1)

# Add momentum touch/drag script for the floating capsule
drag_script = """
<script>
// Enable smooth horizontal dragging on the floating capsule
(function() {
  const bar = document.querySelector('.top-clean-bar');
  if (!bar) return;

  let isDown = false;
  let startX = 0;
  let scrollLeft = 0;

  bar.addEventListener('mousedown', (e) => {
    // Only drag if not clicking a button or input
    if (e.target.closest('button') || e.target.closest('input') || e.target.closest('.modern-select-trigger')) return;
    isDown = true;
    startX = e.pageX - bar.offsetLeft;
    scrollLeft = bar.scrollLeft;
  });

  window.addEventListener('mouseup', () => { isDown = false; });
  window.addEventListener('mouseleave', () => { isDown = false; });

  bar.addEventListener('mousemove', (e) => {
    if (!isDown) return;
    e.preventDefault();
    const x = e.pageX - bar.offsetLeft;
    const walk = (x - startX) * 1.5;
    bar.scrollLeft = scrollLeft - walk;
  });
})();
</script>
"""

html = html.replace('</body>', f'{drag_script}</body>')

with open('app/src/main/assets/editor.html', 'w', encoding='utf-8') as f:
    f.write(html)

print("SUCCESS: Updated editor.html with swipeable/scrollable floating capsule!")
