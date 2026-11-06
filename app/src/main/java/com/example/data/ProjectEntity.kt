package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val layerCount: Int = 0,
    val canvasWidth: Int = 1080,
    val canvasHeight: Int = 1080,
    val canvasMode: String = "infinite", // "infinite" or "fixed"
    val presetName: String = "1:1",
    val accentColor: String = "#0D99FF",
    val uifyContent: String, // Full .uify JSON with all layers, object positioning (x, y, w, h, rot), effects
    val thumbnailUrl: String? = null,
    val isCurrentActive: Boolean = false
)
