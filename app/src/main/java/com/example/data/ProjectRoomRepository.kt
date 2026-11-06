package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ProjectRoomRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val dao = database.projectDao()

    suspend fun getAllProjects(): List<ProjectEntity> = withContext(Dispatchers.IO) {
        val projects = dao.getAllProjects()
        if (projects.isEmpty()) {
            val defaults = createDefaultSeedProjects()
            defaults.forEach { dao.insertOrUpdate(it) }
            return@withContext defaults
        }
        projects
    }

    suspend fun getProjectById(id: String): ProjectEntity? = withContext(Dispatchers.IO) {
        dao.getProjectById(id)
    }

    suspend fun getCurrentActiveProject(): ProjectEntity? = withContext(Dispatchers.IO) {
        dao.getCurrentActiveProject() ?: getAllProjects().firstOrNull()
    }

    suspend fun saveProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        dao.insertOrUpdate(project)
    }

    suspend fun autoSaveFromUifyJson(jsonString: String, activeId: String?): String = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val title = root.optString("projectName", root.optString("title", "Uinify Project"))
            val shapes = root.optJSONArray("shapes") ?: JSONArray()
            val layerCount = shapes.length()

            val canvasConfig = root.optJSONObject("canvasConfig")
            val width = canvasConfig?.optInt("width", 1080) ?: 1080
            val height = canvasConfig?.optInt("height", 1080) ?: 1080
            val preset = canvasConfig?.optString("preset", "1:1") ?: "1:1"
            val canvasMode = root.optString("canvasMode", "infinite")
            val accentColor = root.optString("accentColor", "#0D99FF")

            val targetId = if (!activeId.isNullOrEmpty()) activeId else "proj-${System.currentTimeMillis()}"

            // Generate an SVG preview if shapes exist
            val entity = ProjectEntity(
                id = targetId,
                title = title,
                lastUpdated = System.currentTimeMillis(),
                layerCount = layerCount,
                canvasWidth = width,
                canvasHeight = height,
                canvasMode = canvasMode,
                presetName = preset,
                accentColor = accentColor,
                uifyContent = jsonString,
                thumbnailUrl = "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?q=80&w=400&auto=format&fit=crop",
                isCurrentActive = true
            )

            dao.clearActiveProjects()
            dao.insertOrUpdate(entity)
            return@withContext targetId
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext activeId ?: "proj-${System.currentTimeMillis()}"
        }
    }

    suspend fun createNewProject(title: String, width: Int, height: Int, preset: String): ProjectEntity = withContext(Dispatchers.IO) {
        val id = "proj-${System.currentTimeMillis()}"
        val initialUify = JSONObject().apply {
            put("uinify", true)
            put("version", "1.0")
            put("projectName", title)
            put("canvasMode", if (preset.equals("infinite", ignoreCase = true)) "infinite" else "fixed")
            put("canvasConfig", JSONObject().apply {
                put("preset", preset)
                put("width", width)
                put("height", height)
                put("dpi", 72)
                put("bg", "#1e1e1e")
            })
            put("accentColor", "#0D99FF")
            put("createdAt", System.currentTimeMillis())
            put("shapes", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "frame-main")
                    put("name", "Artboard - 1")
                    put("type", "frame")
                    put("x", 80)
                    put("y", 80)
                    put("w", width.coerceAtMost(600))
                    put("h", height.coerceAtMost(800))
                    put("fill", "#ffffff")
                    put("fillType", "solid")
                    put("stroke", "#000000")
                    put("strokeWidth", 0)
                    put("radius", 16)
                    put("opacity", 100)
                })
            })
            put("viewport", JSONObject().apply {
                put("x", 80)
                put("y", 80)
                put("zoom", 0.9)
            })
        }.toString()

        val entity = ProjectEntity(
            id = id,
            title = title,
            lastUpdated = System.currentTimeMillis(),
            layerCount = 1,
            canvasWidth = width,
            canvasHeight = height,
            canvasMode = if (preset.equals("infinite", ignoreCase = true)) "infinite" else "fixed",
            presetName = preset,
            accentColor = "#0D99FF",
            uifyContent = initialUify,
            thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=400&auto=format&fit=crop",
            isCurrentActive = true
        )

        dao.clearActiveProjects()
        dao.insertOrUpdate(entity)
        entity
    }

    suspend fun duplicateProject(id: String): ProjectEntity? = withContext(Dispatchers.IO) {
        val original = dao.getProjectById(id) ?: return@withContext null
        val newId = "proj-${System.currentTimeMillis()}"
        val newTitle = "${original.title} (Copy)"

        val newContent = try {
            val obj = JSONObject(original.uifyContent)
            obj.put("projectName", newTitle)
            obj.toString()
        } catch (_: Exception) {
            original.uifyContent
        }

        val dup = original.copy(
            id = newId,
            title = newTitle,
            lastUpdated = System.currentTimeMillis(),
            uifyContent = newContent,
            isCurrentActive = false
        )
        dao.insertOrUpdate(dup)
        dup
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    private fun createDefaultSeedProjects(): List<ProjectEntity> {
        val p1Uify = """
        {
          "uinify": true,
          "version": "1.0",
          "projectName": "iOS UI Design Kit",
          "canvasMode": "fixed",
          "canvasConfig": { "preset": "9:16", "width": 375, "height": 812, "dpi": 72, "bg": "#121216" },
          "accentColor": "#0D99FF",
          "shapes": [
            { "id": "f-1", "name": "iPhone 16 Frame", "type": "frame", "x": 80, "y": 80, "w": 375, "h": 812, "fill": "#121216", "radius": 24, "stroke": "#33ffffff", "strokeWidth": 1, "opacity": 100 },
            { "id": "r-1", "name": "Hero Card", "type": "rectangle", "x": 105, "y": 140, "w": 325, "h": 180, "fill": "#7038F4", "fillType": "gradient", "radius": 20, "opacity": 100, "gradients": { "type": "linear", "angle": 135, "stops": [{ "pos": 0, "color": "#7038F4" }, { "pos": 100, "color": "#0D99FF" }] } },
            { "id": "t-1", "name": "Headline", "type": "text", "x": 130, "y": 180, "w": 280, "h": 40, "text": "Uinify Studio Native", "fontSize": 24, "fontWeight": "700", "fill": "#ffffff", "opacity": 100 },
            { "id": "t-2", "name": "Subtitle", "type": "text", "x": 130, "y": 230, "w": 280, "h": 30, "text": "Precision vector canvas", "fontSize": 14, "fill": "#ffffff", "opacity": 80 }
          ],
          "viewport": { "x": 80, "y": 80, "zoom": 0.9 }
        }
        """.trimIndent()

        val p2Uify = """
        {
          "uinify": true,
          "version": "1.0",
          "projectName": "Neumorphic Dashboard",
          "canvasMode": "fixed",
          "canvasConfig": { "preset": "1:1", "width": 1080, "height": 1080, "dpi": 72, "bg": "#16161a" },
          "accentColor": "#00D1B2",
          "shapes": [
            { "id": "f-2", "name": "Square Board", "type": "frame", "x": 100, "y": 100, "w": 500, "h": 500, "fill": "#16161a", "radius": 28, "stroke": "#22ffffff", "strokeWidth": 1, "opacity": 100 },
            { "id": "r-2", "name": "Teal Accent Pill", "type": "ellipse", "x": 140, "y": 160, "w": 120, "h": 60, "fill": "#00D1B2", "opacity": 100 }
          ],
          "viewport": { "x": 80, "y": 80, "zoom": 0.85 }
        }
        """.trimIndent()

        return listOf(
            ProjectEntity(
                id = "proj-seed-1",
                title = "iOS UI Design Kit",
                lastUpdated = System.currentTimeMillis() - 100000,
                layerCount = 4,
                canvasWidth = 375,
                canvasHeight = 812,
                canvasMode = "fixed",
                presetName = "9:16",
                accentColor = "#0D99FF",
                uifyContent = p1Uify,
                thumbnailUrl = "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?q=80&w=400&auto=format&fit=crop",
                isCurrentActive = true
            ),
            ProjectEntity(
                id = "proj-seed-2",
                title = "Neumorphic Dashboard",
                lastUpdated = System.currentTimeMillis() - 3600000,
                layerCount = 2,
                canvasWidth = 1080,
                canvasHeight = 1080,
                canvasMode = "fixed",
                presetName = "1:1",
                accentColor = "#00D1B2",
                uifyContent = p2Uify,
                thumbnailUrl = "https://images.unsplash.com/photo-1558002038-1055907df827?q=80&w=400&auto=format&fit=crop",
                isCurrentActive = false
            )
        )
    }
}
