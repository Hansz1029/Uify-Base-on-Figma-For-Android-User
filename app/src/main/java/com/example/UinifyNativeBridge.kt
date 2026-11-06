package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.ProjectEntity
import com.example.data.ProjectRoomRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class UinifyNativeBridge(
    private val activity: MainActivity,
    private val webView: WebView,
    private val repository: ProjectRoomRepository
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main)
    private val prefs = activity.getSharedPreferences("uinify_prefs", Context.MODE_PRIVATE)

    @JavascriptInterface
    fun autoSave(jsonString: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val currentId = activity.currentProjectId
                val savedId = repository.autoSaveFromUifyJson(jsonString, currentId)
                if (activity.currentProjectId != savedId) {
                    activity.currentProjectId = savedId
                }
                prefs.edit().putLong("last_autosave_time", System.currentTimeMillis()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun getSavedProject(): String {
        return runBlocking(Dispatchers.IO) {
            try {
                val currentId = activity.currentProjectId
                val entity = if (!currentId.isNullOrEmpty()) {
                    repository.getProjectById(currentId)
                } else {
                    repository.getCurrentActiveProject()
                }
                entity?.uifyContent ?: ""
            } catch (e: Exception) {
                ""
            }
        }
    }

    @JavascriptInterface
    fun getProjectsJson(): String {
        return runBlocking(Dispatchers.IO) {
            try {
                val list = repository.getAllProjects()
                val array = JSONArray()
                for (p in list) {
                    val obj = JSONObject().apply {
                        put("id", p.id)
                        put("title", p.title)
                        put("lastUpdated", p.lastUpdated)
                        put("layerCount", p.layerCount)
                        put("canvasWidth", p.canvasWidth)
                        put("canvasHeight", p.canvasHeight)
                        put("canvasMode", p.canvasMode)
                        put("presetName", p.presetName)
                        put("accentColor", p.accentColor)
                        put("uifyContent", p.uifyContent)
                        put("thumbnailUrl", p.thumbnailUrl)
                        put("isCurrentActive", p.isCurrentActive)
                    }
                    array.put(obj)
                }
                array.toString()
            } catch (e: Exception) {
                "[]"
            }
        }
    }

    @JavascriptInterface
    fun openProject(id: String) {
        mainHandler.post {
            activity.openEditorWithProject(id)
        }
    }

    @JavascriptInterface
    fun createNewProject(title: String, width: Int, height: Int, preset: String) {
        scope.launch {
            val entity = repository.createNewProject(title, width, height, preset)
            mainHandler.post {
                activity.openEditorWithProject(entity.id)
            }
        }
    }

    @JavascriptInterface
    fun duplicateProject(id: String) {
        scope.launch {
            repository.duplicateProject(id)
            mainHandler.post {
                activity.reloadDashboardProjects()
            }
        }
    }

    @JavascriptInterface
    fun deleteProject(id: String) {
        scope.launch {
            repository.deleteProject(id)
            mainHandler.post {
                activity.reloadDashboardProjects()
            }
        }
    }

    @JavascriptInterface
    fun returnToDashboard() {
        mainHandler.post {
            activity.switchToDashboard()
        }
    }

    @JavascriptInterface
    fun exportAllBackup() {
        scope.launch(Dispatchers.IO) {
            try {
                val list = repository.getAllProjects()
                val backupJson = JSONArray().apply {
                    for (p in list) {
                        put(JSONObject().apply {
                            put("id", p.id)
                            put("title", p.title)
                            put("uifyContent", p.uifyContent)
                        })
                    }
                }.toString(2)

                mainHandler.post {
                    exportFile("uinify_full_backup.uify", "application/octet-stream", backupJson, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @JavascriptInterface
    fun exportFile(filename: String, mimeType: String, content: String, isBase64: Boolean) {
        mainHandler.post {
            try {
                val exportDir = File(activity.cacheDir, "exports").apply { if (!exists()) mkdirs() }
                val targetFile = File(exportDir, filename)

                if (isBase64) {
                    val cleanBase64 = if (content.contains(",")) content.substringAfter(",") else content
                    val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    targetFile.writeBytes(bytes)
                } else {
                    targetFile.writeText(content)
                }

                val uri: Uri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.provider",
                    targetFile
                )

                val effectiveMime = when {
                    filename.endsWith(".uify") -> "application/octet-stream"
                    filename.endsWith(".svg") -> "image/svg+xml"
                    filename.endsWith(".png") -> "image/png"
                    filename.endsWith(".jpg") || filename.endsWith(".jpeg") -> "image/jpeg"
                    filename.endsWith(".json") -> "application/json"
                    filename.endsWith(".html") -> "text/html"
                    mimeType.isNotEmpty() -> mimeType
                    else -> "*/*"
                }

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = effectiveMime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, filename)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                activity.startActivity(Intent.createChooser(intent, "Simpan / Bagikan $filename"))
                Toast.makeText(activity, "File $filename siap diekspor", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(activity, "Gagal mengekspor file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @JavascriptInterface
    fun requestImport(filterMime: String) {
        mainHandler.post {
            activity.launchFilePicker(filterMime)
        }
    }

    @JavascriptInterface
    fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun setAccentColor(hex: String) {
        prefs.edit().putString("accent_color", hex).apply()
    }
}
