package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DesignShape
import com.example.model.FillType
import com.example.model.ShapeType
import com.example.model.StrokePosition
import com.example.ui.theme.BgInputDark
import com.example.ui.theme.BgPanelDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FloatingInspector(
    selectedShape: DesignShape?,
    accentColor: Long,
    onUpdateShape: ((DesignShape) -> DesignShape) -> Unit,
    onOpenColorPicker: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedShape == null) {
        Box(
            modifier = modifier
                .width(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BgPanelDark)
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select a shape to inspect properties",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
        return
    }

    Box(
        modifier = modifier
            .width(290.dp)
            .fillMaxHeight(0.75f)
            .clip(RoundedCornerShape(16.dp))
            .background(BgPanelDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("floating_inspector")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedShape.name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Inspector",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Divider(color = BorderSubtle)

            // Section 1: Dimensions (W, H, X, Y, Radius, Rotation)
            Text("DIMENSIONS & POSITION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NumberPropertyField("W", selectedShape.width.toInt()) {
                    onUpdateShape { s -> s.copy(width = it.toFloat().coerceAtLeast(10f)) }
                }
                NumberPropertyField("H", selectedShape.height.toInt()) {
                    onUpdateShape { s -> s.copy(height = it.toFloat().coerceAtLeast(10f)) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NumberPropertyField("X", selectedShape.x.toInt()) {
                    onUpdateShape { s -> s.copy(x = it.toFloat()) }
                }
                NumberPropertyField("Y", selectedShape.y.toInt()) {
                    onUpdateShape { s -> s.copy(y = it.toFloat()) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NumberPropertyField("Radius", selectedShape.cornerRadius.toInt()) {
                    onUpdateShape { s -> s.copy(cornerRadius = it.toFloat().coerceAtLeast(0f)) }
                }
                NumberPropertyField("Rotate", selectedShape.rotation.toInt()) {
                    onUpdateShape { s -> s.copy(rotation = it.toFloat()) }
                }
            }

            // Section 2: Fill Color & Type
            Divider(color = BorderSubtle)
            Text("FILL", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(selectedShape.fillColor))
                            .border(1.dp, Color.White.copy(alpha = 0.3f))
                            .clickable { onOpenColorPicker("fill") }
                    )
                    Text(
                        text = String.format("#%06X", (0xFFFFFF and selectedShape.fillColor.toInt())),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Fill Type Toggle
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    InspectorChip(
                        label = "Solid",
                        selected = selectedShape.fillType == FillType.SOLID,
                        accentColor = accentColor
                    ) {
                        onUpdateShape { it.copy(fillType = FillType.SOLID) }
                    }
                    InspectorChip(
                        label = "Grad",
                        selected = selectedShape.fillType == FillType.GRADIENT,
                        accentColor = accentColor
                    ) {
                        onUpdateShape { it.copy(fillType = FillType.GRADIENT) }
                    }
                }
            }

            // Section 3: Stroke & Position
            Divider(color = BorderSubtle)
            Text("STROKE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(selectedShape.strokeColor))
                            .border(1.dp, Color.White.copy(alpha = 0.3f))
                            .clickable { onOpenColorPicker("stroke") }
                    )
                    Text(
                        text = "${selectedShape.strokeWidth.toInt()}px",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    InspectorChip(
                        label = "In",
                        selected = selectedShape.strokePosition == StrokePosition.INSIDE,
                        accentColor = accentColor
                    ) {
                        onUpdateShape { it.copy(strokePosition = StrokePosition.INSIDE) }
                    }
                    InspectorChip(
                        label = "Mid",
                        selected = selectedShape.strokePosition == StrokePosition.CENTER,
                        accentColor = accentColor
                    ) {
                        onUpdateShape { it.copy(strokePosition = StrokePosition.CENTER) }
                    }
                    InspectorChip(
                        label = "Out",
                        selected = selectedShape.strokePosition == StrokePosition.OUTSIDE,
                        accentColor = accentColor
                    ) {
                        onUpdateShape { it.copy(strokePosition = StrokePosition.OUTSIDE) }
                    }
                }
            }

            // Stroke Width Slider
            Slider(
                value = selectedShape.strokeWidth,
                onValueChange = { w -> onUpdateShape { it.copy(strokeWidth = w) } },
                valueRange = 0f..20f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(accentColor),
                    activeTrackColor = Color(accentColor)
                )
            )

            // Section 4: Opacity
            Divider(color = BorderSubtle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("OPACITY", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${(selectedShape.opacity * 100).toInt()}%", color = TextPrimary, fontSize = 11.sp)
            }
            Slider(
                value = selectedShape.opacity,
                onValueChange = { op -> onUpdateShape { it.copy(opacity = op) } },
                valueRange = 0.05f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(accentColor),
                    activeTrackColor = Color(accentColor)
                )
            )

            // Section 5: Typography (if Shape is TEXT)
            if (selectedShape.type == ShapeType.TEXT) {
                Divider(color = BorderSubtle)
                Text("TYPOGRAPHY", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = selectedShape.text,
                    onValueChange = { t -> onUpdateShape { it.copy(text = t) } },
                    label = { Text("Text Content", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BgInputDark,
                        unfocusedContainerColor = BgInputDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Font Size: ${selectedShape.fontSize.toInt()}sp", color = TextSecondary, fontSize = 11.sp)
                    Row {
                        IconButton(
                            onClick = {
                                val newWeight = if (selectedShape.fontWeight >= 700) 400 else 700
                                onUpdateShape { it.copy(fontWeight = newWeight) }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatBold,
                                contentDescription = "Bold",
                                tint = if (selectedShape.fontWeight >= 700) Color(accentColor) else TextMuted
                            )
                        }
                        IconButton(
                            onClick = { onUpdateShape { it.copy(isItalic = !it.isItalic) } },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatItalic,
                                contentDescription = "Italic",
                                tint = if (selectedShape.isItalic) Color(accentColor) else TextMuted
                            )
                        }
                    }
                }

                Slider(
                    value = selectedShape.fontSize,
                    onValueChange = { sz -> onUpdateShape { it.copy(fontSize = sz) } },
                    valueRange = 8f..72f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(accentColor),
                        activeTrackColor = Color(accentColor)
                    )
                )
            }
        }
    }
}

@Composable
private fun NumberPropertyField(
    label: String,
    value: Int,
    onChange: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .width(125.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(BgInputDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value.toString(), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun InspectorChip(
    label: String,
    selected: Boolean,
    accentColor: Long,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) Color(accentColor) else BgInputDark)
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
