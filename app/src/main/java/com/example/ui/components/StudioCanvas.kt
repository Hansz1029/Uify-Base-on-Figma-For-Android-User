package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.CanvasConfig
import com.example.model.CanvasMode
import com.example.model.DesignShape
import com.example.model.FillType
import com.example.model.ShapeType
import com.example.ui.theme.BgCanvasDark
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun StudioCanvas(
    shapes: List<DesignShape>,
    selectedId: String?,
    selectedIds: Set<String>,
    canvasConfig: CanvasConfig,
    zoom: Float,
    panX: Float,
    panY: Float,
    activeTool: String,
    accentColor: Long,
    onTransform: (Float, Float, Float) -> Unit,
    onSelectShape: (String?, Boolean) -> Unit,
    onShapeMove: (String, Float, Float) -> Unit,
    onCanvasTap: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvasDark)
            .testTag("studio_canvas_viewport")
            .pointerInput(activeTool) {
                if (activeTool == "hand") {
                    detectTransformGestures { _, pan, zoomFactor, _ ->
                        onTransform(pan.x, pan.y, zoomFactor)
                    }
                } else {
                    detectTransformGestures { _, pan, zoomFactor, _ ->
                        if (zoomFactor != 1.0f) {
                            onTransform(0f, 0f, zoomFactor)
                        } else {
                            onTransform(pan.x, pan.y, 1f)
                        }
                    }
                }
            }
            .pointerInput(activeTool) {
                detectTapGestures(
                    onTap = { screenOffset ->
                        val canvasX = (screenOffset.x - panX) / zoom
                        val canvasY = (screenOffset.y - panY) / zoom
                        
                        // Check hit test on shapes in reverse order (top to bottom)
                        val hit = shapes.reversed().firstOrNull { s ->
                            canvasX >= s.x && canvasX <= s.x + s.width &&
                            canvasY >= s.y && canvasY <= s.y + s.height
                        }
                        if (hit != null) {
                            onSelectShape(hit.id, activeTool == "multiselect")
                        } else {
                            if (activeTool in listOf("rect", "ellipse", "star", "triangle", "text", "frame", "line")) {
                                onCanvasTap(Offset(canvasX, canvasY))
                            } else {
                                onSelectShape(null, false)
                            }
                        }
                    }
                )
            }
    ) {
        // Render Canvas Native Drawings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 24.dp.toPx() * zoom
            val startGridX = panX % gridSpacing
            val startGridY = panY % gridSpacing

            // Draw subtle dot grid
            var gx = startGridX
            while (gx < size.width) {
                var gy = startGridY
                while (gy < size.height) {
                    drawCircle(
                        color = Color(0x22FFFFFF),
                        radius = 1.2f,
                        center = Offset(gx, gy)
                    )
                    gy += gridSpacing
                }
                gx += gridSpacing
            }

            // If Fixed Canvas Mode (e.g. 1:1, 16:9, etc), render the fixed artboard boundary
            if (canvasConfig.mode == CanvasMode.FIXED) {
                val artboardLeft = panX
                val artboardTop = panY
                val artboardWidth = canvasConfig.width * zoom
                val artboardHeight = canvasConfig.height * zoom

                // Artboard background
                drawRect(
                    color = Color(canvasConfig.backgroundColor),
                    topLeft = Offset(artboardLeft, artboardTop),
                    size = Size(artboardWidth, artboardHeight)
                )

                // Artboard outline
                drawRect(
                    color = Color(0x44FFFFFF),
                    topLeft = Offset(artboardLeft, artboardTop),
                    size = Size(artboardWidth, artboardHeight),
                    style = Stroke(width = 1.5f)
                )
            }

            // Render all shapes in normal order
            shapes.forEach { shape ->
                val shapeScreenX = shape.x * zoom + panX
                val shapeScreenY = shape.y * zoom + panY
                val shapeScreenW = shape.width * zoom
                val shapeScreenH = shape.height * zoom

                rotate(
                    degrees = shape.rotation,
                    pivot = Offset(shapeScreenX + shapeScreenW / 2f, shapeScreenY + shapeScreenH / 2f)
                ) {
                    drawDesignShape(
                        shape = shape,
                        screenX = shapeScreenX,
                        screenY = shapeScreenY,
                        screenW = shapeScreenW,
                        screenH = shapeScreenH,
                        zoom = zoom
                    )
                }
            }
        }

        // Draggable Selection Bounding Box Overlay
        val selectedShape = shapes.find { it.id == selectedId }
        if (selectedShape != null) {
            val boxLeft = (selectedShape.x * zoom + panX).dp
            val boxTop = (selectedShape.y * zoom + panY).dp
            val boxWidth = (selectedShape.width * zoom).dp
            val boxHeight = (selectedShape.height * zoom).dp

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (selectedShape.x * zoom + panX).roundToInt(),
                            (selectedShape.y * zoom + panY).roundToInt()
                        )
                    }
                    .size(boxWidth, boxHeight)
                    .border(1.5.dp, Color(accentColor), RoundedCornerShape(2.dp))
                    .pointerInput(selectedShape.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dx = dragAmount.x / zoom
                            val dy = dragAmount.y / zoom
                            onShapeMove(selectedShape.id, dx, dy)
                        }
                    }
            ) {
                // Corner resize handle visual indicators
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, RoundedCornerShape(1.dp))
                        .border(1.dp, Color(accentColor))
                )
            }
        }
    }
}

private fun DrawScope.drawDesignShape(
    shape: DesignShape,
    screenX: Float,
    screenY: Float,
    screenW: Float,
    screenH: Float,
    zoom: Float
) {
    val brush = if (shape.fillType == FillType.GRADIENT && shape.gradientStops.isNotEmpty()) {
        val colors = shape.gradientStops.map { Color(it.color) }
        val rad = Math.toRadians(shape.gradientAngle.toDouble())
        val x2 = screenX + (screenW * cos(rad)).toFloat()
        val y2 = screenY + (screenH * sin(rad)).toFloat()
        Brush.linearGradient(
            colors = colors,
            start = Offset(screenX, screenY),
            end = Offset(x2, y2)
        )
    } else {
        Brush.linearGradient(listOf(Color(shape.fillColor), Color(shape.fillColor)))
    }

    when (shape.type) {
        ShapeType.FRAME -> {
            drawRoundRect(
                brush = brush,
                topLeft = Offset(screenX, screenY),
                size = Size(screenW, screenH),
                cornerRadius = CornerRadius(shape.cornerRadius * zoom),
                alpha = shape.opacity
            )
            // Border if stroke enabled
            if (shape.strokeWidth > 0f) {
                drawRoundRect(
                    color = Color(shape.strokeColor),
                    topLeft = Offset(screenX, screenY),
                    size = Size(screenW, screenH),
                    cornerRadius = CornerRadius(shape.cornerRadius * zoom),
                    style = Stroke(width = shape.strokeWidth * zoom)
                )
            }
        }
        ShapeType.RECT -> {
            drawRoundRect(
                brush = brush,
                topLeft = Offset(screenX, screenY),
                size = Size(screenW, screenH),
                cornerRadius = CornerRadius(shape.cornerRadius * zoom),
                alpha = shape.opacity
            )
            if (shape.strokeWidth > 0f) {
                drawRoundRect(
                    color = Color(shape.strokeColor),
                    topLeft = Offset(screenX, screenY),
                    size = Size(screenW, screenH),
                    cornerRadius = CornerRadius(shape.cornerRadius * zoom),
                    style = Stroke(width = shape.strokeWidth * zoom)
                )
            }
        }
        ShapeType.ELLIPSE -> {
            drawOval(
                brush = brush,
                topLeft = Offset(screenX, screenY),
                size = Size(screenW, screenH),
                alpha = shape.opacity
            )
            if (shape.strokeWidth > 0f) {
                drawOval(
                    color = Color(shape.strokeColor),
                    topLeft = Offset(screenX, screenY),
                    size = Size(screenW, screenH),
                    style = Stroke(width = shape.strokeWidth * zoom)
                )
            }
        }
        ShapeType.TRIANGLE -> {
            val path = Path().apply {
                moveTo(screenX + screenW / 2f, screenY)
                lineTo(screenX + screenW, screenY + screenH)
                lineTo(screenX, screenY + screenH)
                close()
            }
            drawPath(path, brush = brush, alpha = shape.opacity)
            if (shape.strokeWidth > 0f) {
                drawPath(path, color = Color(shape.strokeColor), style = Stroke(width = shape.strokeWidth * zoom))
            }
        }
        ShapeType.STAR -> {
            val path = Path()
            val cx = screenX + screenW / 2f
            val cy = screenY + screenH / 2f
            val rOuter = min(screenW, screenH) / 2f
            val rInner = rOuter * 0.45f
            for (i in 0 until 10) {
                val angle = Math.toRadians((i * 36.0) - 90.0)
                val r = if (i % 2 == 0) rOuter else rInner
                val px = cx + (r * cos(angle)).toFloat()
                val py = cy + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path, brush = brush, alpha = shape.opacity)
            if (shape.strokeWidth > 0f) {
                drawPath(path, color = Color(shape.strokeColor), style = Stroke(width = shape.strokeWidth * zoom))
            }
        }
        ShapeType.LINE, ShapeType.ARROW -> {
            drawLine(
                color = Color(shape.strokeColor),
                start = Offset(screenX, screenY),
                end = Offset(screenX + screenW, screenY + screenH),
                strokeWidth = (if (shape.strokeWidth > 0) shape.strokeWidth else 2f) * zoom
            )
        }
        ShapeType.TEXT -> {
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = shape.fillColor.toInt()
                    textSize = shape.fontSize * zoom
                    isFakeBoldText = shape.fontWeight >= 600
                    alpha = (shape.opacity * 255).toInt().coerceIn(0, 255)
                    isAntiAlias = true
                }
                drawText(shape.text, screenX, screenY + shape.fontSize * zoom, paint)
            }
        }
        else -> {
            drawRoundRect(
                brush = brush,
                topLeft = Offset(screenX, screenY),
                size = Size(screenW, screenH),
                cornerRadius = CornerRadius(shape.cornerRadius * zoom),
                alpha = shape.opacity
            )
        }
    }
}
