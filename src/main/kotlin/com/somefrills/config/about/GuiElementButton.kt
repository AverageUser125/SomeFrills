package com.somefrills.config.about

import io.github.notenoughupdates.moulconfig.common.RenderContext
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import java.awt.Color

class GuiElementButton {
    var width: Int = -1
    var text: String = ""

    fun getWidth(context: RenderContext): Int {
        val fr = context.minecraft.defaultFontRenderer
        return fr.getStringWidth(text) + 10
    }

    fun render(context: RenderContext, x: Int, y: Int) {
        context.drawColoredRect(
            x.toFloat(),
            y.toFloat(),
            (x + width).toFloat(),
            (y + 18).toFloat(),
            Color.WHITE.rgb
        )
        context.drawColoredRect(
            (x + 1).toFloat(),
            (y + 1).toFloat(),
            (x + width - 1).toFloat(),
            (y + 18 - 1).toFloat(),
            Color.BLACK.rgb
        )
        val fr = context.minecraft.defaultFontRenderer
        context.drawString(fr, StructuredText.of(text), x + 5, y + 5, -1, true)
    }

    companion object {
        const val HEIGHT: Int = 18 + 5
    }
}

