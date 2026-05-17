package com.somefrills.events

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.somefrills.Main.mc
import com.somefrills.misc.RenderColor
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.texture.TextureSetup
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import org.joml.Matrix3x2f
import org.joml.Vector2d

open class ScreenRenderEvent(
    var context: DrawContext,
    var mouseX: Int,
    var mouseY: Int,
    var deltaTicks: Float,
    var title: String?,
    var handler: ScreenHandler,
    var focusedSlot: Slot?
) {
    private fun getSlot(slotId: Int): Slot? {
        if (slotId < 0 || slotId >= this.handler.slots.size) {
            return null
        }
        return this.handler.getSlot(slotId)
    }

    fun drawLine(firstId: Int, secondId: Int, width: Double, color: RenderColor) {
        val slot1 = this.getSlot(firstId)
        val slot2 = this.getSlot(secondId)
        if (slot1 != null && slot2 != null) {
            this.drawLine(
                RenderPipelines.GUI,
                slot1.x + 8,
                slot1.y + 8,
                slot2.x + 8,
                slot2.y + 8,
                width,
                RenderColor.ofArgb(color.argb)
            )
        }
    }

    fun drawLine(pipeline: RenderPipeline?, x1: Int, y1: Int, x2: Int, y2: Int, width: Double, color: RenderColor?) {
        this.context.state.addSimpleElement(
            LineElementRenderState(
                pipeline,
                Matrix3x2f(context.matrices),
                context.scissorStack.peekLast(),
                x1, y1, x2, y2,
                width,
                color
            )
        )
    }

    fun drawBorder(slotId: Int, color: RenderColor) {
        this.getSlot(slotId)?.let { slot: Slot ->
            drawBorder(this.context, slot.x, slot.y, 16, 16, color.argb)
        }
    }

    fun drawLabel(slotId: Int, text: Text?) {
        this.getSlot(slotId)?.let { slot: Slot ->
            this.context.drawCenteredTextWithShadow(
                mc.textRenderer,
                text,
                slot.x + 8,
                slot.y + 4,
                RenderColor.white.argb
            )
        }
    }

    fun drawFill(slotId: Int, color: RenderColor) {
        this.getSlot(slotId)?.let { slot: Slot ->
            this.context.fill(
                slot.x,
                slot.y,
                slot.x + 16,
                slot.y + 16,
                color.argb
            )
        }
    }

    class Before(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
        title: String?,
        handler: ScreenHandler,
        focusedSlot: Slot?
    ) : ScreenRenderEvent(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot)

    class After(
        context: DrawContext,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
        title: String?,
        handler: ScreenHandler,
        focusedSlot: Slot?
    ) : ScreenRenderEvent(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot)

    data class LineElementRenderState(
        val pipeline: RenderPipeline?,
        val pose: Matrix3x2f?,
        val scissorArea: ScreenRect?,
        val x0: Int,
        val y0: Int,
        val x1: Int,
        val y1: Int,
        val thiccness: Double,
        val color: RenderColor?
    ) : SimpleGuiElementRenderState {
        override fun setupVertices(vertices: VertexConsumer) {
            val offset =
                Vector2d((this.x1 - this.x0).toDouble(), (this.y1 - this.y0).toDouble()).perpendicular().normalize()
                    .mul(this.thiccness * .5)

            val vColor = this.color!!.argb
            vertices.vertex(this.pose, (x0 + offset.x).toFloat(), (y0 + offset.y).toFloat()).color(vColor)
            vertices.vertex(this.pose, (x0 - offset.x).toFloat(), (y0 - offset.y).toFloat()).color(vColor)
            vertices.vertex(this.pose, (x1 - offset.x).toFloat(), (y1 - offset.y).toFloat()).color(vColor)
            vertices.vertex(this.pose, (x1 + offset.x).toFloat(), (y1 + offset.y).toFloat()).color(vColor)
        }

        override fun pipeline(): RenderPipeline? {
            return pipeline
        }

        override fun textureSetup(): TextureSetup? {
            return TextureSetup.empty()
        }

        override fun scissorArea(): ScreenRect? {
            return scissorArea
        }

        override fun bounds(): ScreenRect {
            return ScreenRect(this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0)
        }
    }

    companion object {
        fun drawBorder(context: DrawContext, x: Int, y: Int, width: Int, height: Int, argb: Int) {
            context.fill(x, y, x + width, y + 1, argb)
            context.fill(x, y + height - 1, x + width, y + height, argb)
            context.fill(x, y + 1, x + 1, y + height - 1, argb)
            context.fill(x + width - 1, y + 1, x + width, y + height - 1, argb)
        }
    }
}