package com.example

import com.example.model.CanvasConfig
import com.example.model.CanvasMode
import com.example.model.DesignShape
import com.example.model.ShapeType
import com.example.model.UinifyProject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UinifyProjectModelTest {

    @Test
    fun serialization_and_deserialization_isAccurate() {
        val original = UinifyProject(
            title = "Test Studio Project",
            canvasConfig = CanvasConfig(
                mode = CanvasMode.FIXED,
                width = 1080,
                height = 1080,
                presetName = "1:1 Square",
                dpi = 300
            ),
            accentColor = 0xFF0D99FF,
            shapes = listOf(
                DesignShape(
                    name = "Header Text",
                    type = ShapeType.TEXT,
                    x = 100f,
                    y = 120f,
                    width = 250f,
                    height = 40f,
                    text = "Uinify Mobile",
                    fontSize = 24f,
                    fillColor = 0xFFFFFFFF
                ),
                DesignShape(
                    name = "Card Background",
                    type = ShapeType.RECT,
                    x = 80f,
                    y = 200f,
                    width = 300f,
                    height = 150f,
                    cornerRadius = 16f,
                    fillColor = 0xFF7038F4
                )
            )
        )

        val jsonString = original.toJson().toString()
        val parsed = UinifyProject.fromJson(jsonString)

        assertEquals(original.title, parsed.title)
        assertEquals(CanvasMode.FIXED, parsed.canvasConfig.mode)
        assertEquals(1080, parsed.canvasConfig.width)
        assertEquals(300, parsed.canvasConfig.dpi)
        assertEquals(2, parsed.shapes.size)
        assertEquals("Header Text", parsed.shapes[0].name)
        assertEquals(ShapeType.TEXT, parsed.shapes[0].type)
        assertEquals("Uinify Mobile", parsed.shapes[0].text)
        assertEquals("Card Background", parsed.shapes[1].name)
    }

    @Test
    fun infiniteCanvasConfig_defaults_correct() {
        val config = CanvasConfig()
        assertEquals(CanvasMode.INFINITE, config.mode)
        assertEquals(300, config.dpi)
    }
}
