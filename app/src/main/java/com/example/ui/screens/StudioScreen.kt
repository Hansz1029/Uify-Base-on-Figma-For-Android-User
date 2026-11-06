package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.model.DesignShape
import com.example.model.ShapeType
import com.example.ui.components.BottomToolDock
import com.example.ui.components.CanvasConfigDialog
import com.example.ui.components.ColorPickerDialog
import com.example.ui.components.ExportDialog
import com.example.ui.components.FloatingInspector
import com.example.ui.components.ImportDialog
import com.example.ui.components.LayersDrawer
import com.example.ui.components.StudioCanvas
import com.example.ui.components.TopCleanBar
import com.example.ui.theme.BgCanvasDark
import com.example.viewmodel.UinifyViewModel

@Composable
fun StudioScreen(
    viewModel: UinifyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvasDark)
    ) {
        // 1. Interactive Infinite/Fixed Vector Canvas
        StudioCanvas(
            shapes = uiState.project.shapes,
            selectedId = uiState.selectedShapeId,
            selectedIds = uiState.selectedShapeIds,
            canvasConfig = uiState.project.canvasConfig,
            zoom = uiState.zoom,
            panX = uiState.panX,
            panY = uiState.panY,
            activeTool = uiState.activeTool,
            accentColor = uiState.accentColor,
            onTransform = { dx, dy, zoomFactor ->
                viewModel.updateViewport(dx, dy, zoomFactor)
            },
            onSelectShape = { id, isMulti ->
                viewModel.selectShape(id, isMulti)
            },
            onShapeMove = { id, dx, dy ->
                viewModel.updateShapePosition(id, dx, dy)
            },
            onCanvasTap = { offset ->
                val type = when (uiState.activeTool) {
                    "rect" -> ShapeType.RECT
                    "ellipse" -> ShapeType.ELLIPSE
                    "star" -> ShapeType.STAR
                    "triangle" -> ShapeType.TRIANGLE
                    "text" -> ShapeType.TEXT
                    "frame" -> ShapeType.FRAME
                    else -> ShapeType.RECT
                }
                viewModel.addShape(
                    DesignShape(
                        name = "${type.name.lowercase().replaceFirstChar { it.uppercase() }} ${uiState.project.shapes.size + 1}",
                        type = type,
                        x = offset.x - 75f,
                        y = offset.y - 50f,
                        width = if (type == ShapeType.FRAME) 360f else 150f,
                        height = if (type == ShapeType.FRAME) 640f else 100f,
                        fillColor = if (type == ShapeType.FRAME) 0xFF1E1E24 else uiState.accentColor
                    )
                )
            }
        )

        // 2. Top Bar
        TopCleanBar(
            projectTitle = uiState.project.title,
            autoSaveStatus = uiState.autoSaveMessage,
            accentColor = uiState.accentColor,
            zoomPercent = (uiState.zoom * 100).toInt(),
            onResetZoom = { viewModel.resetViewport() },
            onOpenCanvasConfig = { viewModel.openCanvasConfigDialog(true) },
            onOpenColorTheme = { viewModel.openColorPicker(true, "accent") },
            onToggleInspector = { viewModel.toggleInspector() },
            onToggleLayers = { viewModel.toggleLayers() },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onExportClick = { viewModel.openExportDialog(true) },
            onImportClick = { viewModel.openImportDialog(true) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // 3. Floating Layers Drawer (Left)
        AnimatedVisibility(
            visible = uiState.showLayers,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 64.dp, start = 12.dp)
        ) {
            LayersDrawer(
                shapes = uiState.project.shapes,
                selectedId = uiState.selectedShapeId,
                accentColor = uiState.accentColor,
                onSelectShape = { id -> viewModel.selectShape(id) },
                onToggleLock = { id ->
                    viewModel.updateSelectedShape { s ->
                        if (s.id == id) s.copy(isLocked = !s.isLocked) else s
                    }
                },
                onClose = { viewModel.toggleLayers() }
            )
        }

        // 4. Floating Design Inspector (Right)
        AnimatedVisibility(
            visible = uiState.showInspector && uiState.selectedShapeId != null,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 64.dp, end = 12.dp)
        ) {
            val selected = uiState.project.shapes.find { it.id == uiState.selectedShapeId }
            FloatingInspector(
                selectedShape = selected,
                accentColor = uiState.accentColor,
                onUpdateShape = { transform -> viewModel.updateSelectedShape(transform) },
                onOpenColorPicker = { target -> viewModel.openColorPicker(true, target) },
                onClose = { viewModel.toggleInspector() }
            )
        }

        // 5. Bottom Floating Tools Dock
        BottomToolDock(
            activeTool = uiState.activeTool,
            accentColor = uiState.accentColor,
            onSelectTool = { tool -> viewModel.selectTool(tool) },
            onAddShapePreset = { type, name ->
                val centerCanvasX = (-uiState.panX + 400f) / uiState.zoom
                val centerCanvasY = (-uiState.panY + 700f) / uiState.zoom
                viewModel.addShape(
                    DesignShape(
                        name = "$name ${uiState.project.shapes.size + 1}",
                        type = type,
                        x = centerCanvasX,
                        y = centerCanvasY,
                        width = if (type == ShapeType.FRAME) 375f else 150f,
                        height = if (type == ShapeType.FRAME) 812f else 100f,
                        fillColor = if (type == ShapeType.FRAME) 0xFF16161A else uiState.accentColor
                    )
                )
            },
            onDeleteSelected = { viewModel.deleteSelectedShape() },
            onDuplicateSelected = { viewModel.duplicateSelectedShape() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )

        // Snackbar Notification Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp)
        )
    }

    // Dialogs
    if (uiState.showCanvasConfigDialog) {
        CanvasConfigDialog(
            currentConfig = uiState.project.canvasConfig,
            accentColor = uiState.accentColor,
            onApply = { config ->
                viewModel.setCanvasConfig(config)
                viewModel.openCanvasConfigDialog(false)
            },
            onDismiss = { viewModel.openCanvasConfigDialog(false) }
        )
    }

    if (uiState.showColorPicker) {
        ColorPickerDialog(
            title = when (uiState.colorPickerTarget) {
                "accent" -> "Pilih Warna Aksen Tema Studio"
                "stroke" -> "Pilih Warna Stroke"
                else -> "Pilih Warna Fill Objek"
            },
            accentColor = uiState.accentColor,
            onSelectColor = { c -> viewModel.applyColor(c) },
            onDismiss = { viewModel.openColorPicker(false) }
        )
    }

    if (uiState.showExportDialog) {
        ExportDialog(
            accentColor = uiState.accentColor,
            onExportFormat = { format ->
                viewModel.exportToFormat(format) { file, text ->
                    if (file != null) {
                        try {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share exported design"))
                        } catch (e: Exception) {
                            // Fallback generic send
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Exported PNG ready at: ${file.absolutePath}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share"))
                        }
                    } else if (text != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export $format"))
                    }
                }
            },
            onDismiss = { viewModel.openExportDialog(false) }
        )
    }

    if (uiState.showImportDialog) {
        ImportDialog(
            accentColor = uiState.accentColor,
            onImportTextContent = { content ->
                viewModel.importProjectContent(content)
            },
            onDismiss = { viewModel.openImportDialog(false) }
        )
    }
}
