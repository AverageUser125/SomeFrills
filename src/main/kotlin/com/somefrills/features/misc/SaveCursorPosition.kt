package com.somefrills.features.misc

import com.somefrills.Main.mc
import com.somefrills.config.FrillsConfig
import com.somefrills.events.ScreenCloseEvent
import com.somefrills.events.ScreenOpenEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.features.misc.glowmob.chestui.ChestUI
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import kotlin.math.abs

@FrillsFeature
class SaveCursorPosition : Feature(FrillsConfig.misc.saveCursorPosition.enabled) {
    private val config get() = FrillsConfig.misc.saveCursorPosition
    private var savedPositionedP1: Pair<Double, Double>? = null
    private var savedPosition: SavedPosition? = null

    fun active(): Boolean {
        if (config.onlyChestUI) {
            return mc.currentScreen is ChestUI
        }
        return true
    }

    fun saveCursorOriginal(x: Double, y: Double) {
        if (!active()) return
        savedPositionedP1 = Pair(x, y)
    }

    fun saveCursorMiddle(middleX: Double, middleY: Double) {
        if (!active()) return

        savedPosition = SavedPosition(
            Pair(middleX, middleY),
            savedPositionedP1 ?: return,
            System.currentTimeMillis()
        )
    }

    @EventHandler
    fun onScreen(event: ScreenOpenEvent) {
        loadCursor(mc.mouse.x, mc.mouse.y)
    }

    @EventHandler
    fun onScreen(event: ScreenCloseEvent) {
        savedPosition = null
    }

    fun loadCursor(middleX: Double, middleY: Double): Pair<Double, Double>? {
        if (!active()) return null
        val pos = savedPosition ?: return null

        val now = System.currentTimeMillis()

        if (now - pos.savedAt > 5000) {
            savedPosition = null
            return null
        }

        if (abs(pos.middle.first - middleX) < 1
            && abs(pos.middle.second - middleY) < 1
        ) {
            val client = MinecraftClient.getInstance()

            InputUtil.setCursorParameters(
                client.window,
                InputUtil.GLFW_CURSOR_NORMAL,
                pos.cursor.first,
                pos.cursor.second
            )

            return pos.cursor
        }

        return null
    }

    @JvmRecord
    data class SavedPosition(
        val middle: Pair<Double, Double>,
        val cursor: Pair<Double, Double>,
        val savedAt: Long
    )
}