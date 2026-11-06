package com.example

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ProjectRoomRepository
import org.json.JSONObject
import java.io.InputStream

class MainActivity : ComponentActivity() {

    private var webView: WebView? = null
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    val repository: ProjectRoomRepository by lazy { ProjectRoomRepository(this) }

    var currentScreen by mutableStateOf("dashboard") // "dashboard" or "editor"
    var currentProjectId: String? = null
    var currentProjectTitle by mutableStateOf("Uinify Studio")

    // Native file picker launcher for Import
    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            handleImportedUri(uri)
        }
    }

    // Chrome file chooser launcher for <input type="file">
    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val results: Array<Uri>? = when {
                data?.data != null -> arrayOf(data.data!!)
                data?.clipData != null -> {
                    val count = data.clipData!!.itemCount
                    Array(count) { i -> data.clipData!!.getItemAt(i).uri }
                }
                else -> null
            }
            filePathCallback?.onReceiveValue(results)
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentScreen == "editor") {
                    switchToDashboard()
                } else if (webView?.canGoBack() == true) {
                    webView?.goBack()
                } else {
                    finish()
                }
            }
        })

        setContent {
            val insets = WindowInsets.systemBars.asPaddingValues()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0A0C))
                    .padding(
                        top = insets.calculateTopPadding(),
                        bottom = insets.calculateBottomPadding()
                    )
            ) {
                UinifyWebView()

                // Render Floating Capsule Topbar with Horizontal Scrolling Row in Editor
                if (currentScreen == "editor") {
                    FloatingCapsuleTopBar(
                        projectName = currentProjectTitle,
                        onBackClick = { switchToDashboard() },
                        onProjectMenuClick = {
                            webView?.evaluateJavascript("window.openProjectMenuModal?.();", null)
                        },
                        onAction = { script ->
                            webView?.evaluateJavascript(script, null)
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    private fun UinifyWebView() {
        val context = LocalContext.current
        val webViewInstance = remember {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.parseColor("#0A0A0C"))
                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(true)
                    builtInZoomControls = false
                    displayZoomControls = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    cacheMode = WebSettings.LOAD_DEFAULT
                }

                val bridge = UinifyNativeBridge(this@MainActivity, this, repository)
                addJavascriptInterface(bridge, "UinifyNative")

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        android.util.Log.d("UinifyApp", "${consoleMessage?.message()} -- line ${consoleMessage?.lineNumber()}")
                        return true
                    }

                    override fun onShowFileChooser(
                        mWebView: WebView?,
                        callback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        filePathCallback?.onReceiveValue(null)
                        filePathCallback = callback

                        val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "*/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        try {
                            fileChooserLauncher.launch(intent)
                        } catch (e: Exception) {
                            filePathCallback = null
                            return false
                        }
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (currentScreen == "dashboard") {
                            evaluateJavascript("window.loadProjectsFromNative?.();", null)
                        }
                    }
                }

                loadUrl("file:///android_asset/dashboard.html")
            }
        }

        DisposableEffect(webViewInstance) {
            webView = webViewInstance
            onDispose {
                webViewInstance.destroy()
                webView = null
            }
        }

        AndroidView(
            factory = { webViewInstance },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    private fun FloatingCapsuleTopBar(
        projectName: String,
        onBackClick: () -> Unit,
        onProjectMenuClick: () -> Unit,
        onAction: (String) -> Unit
    ) {
        val scrollState = rememberScrollState()

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .height(48.dp),
            shape = CircleShape,
            color = Color(0xF208080A),
            border = BorderStroke(1.dp, Color(0x30FFFFFF)),
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Back Button to Dashboard
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Dashboard",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 2. Uinify Monochrome White Logo + Project Name
                Surface(
                    onClick = onProjectMenuClick,
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x1AFFFFFF),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "U",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = projectName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Menu Proyek",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(16.dp)
                        .width(1.dp),
                    color = Color(0x22FFFFFF)
                )

                // 3. Canvas Mode (1:1, Infinity, Custom, DPI)
                CapsuleActionIconButton(
                    icon = Icons.Default.AspectRatio,
                    tooltip = "Mode Kanvas",
                    onClick = { onAction("window.openCanvasModeModal?.();") }
                )

                // 4. Accent Color
                CapsuleActionIconButton(
                    icon = Icons.Default.Palette,
                    tooltip = "Aksen Warna",
                    onClick = { onAction("window.openAccentColorModal?.();") }
                )

                // 5. Import File
                CapsuleActionIconButton(
                    icon = Icons.Default.FileUpload,
                    tooltip = "Impor File",
                    onClick = { onAction("window.openImportModal?.();") }
                )

                // 6. Export File
                CapsuleActionIconButton(
                    icon = Icons.Default.FileDownload,
                    tooltip = "Ekspor File",
                    onClick = { onAction("window.openExportModal?.();") }
                )

                // 7. Fit to Screen
                CapsuleActionIconButton(
                    icon = Icons.Default.FitScreen,
                    tooltip = "Fit to Screen",
                    onClick = { onAction("window.centerCanvas?.();") }
                )

                // 8. Toggle Grid
                CapsuleActionIconButton(
                    icon = Icons.Default.GridOn,
                    tooltip = "Toggle Grid",
                    onClick = { onAction("window.toggleGrid?.();") }
                )

                // 9. Component
                CapsuleActionIconButton(
                    icon = Icons.Default.AutoAwesome,
                    tooltip = "Komponen",
                    onClick = { onAction("window.createComponent?.();") }
                )

                // 10. Mask
                CapsuleActionIconButton(
                    icon = Icons.Default.Contrast,
                    tooltip = "Mask",
                    onClick = { onAction("window.toggleMask?.();") }
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(16.dp)
                        .width(1.dp),
                    color = Color(0x22FFFFFF)
                )

                // 11. Undo
                CapsuleActionIconButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    tooltip = "Undo",
                    onClick = { onAction("window.undo?.();") }
                )

                // 12. Redo
                CapsuleActionIconButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    tooltip = "Redo",
                    onClick = { onAction("window.redo?.();") }
                )

                // 13. Layers Toggle Button
                Surface(
                    onClick = { onAction("document.getElementById('btn-toggle-layers-top')?.click();") },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0x22FFFFFF),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layers",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text("Layers", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 14. Design Toggle Button
                Surface(
                    onClick = { onAction("document.getElementById('btn-toggle-insp-top')?.click();") },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0D99FF),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Design",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text("Design", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable
    private fun CapsuleActionIconButton(
        icon: ImageVector,
        tooltip: String,
        onClick: () -> Unit
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tooltip,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(17.dp)
            )
        }
    }

    fun openEditorWithProject(projectId: String) {
        currentProjectId = projectId
        currentScreen = "editor"
        webView?.loadUrl("file:///android_asset/editor.html")
    }

    fun switchToDashboard() {
        currentScreen = "dashboard"
        webView?.loadUrl("file:///android_asset/dashboard.html")
    }

    fun reloadDashboardProjects() {
        webView?.evaluateJavascript("window.loadProjectsFromNative?.();", null)
    }

    fun launchFilePicker(filterMime: String) {
        val types = when (filterMime) {
            "image/*" -> arrayOf("image/*")
            ".svg" -> arrayOf("image/svg+xml")
            ".html" -> arrayOf("text/html")
            ".uify" -> arrayOf("*/*")
            else -> arrayOf("*/*")
        }
        try {
            openDocumentLauncher.launch(types)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak dapat membuka pemilih file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleImportedUri(uri: Uri) {
        try {
            var displayName = "file"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    displayName = cursor.getString(nameIndex)
                }
            }

            val mimeType = contentResolver.getType(uri) ?: when {
                displayName.endsWith(".uify") -> "application/octet-stream"
                displayName.endsWith(".svg") -> "image/svg+xml"
                displayName.endsWith(".png") -> "image/png"
                displayName.endsWith(".jpg") || displayName.endsWith(".jpeg") -> "image/jpeg"
                displayName.endsWith(".json") -> "application/json"
                displayName.endsWith(".html") -> "text/html"
                else -> "*/*"
            }

            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Toast.makeText(this, "Gagal membaca file", Toast.LENGTH_SHORT).show()
                return
            }

            val isImage = mimeType.startsWith("image/") && !displayName.endsWith(".svg")

            val content = if (isImage) {
                val bytes = inputStream.readBytes()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                "data:$mimeType;base64,$base64"
            } else {
                inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }

            val safeName = JSONObject.quote(displayName)
            val safeMime = JSONObject.quote(mimeType)
            val safeContent = JSONObject.quote(content)

            val script = "window.handleImportedFile($safeName, $safeMime, $safeContent);"
            webView?.evaluateJavascript(script) {
                Toast.makeText(this, "Berhasil memuat $displayName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal mengimpor file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
