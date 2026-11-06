package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CanvasConfig
import com.example.model.CanvasMode
import com.example.ui.theme.BgInputDark
import com.example.ui.theme.BgPanelDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

// 1. Canvas Configuration Dialog (Infinite vs Fixed, DPI, Resolution, 1:1 to Custom)
@Composable
fun CanvasConfigDialog(
    currentConfig: CanvasConfig,
    accentColor: Long,
    onApply: (CanvasConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf(currentConfig.mode) }
    var width by remember { mutableStateOf(currentConfig.width.toString()) }
    var height by remember { mutableStateOf(currentConfig.height.toString()) }
    var dpi by remember { mutableStateOf(currentConfig.dpi.toString()) }
    var presetName by remember { mutableStateOf(currentConfig.presetName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgPanelDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth().testTag("dialog_canvas_config")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Konfigurasi Mode Kanvas & Resolusi",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Divider(color = BorderSubtle)

                // Mode Selector: Infinite Canvas vs Fixed Resolution
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { mode = CanvasMode.INFINITE }
                    ) {
                        RadioButton(
                            selected = mode == CanvasMode.INFINITE,
                            onClick = { mode = CanvasMode.INFINITE },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(accentColor))
                        )
                        Text("Infinite Canvas (Tak Terbatas)", color = TextPrimary, fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { mode = CanvasMode.FIXED }
                    ) {
                        RadioButton(
                            selected = mode == CanvasMode.FIXED,
                            onClick = { mode = CanvasMode.FIXED },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(accentColor))
                        )
                        Text("Fixed Canvas (Dimensi & DPI Tertentu)", color = TextPrimary, fontSize = 11.sp)
                    }
                }

                // If Fixed, show Preset quick picks
                if (mode == CanvasMode.FIXED) {
                    Text("PILIHAN PRESET UKURAN", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PresetChip("1:1 Square", 1080, 1080, presetName) { n, w, h ->
                            presetName = n; width = w.toString(); height = h.toString()
                        }
                        PresetChip("iPhone 16", 375, 812, presetName) { n, w, h ->
                            presetName = n; width = w.toString(); height = h.toString()
                        }
                        PresetChip("Desktop", 1440, 900, presetName) { n, w, h ->
                            presetName = n; width = w.toString(); height = h.toString()
                        }
                        PresetChip("Full HD", 1920, 1080, presetName) { n, w, h ->
                            presetName = n; width = w.toString(); height = h.toString()
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = width,
                            onValueChange = { width = it },
                            label = { Text("Width (px)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = BgInputDark,
                                unfocusedContainerColor = BgInputDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height (px)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = BgInputDark,
                                unfocusedContainerColor = BgInputDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    OutlinedTextField(
                        value = dpi,
                        onValueChange = { dpi = it },
                        label = { Text("Density / DPI (Misal: 72, 150, 300 DPI)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BgInputDark,
                            unfocusedContainerColor = BgInputDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Batal", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            val w = width.toIntOrNull() ?: 1080
                            val h = height.toIntOrNull() ?: 1080
                            val d = dpi.toIntOrNull() ?: 300
                            onApply(
                                currentConfig.copy(
                                    mode = mode,
                                    width = w,
                                    height = h,
                                    dpi = d,
                                    presetName = if (mode == CanvasMode.INFINITE) "Infinite Canvas" else presetName
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(accentColor)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Terapkan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    name: String,
    w: Int,
    h: Int,
    currentPreset: String,
    onSelect: (String, Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (currentPreset == name) Color(0x330D99FF) else BgInputDark)
            .border(1.dp, if (currentPreset == name) Color(0xFF0D99FF) else BorderSubtle, RoundedCornerShape(6.dp))
            .clickable { onSelect(name, w, h) }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(name, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

// 2. Color Palette / Custom Theme Accent Dialog
@Composable
fun ColorPickerDialog(
    title: String,
    accentColor: Long,
    onSelectColor: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val presetColors = listOf(
        0xFF0D99FF, // Figma Blue
        0xFF00D1B2, // Teal
        0xFF7038F4, // Purple
        0xFFFFD166, // Yellow
        0xFFEF4444, // Red
        0xFF10B981, // Emerald
        0xFFF59E0B, // Amber
        0xFFEC4899, // Pink
        0xFF6366F1, // Indigo
        0xFFFFFFFF, // White
        0xFF1E1E24, // Dark Gray
        0xFF000000  // Black
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgPanelDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.width(300.dp).testTag("dialog_color_picker")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Divider(color = BorderSubtle)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetColors) { colorLong ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .border(2.dp, if (accentColor == colorLong) Color.White else BorderSubtle, CircleShape)
                                .clickable {
                                    onSelectColor(colorLong)
                                    onDismiss()
                                }
                        )
                    }
                }
            }
        }
    }
}

// 3. Export Dialog (Exporting .uify, SVG, PNG, HTML, JSON)
@Composable
fun ExportDialog(
    accentColor: Long,
    onExportFormat: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgPanelDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.width(320.dp).testTag("dialog_export")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Ekspor Berkas Desain", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Pilih format output untuk menyimpan atau membagikan karya:",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Divider(color = BorderSubtle)

                ExportOptionItem(
                    title = "Proyek Uinify (.uify)",
                    desc = "Format asli lengkap untuk simpan & edit kembali di Uinify Studio",
                    tag = "UIFY",
                    accentColor = accentColor
                ) {
                    onExportFormat("UIFY")
                    onDismiss()
                }

                ExportOptionItem(
                    title = "Vektor SVG (.svg)",
                    desc = "Grafik vektor scalable standar untuk web & editor grafis",
                    tag = "SVG",
                    accentColor = accentColor
                ) {
                    onExportFormat("SVG")
                    onDismiss()
                }

                ExportOptionItem(
                    title = "Gambar PNG HD (.png)",
                    desc = "Raster image kualitas tinggi transparan/berwarna",
                    tag = "PNG",
                    accentColor = accentColor
                ) {
                    onExportFormat("PNG")
                    onDismiss()
                }

                ExportOptionItem(
                    title = "Single-File HTML (.html)",
                    desc = "Halaman web mandiri yang dapat langsung dibuka di browser",
                    tag = "HTML",
                    accentColor = accentColor
                ) {
                    onExportFormat("HTML")
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun ExportOptionItem(
    title: String,
    desc: String,
    tag: String,
    accentColor: Long,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BgInputDark)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = TextMuted, fontSize = 10.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(accentColor).copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(tag, color = Color(accentColor), fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

// 4. Import Dialog (Universal File Importer: .uify, JSON, SVG, HTML)
@Composable
fun ImportDialog(
    accentColor: Long,
    onImportTextContent: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var rawInputText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BgPanelDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth().testTag("dialog_import")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Impor Berkas Proyek", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Mendukung format .uify, JSON, kode SVG vektor, maupun dokumen HTML:",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    label = { Text("Paste isi file .uify / SVG / JSON / HTML disini") },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
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
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Tutup", color = TextSecondary)
                    }
                    Button(
                        onClick = {
                            if (rawInputText.isNotBlank()) {
                                onImportTextContent(rawInputText)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(accentColor)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Impor Sekarang", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
