// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiClass
package com.somefrills.utils

import com.somefrills.Main.config
import com.somefrills.Main.mc
import com.somefrills.misc.RenderColor
import com.somefrills.mixin.BossHealthOverlayAccessor
import com.somefrills.mixin.PlayerTabOverlayAccessor
import com.somefrills.mixininterface.TitleRendering
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import net.minecraft.client.gui.components.LerpingBossEvent
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent




object GuiUtils {
    fun getGuiScreenInternal(): Screen {
        return getGuiScreenInternal(null)
    }

    fun getGuiScreenInternal(previous: Screen?): Screen {
        val editor = config.getEditor()
        val guiContext = GuiContext(GuiElementComponent(editor))
        return object : MoulConfigScreenComponent(Component.empty(), guiContext, previous) {
            override fun onClose() {
                if (previous == null) {
                    super.onClose()
                } else {
                    setScreenInternal(previous)
                }
            }
        }
    }

    fun setScreenInternal(screen: Screen) {
        mc.execute { mc.setScreen(screen) }
    }

    fun getFooterLinesInternal(): MutableList<String> {
        val list: MutableList<String> = ArrayList<String>()
        val footer = (mc.gui.getTabList() as PlayerTabOverlayAccessor).getFooter()
        if (footer != null) {
            val lines = footer.getString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (line in lines) {
                val l = line.trim { it <= ' ' }
                if (!l.isEmpty()) {
                    list.add(l)
                }
            }
        }
        return list
    }

    fun getBossBarsInternal(): MutableList<LerpingBossEvent> {
        return (mc.gui.bossOverlay as BossHealthOverlayAccessor).getEvents().values.stream().toList()
    }


// ========== Screen Extension Functions ==========

    fun Screen.setAsPrevious(): Screen = this

// ========== Title Rendering Extension Functions ==========

    fun showTitle(
        title: MutableComponent,
        subtitle: MutableComponent,
        fadeInTicks: Int,
        stayTicks: Int,
        fadeOutTicks: Int
    ) {
        mc.gui.setTitle(title)
        mc.gui.setSubtitle(subtitle)
        mc.gui.setTimes(fadeInTicks, stayTicks, fadeOutTicks)
    }

    fun showTitle(title: String, subtitle: String, fadeInTicks: Int, stayTicks: Int, fadeOutTicks: Int) {
        showTitle(Component.literal(title), Component.literal(subtitle), fadeInTicks, stayTicks, fadeOutTicks)
    }

    fun showTitleCustom(title: String, stayTicks: Int, yOffset: Int = 0, scale: Float = 1.0f, color: RenderColor? = null) {
        val c = color ?: RenderColor(255, 255, 255, 255)
        (mc.gui as TitleRendering).`somefrills$setRenderTitle`(title, stayTicks, yOffset, scale, c)
    }

    val isRenderingCustomTitle: Boolean
        get() = (mc.gui as TitleRendering).`somefrills$isRenderingTitle`()

// ========== HUD Access Extension Functions ==========

    val footerLines: List<String>
        get() = GuiUtils.getFooterLinesInternal()

    val bossBars: List<LerpingBossEvent>
        get() = GuiUtils.getBossBarsInternal()

// ========== GUI Screen Management ==========

    fun getGuiScreen(): Screen = GuiUtils.getGuiScreenInternal()

    fun getGuiScreen(previous: Screen?): Screen = GuiUtils.getGuiScreenInternal(previous)

    fun setScreen(screen: Screen) = GuiUtils.setScreenInternal(screen)

    fun showGui() = setScreen(getGuiScreen())

    val mousePos: org.joml.Vector2d
        get() = org.joml.Vector2d(
            mc.mouseHandler.getScaledXPos(mc.window),
            mc.mouseHandler.getScaledYPos(mc.window)
        )

}