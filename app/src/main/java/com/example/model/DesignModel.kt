package com.example.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class ShapeType {
    FRAME, RECT, ELLIPSE, TRIANGLE, STAR, POLYGON, HEART, LINE, ARROW, TEXT, PATH
}

enum class StrokePosition {
    INSIDE, CENTER, OUTSIDE
}

enum class FillType {
    SOLID, GRADIENT, IMAGE
}

enum class CanvasMode {
    INFINITE, FIXED
}

data class CanvasConfig(
    val mode: CanvasMode = CanvasMode.INFINITE,
    val width: Int = 1080,
    val height: Int = 1080,
    val presetName: String = "1:1 Square (1080x1080)",
    val dpi: Int = 300,
    val backgroundColor: Long = 0xFF1E1E24
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("mode", mode.name)
        put("width", width)
        put("height", height)
        put("presetName", presetName)
        put("dpi", dpi)
        put("backgroundColor", backgroundColor)
    }

    companion object {
        fun fromJson(json: JSONObject?): CanvasConfig {
            if (json == null) return CanvasConfig()
            return CanvasConfig(
                mode = try { CanvasMode.valueOf(json.optString("mode", CanvasMode.INFINITE.name)) } catch (e: Exception) { CanvasMode.INFINITE },
                width = json.optInt("width", 1080),
                height = json.optInt("height", 1080),
                presetName = json.optString("presetName", "1:1 Square"),
                dpi = json.optInt("dpi", 300),
                backgroundColor = json.optLong("backgroundColor", 0xFF1E1E24)
            )
        }
    }
}

data class GradientStop(
    val color: Long,
    val position: Float // 0.0f to 1.0f
)

data class EffectSettings(
    val dropShadowEnabled: Boolean = false,
    val dropShadowColor: Long = 0x66000000,
    val dropShadowRadius: Float = 12f,
    val dropShadowDx: Float = 0f,
    val dropShadowDy: Float = 6f,
    val layerBlurEnabled: Boolean = false,
    val blurRadius: Float = 8f,
    val glassEnabled: Boolean = false,
    val glassOpacity: Float = 0.25f
)

data class DesignShape(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Element",
    val type: ShapeType = ShapeType.RECT,
    val x: Float = 100f,
    val y: Float = 100f,
    val width: Float = 150f,
    val height: Float = 100f,
    val rotation: Float = 0f,
    val cornerRadius: Float = 8f,
    val fillType: FillType = FillType.SOLID,
    val fillColor: Long = 0xFF7038F4,
    val gradientAngle: Float = 90f,
    val gradientStops: List<GradientStop> = listOf(
        GradientStop(0xFF7038F4, 0f),
        GradientStop(0xFF0D99FF, 1f)
    ),
    val strokeColor: Long = 0xFF000000,
    val strokeWidth: Float = 0f,
    val strokePosition: StrokePosition = StrokePosition.INSIDE,
    val opacity: Float = 1f,
    val aspectLocked: Boolean = false,
    val isLocked: Boolean = false,
    // Typography properties for TEXT
    val text: String = "Hello Uinify",
    val fontSize: Float = 22f,
    val fontWeight: Int = 400, // 400 = Normal, 700 = Bold
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val textAlign: String = "CENTER",
    // Vector Path Points
    val pathPoints: List<Pair<Float, Float>> = emptyList(),
    val effects: EffectSettings = EffectSettings()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("type", type.name)
        put("x", x.toDouble())
        put("y", y.toDouble())
        put("width", width.toDouble())
        put("height", height.toDouble())
        put("rotation", rotation.toDouble())
        put("cornerRadius", cornerRadius.toDouble())
        put("fillType", fillType.name)
        put("fillColor", fillColor)
        put("gradientAngle", gradientAngle.toDouble())
        val stopsArray = JSONArray()
        gradientStops.forEach {
            stopsArray.put(JSONObject().apply {
                put("color", it.color)
                put("position", it.position.toDouble())
            })
        }
        put("gradientStops", stopsArray)
        put("strokeColor", strokeColor)
        put("strokeWidth", strokeWidth.toDouble())
        put("strokePosition", strokePosition.name)
        put("opacity", opacity.toDouble())
        put("aspectLocked", aspectLocked)
        put("isLocked", isLocked)
        put("text", text)
        put("fontSize", fontSize.toDouble())
        put("fontWeight", fontWeight)
        put("isItalic", isItalic)
        put("isUnderline", isUnderline)
        put("textAlign", textAlign)
        val pathArr = JSONArray()
        pathPoints.forEach {
            val pt = JSONObject()
            pt.put("x", it.first.toDouble())
            pt.put("y", it.second.toDouble())
            pathArr.put(pt)
        }
        put("pathPoints", pathArr)
    }

    companion object {
        fun fromJson(json: JSONObject): DesignShape {
            val typeStr = json.optString("type", ShapeType.RECT.name)
            val shapeType = try { ShapeType.valueOf(typeStr) } catch (e: Exception) { ShapeType.RECT }
            val strokePosStr = json.optString("strokePosition", StrokePosition.INSIDE.name)
            val strokePos = try { StrokePosition.valueOf(strokePosStr) } catch (e: Exception) { StrokePosition.INSIDE }
            val fillTypeStr = json.optString("fillType", FillType.SOLID.name)
            val fillType = try { FillType.valueOf(fillTypeStr) } catch (e: Exception) { FillType.SOLID }

            val stops = mutableListOf<GradientStop>()
            val stopsArr = json.optJSONArray("gradientStops")
            if (stopsArr != null) {
                for (i in 0 until stopsArr.length()) {
                    val stopObj = stopsArr.getJSONObject(i)
                    stops.add(
                        GradientStop(
                            color = stopObj.optLong("color", 0xFF7038F4),
                            position = stopObj.optDouble("position", 0.0).toFloat()
                        )
                    )
                }
            } else {
                stops.add(GradientStop(0xFF7038F4, 0f))
                stops.add(GradientStop(0xFF0D99FF, 1f))
            }

            val pts = mutableListOf<Pair<Float, Float>>()
            val ptsArr = json.optJSONArray("pathPoints")
            if (ptsArr != null) {
                for (i in 0 until ptsArr.length()) {
                    val ptObj = ptsArr.getJSONObject(i)
                    pts.add(Pair(ptObj.optDouble("x", 0.0).toFloat(), ptObj.optDouble("y", 0.0).toFloat()))
                }
            }

            return DesignShape(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", "Shape"),
                type = shapeType,
                x = json.optDouble("x", 100.0).toFloat(),
                y = json.optDouble("y", 100.0).toFloat(),
                width = json.optDouble("width", 150.0).toFloat(),
                height = json.optDouble("height", 100.0).toFloat(),
                rotation = json.optDouble("rotation", 0.0).toFloat(),
                cornerRadius = json.optDouble("cornerRadius", 8.0).toFloat(),
                fillType = fillType,
                fillColor = json.optLong("fillColor", 0xFF7038F4),
                gradientAngle = json.optDouble("gradientAngle", 90.0).toFloat(),
                gradientStops = stops,
                strokeColor = json.optLong("strokeColor", 0xFF000000),
                strokeWidth = json.optDouble("strokeWidth", 0.0).toFloat(),
                strokePosition = strokePos,
                opacity = json.optDouble("opacity", 1.0).toFloat(),
                aspectLocked = json.optBoolean("aspectLocked", false),
                isLocked = json.optBoolean("isLocked", false),
                text = json.optString("text", "Text"),
                fontSize = json.optDouble("fontSize", 20.0).toFloat(),
                fontWeight = json.optInt("fontWeight", 400),
                isItalic = json.optBoolean("isItalic", false),
                isUnderline = json.optBoolean("isUnderline", false),
                textAlign = json.optString("textAlign", "CENTER"),
                pathPoints = pts
            )
        }
    }
}

data class UinifyProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Studio Project",
    val canvasConfig: CanvasConfig = CanvasConfig(),
    val shapes: List<DesignShape> = emptyList(),
    val accentColor: Long = 0xFF0D99FF, // User custom accent color
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("uinify_format", "1.0")
        put("id", id)
        put("title", title)
        put("accentColor", accentColor)
        put("lastUpdated", lastUpdated)
        put("canvasConfig", canvasConfig.toJson())
        val shapesArray = JSONArray()
        shapes.forEach { shapesArray.put(it.toJson()) }
        put("shapes", shapesArray)
    }

    companion object {
        fun fromJson(jsonStr: String): UinifyProject {
            val json = JSONObject(jsonStr)
            val shapesList = mutableListOf<DesignShape>()
            val shapesArr = json.optJSONArray("shapes")
            if (shapesArr != null) {
                for (i in 0 until shapesArr.length()) {
                    shapesList.add(DesignShape.fromJson(shapesArr.getJSONObject(i)))
                }
            }
            return UinifyProject(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", "Project"),
                accentColor = json.optLong("accentColor", 0xFF0D99FF),
                lastUpdated = json.optLong("lastUpdated", System.currentTimeMillis()),
                canvasConfig = CanvasConfig.fromJson(json.optJSONObject("canvasConfig")),
                shapes = shapesList
            )
        }
    }
}
