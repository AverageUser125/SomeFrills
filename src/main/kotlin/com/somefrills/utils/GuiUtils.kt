package com.somefrills.utils

import com.somefrills.Main.mc
import com.somefrills.Main.config
import com.somefrills.mixin.BossBarHudAccessor
import com.somefrills.mixin.PlayerListHudAccessor
import com.somefrills.mixininterface.TitleRendering
import com.somefrills.misc.RenderColor
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import net.minecraft.client.gui.hud.ClientBossBar
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

object GuiUtils {
    fun getGuiScreenInternal(): Screen {
        return getGuiScreenInternal(null)
    }

    fun getGuiScreenInternal(previous: Screen?): Screen {
        val editor = config.getEditor()
        val guiContext = GuiContext(GuiElementComponent(editor))
        return object : MoulConfigScreenComponent(Text.empty(), guiContext, previous) {
            override fun close() {
                if (previous == null) {
                    super.close()
                } else {
                    setScreenInternal(previous)
                }
            }
        }
    }

    fun setScreenInternal(screen: Screen) {
        mc.send { mc.setScreen(screen) }
    }

    fun getFooterLinesInternal(): List<String> {
        val list = ArrayList<String>()
        val footer = (mc.inGameHud.playerListHud as PlayerListHudAccessor).footer
        if (footer != null) {
            val lines = footer.string.split("\n")
            for (line in lines) {
                val l = line.trim()
                if (l.isNotEmpty()) {
                    list.add(l)
                }
            }
        }
        return list
    }

    fun getBossBarsInternal(): List<ClientBossBar> {
        val bossBars = (mc.inGameHud.bossBarHud as BossBarHudAccessor).bossBars
        return bossBars.values.toList()
    }



// ========== Screen Extension Functions ==========

    fun Screen.setAsPrevious(): Screen = this

// ========== Title Rendering Extension Functions ==========

    fun showTitle(title: String, subtitle: String, fadeInTicks: Int, stayTicks: Int, fadeOutTicks: Int) {
        mc.inGameHud.setTitle(Text.of(title))
        mc.inGameHud.setSubtitle(Text.of(subtitle))
        mc.inGameHud.setTitleTicks(fadeInTicks, stayTicks, fadeOutTicks)
    }

    fun showTitleCustom(title: String, stayTicks: Int, yOffset: Int = 0, scale: Float = 1.0f, color: RenderColor? = null) {
        val c = color ?: RenderColor(255, 255, 255, 255)
        (mc.inGameHud as TitleRendering).`somefrills$setRenderTitle`(title, stayTicks, yOffset, scale, c)
    }

    val isRenderingCustomTitle: Boolean
        get() = (mc.inGameHud as TitleRendering).`somefrills$isRenderingTitle`()

// ========== HUD Access Extension Functions ==========

    val footerLines: List<String>
        get() = GuiUtils.getFooterLinesInternal()

    val bossBars: List<ClientBossBar>
        get() = GuiUtils.getBossBarsInternal()

// ========== GUI Screen Management ==========

    fun getGuiScreen(): Screen = GuiUtils.getGuiScreenInternal()

    fun getGuiScreen(previous: Screen?): Screen = GuiUtils.getGuiScreenInternal(previous)

    fun setScreen(screen: Screen) = GuiUtils.setScreenInternal(screen)

    fun showGui() = setScreen(getGuiScreen())

    val mousePos: org.joml.Vector2d
        get() = org.joml.Vector2d(
            mc.mouse.getScaledX(mc.window),
            mc.mouse.getScaledY(mc.window)
        )

}