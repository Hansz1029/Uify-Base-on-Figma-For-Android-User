package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DesignShape
import com.example.model.ShapeType
import com.example.ui.theme.BgPanelDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BottomToolDock(
    activeTool: String,
    accentColor: Long,
    onSelectTool: (String) -> Unit,
    onAddShapePreset: (ShapeType, String) -> Unit,
    onDeleteSelected: () -> Unit,
    onDuplicateSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showShapesPopup by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Shapes Popup Menu
        AnimatedVisibility(
            visible = showShapesPopup,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgPanelDark)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShapePopupItem(Icons.Default.CropSquare, "Rectangle") {
                        onAddShapePreset(ShapeType.RECT, "Rectangle")
                        showShapesPopup = false
                    }
                    ShapePopupItem(Icons.Default.RadioButtonUnchecked, "Circle") {
                        onAddShapePreset(ShapeType.ELLIPSE, "Circle")
                        showShapesPopup = false
                    }
                    ShapePopupItem(Icons.Default.ChangeHistory, "Triangle") {
                        onAddShapePreset(ShapeType.TRIANGLE, "Triangle")
                        showShapesPopup = false
                    }
                    ShapePopupItem(Icons.Default.StarOutline, "Star") {
                        onAddShapePreset(ShapeType.STAR, "Star")
                        showShapesPopup = false
                    }
                    ShapePopupItem(Icons.Default.ViewQuilt, "Frame") {
                        onAddShapePreset(ShapeType.FRAME, "Frame")
                        showShapesPopup = false
                    }
                }
            }
        }

        // Main Bottom Floating Dock
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(BgPanelDark)
                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("bottom_tool_dock")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Select / Move
                DockButton(
                    icon = Icons.Default.NearMe,
                    label = "Move",
                    isSelected = activeTool == "select",
                    accentColor = accentColor,
                    onClick = {
                        showShapesPopup = false
                        onSelectTool("select")
                    }
                )

                // Frame
                DockButton(
                    icon = Icons.Default.ViewQuilt,
                    label = "Frame",
                    isSelected = activeTool == "frame",
                    accentColor = accentColor,
                    onClick = {
                        showShapesPopup = false
                        onSelectTool("frame")
                        onAddShapePreset(ShapeType.FRAME, "Frame")
                    }
                )

                // Shapes Dropdown Trigger
                DockButton(
                    icon = Icons.Default.CropSquare,
                    label = "Shapes",
                    isSelected = showShapesPopup || activeTool in listOf("rect", "ellipse", "star", "triangle"),
                    accentColor = accentColor,
                    onClick = {
                        showShapesPopup = !showShapesPopup
                    }
                )

                // Text
                DockButton(
                    icon = Icons.Default.TextFields,
                    label = "Text",
                    isSelected = activeTool == "text",
                    accentColor = accentColor,
                    onClick = {
                        showShapesPopup = false
                        onSelectTool("text")
                        onAddShapePreset(ShapeType.TEXT, "Heading")
                    }
                )

                // Hand Pan Tool
                DockButton(
                    icon = Icons.Default.PanTool,
                    label = "Hand",
                    isSelected = activeTool == "hand",
                    accentColor = accentColor,
                    onClick = {
                        showShapesPopup = false
                        onSelectTool("hand")
                    }
                )

                // Duplicate
                IconButton(
                    onClick = onDuplicateSelected,
                    modifier = Modifier.size(36.dp).testTag("btn_duplicate")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileCopy,
                        contentDescription = "Duplicate",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete
                IconButton(
                    onClick = onDeleteSelected,
                    modifier = Modifier.size(36.dp).testTag("btn_delete")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DockButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Long,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(accentColor) else Color.Transparent)
            .clickable { onClick() }
            .testTag("dock_btn_$label"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ShapePopupItem(
    icon: ImageVector,
    name: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22FFFFFF))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}
