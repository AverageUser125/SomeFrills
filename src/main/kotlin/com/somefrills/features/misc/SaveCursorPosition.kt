package com.somefrills.features.misc

import com.mojang.blaze3d.platform.InputConstants
import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.events.ScreenCloseEvent
import com.somefrills.events.ScreenOpenEvent
import com.somefrills.features.core.Feature
import com.somefrills.modules.FrillsFeature
import com.somefrills.features.misc.glowmob.chestui.ChestUI
import com.somefrills.events.core.EventHandle
import net.minecraft.client.Minecraft
import kotlin.math.abs

@FrillsFeature
object SaveCursorPosition : Feature(FrillsMod.config.misc.saveCursorPosition.enabled) {
    private val config get() = FrillsMod.config.misc.saveCursorPosition
    private var savedPositionedP1: Pair<Double, Double>? = null
    private var savedPosition: SavedPosition? = null

    fun active(): Boolean {
        if (config.onlyChestUI) {
            return mc.screen is ChestUI
        }
        return true
    }

    @JvmStatic
    fun saveCursorOriginal(x: Double, y: Double) {
        if (!active()) return
        savedPositionedP1 = Pair(x, y)
    }

    @JvmStatic
    fun saveCursorMiddle(middleX: Double, middleY: Double) {
        if (!active()) return

        savedPosition = SavedPosition(
            Pair(middleX, middleY),
            savedPositionedP1 ?: return,
            System.currentTimeMillis()
        )
    }

    @EventHandle
    fun onScreen(event: ScreenOpenEvent) {
        loadCursor(mc.mouseHandler.xpos(), mc.mouseHandler.ypos())
    }

    @EventHandle
    fun onScreen(event: ScreenCloseEvent) {
        savedPosition = null
    }

    @JvmStatic
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
            val client = Minecraft.getInstance()

            InputConstants.grabOrReleaseMouse(
                client.window,
                InputConstants.CURSOR_NORMAL,
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