package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CanvasConfig
import com.example.model.CanvasMode
import com.example.model.DesignShape
import com.example.model.FillType
import com.example.model.ShapeType
import com.example.model.StrokePosition
import com.example.model.UinifyProject
import com.example.storage.ProjectStorageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class StudioUiState(
    val project: UinifyProject = UinifyProject(),
    val selectedShapeId: String? = null,
    val selectedShapeIds: Set<String> = emptySet(),
    val activeTool: String = "select", // select, multiselect, frame, rect, ellipse, star, triangle, pen, text, hand
    val zoom: Float = 1.0f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val isAutoSaved: Boolean = true,
    val autoSaveMessage: String = "All changes saved",
    val showInspector: Boolean = true,
    val showLayers: Boolean = false,
    val showCanvasConfigDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val showColorPicker: Boolean = false,
    val colorPickerTarget: String = "fill", // "fill", "stroke", "accent"
    val accentColor: Long = 0xFF0D99FF,
    val toastMessage: String? = null,
    val exportedFile: File? = null,
    val exportedText: String? = null
)

class UinifyViewModel(application: Application) : AndroidViewModel(application) {

    private val storageManager = ProjectStorageManager(application)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<List<DesignShape>>()
    private val redoStack = mutableListOf<List<DesignShape>>()
    private var autoSaveJob: Job? = null

    init {
        loadInitialProject()
    }

    private fun loadInitialProject() {
        viewModelScope.launch {
            val project = storageManager.loadCurrentProject()
            _uiState.update { 
                it.copy(
                    project = project,
                    accentColor = project.accentColor,
                    selectedShapeId = project.shapes.firstOrNull()?.id
                )
            }
            saveHistory()
        }
    }

    private fun triggerAutoSave() {
        _uiState.update { it.copy(isAutoSaved = false, autoSaveMessage = "Saving changes...") }
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(150) // Micro debounce
            storageManager.saveCurrentProject(_uiState.value.project)
            _uiState.update { it.copy(isAutoSaved = true, autoSaveMessage = "Saved automatically") }
        }
    }

    private fun saveHistory() {
        undoStack.add(_uiState.value.project.shapes.map { it.copy() })
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.size > 1) {
            val current = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(current)
            val prev = undoStack.last()
            _uiState.update { state ->
                val updatedProj = state.project.copy(shapes = prev.map { it.copy() }, lastUpdated = System.currentTimeMillis())
                state.copy(project = updatedProj)
            }
            triggerAutoSave()
            showToast("Undo")
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(next)
            _uiState.update { state ->
                val updatedProj = state.project.copy(shapes = next.map { it.copy() }, lastUpdated = System.currentTimeMillis())
                state.copy(project = updatedProj)
            }
            triggerAutoSave()
            showToast("Redo")
        }
    }

    fun selectTool(tool: String) {
        _uiState.update { it.copy(activeTool = tool) }
    }

    fun selectShape(id: String?, isMulti: Boolean = false) {
        _uiState.update { state ->
            if (id == null) {
                state.copy(selectedShapeId = null, selectedShapeIds = emptySet())
            } else if (isMulti) {
                val newSet = if (state.selectedShapeIds.contains(id)) {
                    state.selectedShapeIds - id
                } else {
                    state.selectedShapeIds + id
                }
                state.copy(selectedShapeId = id, selectedShapeIds = newSet)
            } else {
                state.copy(selectedShapeId = id, selectedShapeIds = setOf(id))
            }
        }
    }

    fun addShape(shape: DesignShape) {
        saveHistory()
        _uiState.update { state ->
            val updated = state.project.shapes + shape
            val updatedProj = state.project.copy(shapes = updated, lastUpdated = System.currentTimeMillis())
            state.copy(project = updatedProj, selectedShapeId = shape.id, selectedShapeIds = setOf(shape.id))
        }
        triggerAutoSave()
        showToast("Added ${shape.name}")
    }

    fun updateSelectedShape(transform: (DesignShape) -> DesignShape) {
        val selId = _uiState.value.selectedShapeId ?: return
        _uiState.update { state ->
            val updatedShapes = state.project.shapes.map { s ->
                if (s.id == selId) transform(s) else s
            }
            val updatedProj = state.project.copy(shapes = updatedShapes, lastUpdated = System.currentTimeMillis())
            state.copy(project = updatedProj)
        }
        triggerAutoSave()
    }

    fun updateShapePosition(id: String, dx: Float, dy: Float) {
        _uiState.update { state ->
            val updated = state.project.shapes.map { s ->
                if (s.id == id && !s.isLocked) s.copy(x = s.x + dx, y = s.y + dy) else s
            }
            state.copy(project = state.project.copy(shapes = updated))
        }
        triggerAutoSave()
    }

    fun deleteSelectedShape() {
        val selId = _uiState.value.selectedShapeId ?: return
        saveHistory()
        _uiState.update { state ->
            val updated = state.project.shapes.filter { it.id != selId }
            state.copy(
                project = state.project.copy(shapes = updated, lastUpdated = System.currentTimeMillis()),
                selectedShapeId = null,
                selectedShapeIds = emptySet()
            )
        }
        triggerAutoSave()
        showToast("Deleted element")
    }

    fun duplicateSelectedShape() {
        val current = _uiState.value.project.shapes.find { it.id == _uiState.value.selectedShapeId } ?: return
        saveHistory()
        val copy = current.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = "${current.name} Copy",
            x = current.x + 20f,
            y = current.y + 20f
        )
        _uiState.update { state ->
            val updated = state.project.shapes + copy
            state.copy(
                project = state.project.copy(shapes = updated, lastUpdated = System.currentTimeMillis()),
                selectedShapeId = copy.id,
                selectedShapeIds = setOf(copy.id)
            )
        }
        triggerAutoSave()
        showToast("Duplicated ${copy.name}")
    }

    fun updateViewport(panDeltaX: Float, panDeltaY: Float, zoomDelta: Float) {
        _uiState.update { state ->
            val newZoom = (state.zoom * zoomDelta).coerceIn(0.2f, 5.0f)
            state.copy(
                panX = state.panX + panDeltaX,
                panY = state.panY + panDeltaY,
                zoom = newZoom
            )
        }
    }

    fun resetViewport() {
        _uiState.update { it.copy(panX = 0f, panY = 0f, zoom = 1.0f) }
        showToast("Centered canvas")
    }

    fun setCanvasConfig(config: CanvasConfig) {
        saveHistory()
        _uiState.update { state ->
            state.copy(project = state.project.copy(canvasConfig = config))
        }
        triggerAutoSave()
        showToast("Canvas mode updated: ${config.presetName}")
    }

    fun setAccentColor(color: Long) {
        _uiState.update { state ->
            state.copy(
                accentColor = color,
                project = state.project.copy(accentColor = color)
            )
        }
        triggerAutoSave()
        showToast("Accent theme color updated")
    }

    fun toggleInspector() {
        _uiState.update { it.copy(showInspector = !it.showInspector) }
    }

    fun toggleLayers() {
        _uiState.update { it.copy(showLayers = !it.showLayers) }
    }

    fun openCanvasConfigDialog(open: Boolean) {
        _uiState.update { it.copy(showCanvasConfigDialog = open) }
    }

    fun openExportDialog(open: Boolean) {
        _uiState.update { it.copy(showExportDialog = open) }
    }

    fun openImportDialog(open: Boolean) {
        _uiState.update { it.copy(showImportDialog = open) }
    }

    fun openColorPicker(open: Boolean, target: String = "fill") {
        _uiState.update { it.copy(showColorPicker = open, colorPickerTarget = target) }
    }

    fun applyColor(color: Long) {
        when (_uiState.value.colorPickerTarget) {
            "fill" -> updateSelectedShape { it.copy(fillColor = color, fillType = FillType.SOLID) }
            "stroke" -> updateSelectedShape { it.copy(strokeColor = color) }
            "accent" -> setAccentColor(color)
        }
    }

    fun importProjectContent(content: String) {
        viewModelScope.launch {
            try {
                val proj = storageManager.importProjectFromString(content)
                saveHistory()
                _uiState.update { state ->
                    state.copy(
                        project = proj,
                        selectedShapeId = proj.shapes.firstOrNull()?.id,
                        selectedShapeIds = emptySet()
                    )
                }
                triggerAutoSave()
                showToast("Successfully imported: ${proj.title}")
            } catch (e: Exception) {
                showToast("Failed to import file: ${e.message}")
            }
        }
    }

    fun exportToFormat(format: String, onReady: (File?, String?) -> Unit) {
        viewModelScope.launch {
            val proj = _uiState.value.project
            when (format.uppercase()) {
                "UIFY" -> {
                    val content = storageManager.exportProjectToUify(proj)
                    onReady(null, content)
                }
                "SVG" -> {
                    val svg = storageManager.exportProjectToSvg(proj)
                    onReady(null, svg)
                }
                "HTML" -> {
                    val html = storageManager.exportProjectToHtml(proj)
                    onReady(null, html)
                }
                "PNG" -> {
                    val file = storageManager.exportProjectToPng(proj)
                    onReady(file, null)
                }
                else -> {
                    val json = storageManager.exportProjectToUify(proj)
                    onReady(null, json)
                }
            }
        }
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
