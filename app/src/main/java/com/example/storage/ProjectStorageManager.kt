package com.example.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.example.model.CanvasConfig
import com.example.model.CanvasMode
import com.example.model.DesignShape
import com.example.model.EffectSettings
import com.example.model.FillType
import com.example.model.GradientStop
import com.example.model.ShapeType
import com.example.model.UinifyProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class ProjectStorageManager(private val context: Context) {

    private val projectsDir: File by lazy {
        File(context.filesDir, "uinify_projects").apply { if (!exists()) mkdirs() }
    }

    private val currentProjectFile: File by lazy {
        File(context.filesDir, "uinify_autosave_current.uify")
    }

    suspend fun saveCurrentProject(project: UinifyProject) = withContext(Dispatchers.IO) {
        val json = project.toJson().toString(2)
        currentProjectFile.writeText(json)
        // Also save to projectsDir under its id
        val projFile = File(projectsDir, "${project.id}.uify")
        projFile.writeText(json)
    }

    suspend fun loadCurrentProject(): UinifyProject = withContext(Dispatchers.IO) {
        if (currentProjectFile.exists()) {
            try {
                val content = currentProjectFile.readText()
                return@withContext UinifyProject.fromJson(content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Fallback default sample project
        return@withContext createDefaultProject()
    }

    suspend fun listSavedProjects(): List<UinifyProject> = withContext(Dispatchers.IO) {
        val list = mutableListOf<UinifyProject>()
        val files = projectsDir.listFiles { _, name -> name.endsWith(".uify") } ?: emptyArray()
        for (f in files) {
            try {
                list.add(UinifyProject.fromJson(f.readText()))
            } catch (e: Exception) {
                // Ignore corrupt
            }
        }
        if (list.isEmpty()) {
            val def = createDefaultProject()
            saveCurrentProject(def)
            list.add(def)
        }
        list.sortedByDescending { it.lastUpdated }
    }

    suspend fun exportProjectToUify(project: UinifyProject): String = withContext(Dispatchers.IO) {
        return@withContext project.toJson().toString(2)
    }

    suspend fun exportProjectToSvg(project: UinifyProject): String = withContext(Dispatchers.IO) {
        val width = if (project.canvasConfig.mode == CanvasMode.FIXED) project.canvasConfig.width else 1440
        val height = if (project.canvasConfig.mode == CanvasMode.FIXED) project.canvasConfig.height else 900

        val sb = StringBuilder()
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" width="$width" height="$height" viewBox="0 0 $width $height">""")
        sb.append("\n  <defs></defs>\n")
        
        // Background if fixed
        if (project.canvasConfig.mode == CanvasMode.FIXED) {
            val bgHex = String.format("#%06X", (0xFFFFFF and project.canvasConfig.backgroundColor.toInt()))
            sb.append("""  <rect width="$width" height="$height" fill="$bgHex" />""").append("\n")
        }

        for (s in project.shapes) {
            val fillHex = String.format("#%06X", (0xFFFFFF and s.fillColor.toInt()))
            val strokeHex = String.format("#%06X", (0xFFFFFF and s.strokeColor.toInt()))
            val strokeAttr = if (s.strokeWidth > 0) """stroke="$strokeHex" stroke-width="${s.strokeWidth}"""" else ""
            val opacityAttr = if (s.opacity < 1f) """opacity="${s.opacity}"""" else ""

            when (s.type) {
                ShapeType.RECT, ShapeType.FRAME -> {
                    sb.append("""  <rect x="${s.x}" y="${s.y}" width="${s.width}" height="${s.height}" rx="${s.cornerRadius}" fill="$fillHex" $strokeAttr $opacityAttr />""").append("\n")
                }
                ShapeType.ELLIPSE -> {
                    val cx = s.x + s.width / 2f
                    val cy = s.y + s.height / 2f
                    val rx = s.width / 2f
                    val ry = s.height / 2f
                    sb.append("""  <ellipse cx="$cx" cy="$cy" rx="$rx" ry="$ry" fill="$fillHex" $strokeAttr $opacityAttr />""").append("\n")
                }
                ShapeType.TEXT -> {
                    val weight = if (s.fontWeight >= 600) "bold" else "normal"
                    val style = if (s.isItalic) "italic" else "normal"
                    sb.append("""  <text x="${s.x}" y="${s.y + s.fontSize}" fill="$fillHex" font-size="${s.fontSize}" font-family="sans-serif" font-weight="$weight" font-style="$style" $opacityAttr>${escapeXml(s.text)}</text>""").append("\n")
                }
                ShapeType.LINE, ShapeType.ARROW -> {
                    sb.append("""  <line x1="${s.x}" y1="${s.y}" x2="${s.x + s.width}" y2="${s.y + s.height}" stroke="$strokeHex" stroke-width="${if (s.strokeWidth > 0) s.strokeWidth else 2f}" $opacityAttr />""").append("\n")
                }
                ShapeType.TRIANGLE -> {
                    val points = "${s.x + s.width / 2},${s.y} ${s.x},${s.y + s.height} ${s.x + s.width},${s.y + s.height}"
                    sb.append("""  <polygon points="$points" fill="$fillHex" $strokeAttr $opacityAttr />""").append("\n")
                }
                ShapeType.STAR -> {
                    // 5-point star
                    val cx = s.x + s.width / 2f
                    val cy = s.y + s.height / 2f
                    val rOuter = minOf(s.width, s.height) / 2f
                    val rInner = rOuter * 0.45f
                    val pts = mutableListOf<String>()
                    for (i in 0 until 10) {
                        val angle = Math.toRadians((i * 36.0) - 90.0)
                        val r = if (i % 2 == 0) rOuter else rInner
                        val px = cx + (r * Math.cos(angle)).toFloat()
                        val py = cy + (r * Math.sin(angle)).toFloat()
                        pts.add("$px,$py")
                    }
                    sb.append("""  <polygon points="${pts.joinToString(" ")}" fill="$fillHex" $strokeAttr $opacityAttr />""").append("\n")
                }
                else -> {
                    sb.append("""  <rect x="${s.x}" y="${s.y}" width="${s.width}" height="${s.height}" rx="${s.cornerRadius}" fill="$fillHex" $strokeAttr $opacityAttr />""").append("\n")
                }
            }
        }
        sb.append("</svg>")
        return@withContext sb.toString()
    }

    suspend fun exportProjectToHtml(project: UinifyProject): String = withContext(Dispatchers.IO) {
        val svgContent = exportProjectToSvg(project)
        return@withContext """
            <!DOCTYPE html>
            <html lang="id">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>${escapeXml(project.title)} - Exported by Uinify</title>
              <style>
                body { margin: 0; background: #0E0E12; display: flex; align-items: center; justify-content: center; min-height: 100vh; color: #fff; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
                .canvas-card { box-shadow: 0 20px 60px rgba(0,0,0,0.8); border: 1px solid rgba(255,255,255,0.12); border-radius: 16px; overflow: hidden; background: #16161A; }
              </style>
            </head>
            <body>
              <div class="canvas-card">
                $svgContent
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    suspend fun exportProjectToPng(project: UinifyProject): File = withContext(Dispatchers.IO) {
        val width = (if (project.canvasConfig.mode == CanvasMode.FIXED) project.canvasConfig.width else 1080).coerceIn(300, 4096)
        val height = (if (project.canvasConfig.mode == CanvasMode.FIXED) project.canvasConfig.height else 1080).coerceIn(300, 4096)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw background
        val bgPaint = Paint().apply {
            color = if (project.canvasConfig.mode == CanvasMode.FIXED) project.canvasConfig.backgroundColor.toInt() else 0xFF16161A.toInt()
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (s in project.shapes) {
            paint.color = s.fillColor.toInt()
            paint.alpha = (s.opacity * 255).toInt().coerceIn(0, 255)
            
            when (s.type) {
                ShapeType.RECT, ShapeType.FRAME -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(s.x, s.y, s.x + s.width, s.y + s.height, s.cornerRadius, s.cornerRadius, paint)
                }
                ShapeType.ELLIPSE -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawOval(s.x, s.y, s.x + s.width, s.y + s.height, paint)
                }
                ShapeType.TEXT -> {
                    paint.textSize = s.fontSize
                    paint.isFakeBoldText = s.fontWeight >= 600
                    canvas.drawText(s.text, s.x, s.y + s.fontSize, paint)
                }
                ShapeType.LINE, ShapeType.ARROW -> {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = if (s.strokeWidth > 0) s.strokeWidth else 3f
                    paint.color = s.strokeColor.toInt()
                    canvas.drawLine(s.x, s.y, s.x + s.width, s.y + s.height, paint)
                }
                else -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(s.x, s.y, s.x + s.width, s.y + s.height, s.cornerRadius, s.cornerRadius, paint)
                }
            }

            // Draw stroke if specified
            if (s.strokeWidth > 0 && s.type != ShapeType.LINE && s.type != ShapeType.ARROW) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = s.strokeWidth
                    color = s.strokeColor.toInt()
                }
                if (s.type == ShapeType.ELLIPSE) {
                    canvas.drawOval(s.x, s.y, s.x + s.width, s.y + s.height, strokePaint)
                } else {
                    canvas.drawRoundRect(s.x, s.y, s.x + s.width, s.y + s.height, s.cornerRadius, s.cornerRadius, strokePaint)
                }
            }
        }

        val exportFile = File(context.cacheDir, "uinify_export_${System.currentTimeMillis()}.png")
        FileOutputStream(exportFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return@withContext exportFile
    }

    suspend fun importProjectFromString(content: String): UinifyProject = withContext(Dispatchers.IO) {
        val trimmed = content.trim()
        if (trimmed.startsWith("{") && trimmed.contains("\"shapes\"")) {
            // Native .uify or json format
            return@withContext UinifyProject.fromJson(trimmed)
        } else if (trimmed.startsWith("<svg") || trimmed.contains("<svg")) {
            // Import basic SVG
            return@withContext parseSvgToProject(trimmed)
        } else if (trimmed.contains("<html") || trimmed.contains("<!DOCTYPE html>")) {
            // Import HTML
            return@withContext parseHtmlToProject(trimmed)
        } else {
            // Generic JSON
            try {
                return@withContext UinifyProject.fromJson(trimmed)
            } catch (e: Exception) {
                return@withContext createDefaultProject().copy(title = "Imported Document")
            }
        }
    }

    private fun parseSvgToProject(svg: String): UinifyProject {
        val shapes = mutableListOf<DesignShape>()
        // Simple regex parse for common elements
        val rectRegex = Regex("""<rect[^>]+x="([^"]+)"[^>]+y="([^"]+)"[^>]+width="([^"]+)"[^>]+height="([^"]+)"""")
        rectRegex.findAll(svg).forEachIndexed { i, m ->
            val x = m.groupValues[1].toFloatOrNull() ?: 100f
            val y = m.groupValues[2].toFloatOrNull() ?: 100f
            val w = m.groupValues[3].toFloatOrNull() ?: 200f
            val h = m.groupValues[4].toFloatOrNull() ?: 150f
            shapes.add(
                DesignShape(
                    name = "SVG Rect ${i + 1}",
                    type = ShapeType.RECT,
                    x = x, y = y, width = w, height = h,
                    fillColor = 0xFF7038F4
                )
            )
        }

        val textRegex = Regex("""<text[^>]+x="([^"]+)"[^>]+y="([^"]+)"[^>]*>([^<]+)</text>""")
        textRegex.findAll(svg).forEachIndexed { i, m ->
            val x = m.groupValues[1].toFloatOrNull() ?: 120f
            val y = m.groupValues[2].toFloatOrNull() ?: 120f
            val text = m.groupValues[3].trim()
            shapes.add(
                DesignShape(
                    name = "SVG Text ${i + 1}",
                    type = ShapeType.TEXT,
                    x = x, y = y, width = 200f, height = 40f,
                    text = text,
                    fillColor = 0xFFF8F9FA
                )
            )
        }

        if (shapes.isEmpty()) {
            shapes.add(
                DesignShape(
                    name = "SVG Artboard",
                    type = ShapeType.FRAME,
                    x = 80f, y = 80f, width = 360f, height = 640f,
                    fillColor = 0xFFFFFFFF
                )
            )
        }

        return UinifyProject(
            title = "Imported SVG Art",
            shapes = shapes
        )
    }

    private fun parseHtmlToProject(html: String): UinifyProject {
        val shapes = mutableListOf<DesignShape>()
        shapes.add(
            DesignShape(
                name = "HTML Root Frame",
                type = ShapeType.FRAME,
                x = 60f, y = 60f, width = 375f, height = 812f,
                fillColor = 0xFF16161A
            )
        )
        // Extract title or headers if available
        val hRegex = Regex("""<(h[1-6]|p|title)[^>]*>([^<]+)</\1>""")
        var offsetY = 120f
        hRegex.findAll(html).take(6).forEachIndexed { index, matchResult ->
            val t = matchResult.groupValues[2].trim()
            if (t.isNotEmpty()) {
                shapes.add(
                    DesignShape(
                        name = "Text ($index)",
                        type = ShapeType.TEXT,
                        x = 90f, y = offsetY, width = 300f, height = 36f,
                        text = t,
                        fontSize = if (index == 0) 24f else 16f,
                        fontWeight = if (index == 0) 700 else 400,
                        fillColor = 0xFFFFFFFF
                    )
                )
                offsetY += 50f
            }
        }
        return UinifyProject(
            title = "Imported HTML Project",
            shapes = shapes
        )
    }

    private fun createDefaultProject(): UinifyProject {
        return UinifyProject(
            title = "Design Systems Mobile UI",
            canvasConfig = CanvasConfig(
                mode = CanvasMode.INFINITE,
                presetName = "Infinite Canvas"
            ),
            accentColor = 0xFF0D99FF,
            shapes = listOf(
                DesignShape(
                    name = "Frame - iPhone 16 Pro",
                    type = ShapeType.FRAME,
                    x = 80f, y = 80f, width = 375f, height = 812f,
                    fillColor = 0xFF121216,
                    cornerRadius = 24f,
                    strokeColor = 0x33FFFFFF,
                    strokeWidth = 1f
                ),
                DesignShape(
                    name = "Hero Card Gradient",
                    type = ShapeType.RECT,
                    x = 105f, y = 140f, width = 325f, height = 180f,
                    fillType = FillType.GRADIENT,
                    fillColor = 0xFF7038F4,
                    gradientStops = listOf(
                        GradientStop(0xFF7038F4, 0f),
                        GradientStop(0xFF0D99FF, 1f)
                    ),
                    cornerRadius = 20f,
                    effects = EffectSettings(
                        dropShadowEnabled = true,
                        dropShadowColor = 0x66000000,
                        dropShadowRadius = 24f,
                        dropShadowDy = 10f
                    )
                ),
                DesignShape(
                    name = "Headline Text",
                    type = ShapeType.TEXT,
                    x = 130f, y = 180f, width = 280f, height = 40f,
                    text = "Uinify Studio Native",
                    fontSize = 24f,
                    fontWeight = 700,
                    fillColor = 0xFFFFFFFF
                ),
                DesignShape(
                    name = "Subtitle Text",
                    type = ShapeType.TEXT,
                    x = 130f, y = 230f, width = 280f, height = 30f,
                    text = "Precision mobile vector canvas",
                    fontSize = 14f,
                    fontWeight = 400,
                    fillColor = 0xCCFFFFFF
                ),
                DesignShape(
                    name = "Teal Accent Pill",
                    type = ShapeType.ELLIPSE,
                    x = 105f, y = 350f, width = 100f, height = 40f,
                    fillColor = 0xFF00D1B2,
                    cornerRadius = 20f
                ),
                DesignShape(
                    name = "Star Icon Shape",
                    type = ShapeType.STAR,
                    x = 240f, y = 350f, width = 44f, height = 44f,
                    fillColor = 0xFFFFD166
                )
            )
        )
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
