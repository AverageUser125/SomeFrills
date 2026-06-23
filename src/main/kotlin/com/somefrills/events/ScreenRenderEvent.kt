package com.somefrills.events

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import com.somefrills.Main.mc
import com.somefrills.misc.RenderColor
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import org.joml.Matrix3x2f
import org.joml.Vector2d
import java.util.*
import java.util.function.Consumer


open class ScreenRenderEvent(
    var context: GuiGraphicsExtractor,
    var mouseX: Int,
    var mouseY: Int,
    var deltaTicks: Float,
    var title: String?,
    var handler: AbstractContainerMenu,
    var focusedSlot: Slot?
) {
    private fun getSlot(slotId: Int): Optional<Slot> {
        if (slotId < 0 || slotId >= this.handler.slots.size) {
            return Optional.empty<Slot>()
        }
        return Optional.of<Slot>(this.handler.getSlot(slotId))
    }

    fun drawLine(firstId: Int, secondId: Int, width: Double, color: RenderColor) {
        val slot1 = this.getSlot(firstId)
        val slot2 = this.getSlot(secondId)
        if (slot1.isPresent() && slot2.isPresent()) {
            val first = slot1.get()
            val second = slot2.get()
            this.drawLine(
                RenderPipelines.GUI,
                first.x + 8,
                first.y + 8,
                second.x + 8,
                second.y + 8,
                width,
                RenderColor.ofArgb(color.argb)
            )
        }
    }

    fun drawLine(pipeline: RenderPipeline?, x1: Int, y1: Int, x2: Int, y2: Int, width: Double, color: RenderColor) {
        this.context.guiRenderState.addGuiElement(
            LineElementRenderState(
                pipeline,
                Matrix3x2f(context.pose()),
                context.scissorStack.peek(),
                x1, y1, x2, y2,
                width,
                color
            )
        )
    }

    fun drawBorder(context: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, color: RenderColor) {
        drawBorder(context, x, y, width, height, color.argb)
    }

    fun drawBorder(context: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, argb: Int) {
        context.fill(x, y, x + width, y + 1, argb)
        context.fill(x, y + height - 1, x + width, y + height, argb)
        context.fill(x, y + 1, x + 1, y + height - 1, argb)
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, argb)
    }

    fun drawBorder(slotId: Int, color: RenderColor) {
        this.getSlot(slotId).ifPresent(Consumer { slot: Slot? ->
            drawBorder(
                this.context,
                slot!!.x,
                slot.y,
                16,
                16,
                color.argb
            )
        })
    }

    fun drawLabel(slotId: Int, text: Component?) {
        this.getSlot(slotId).ifPresent(Consumer { slot: Slot? ->
            this.context.centeredText(
                mc.font,
                text!!,
                slot!!.x + 8,
                slot.y + 4,
                RenderColor.white.argb
            )
        })
    }

    fun drawFill(slotId: Int, color: RenderColor) {
        this.getSlot(slotId).ifPresent(Consumer { slot: Slot? ->
            this.context.fill(
                slot!!.x,
                slot.y,
                slot.x + 16,
                slot.y + 16,
                color.argb
            )
        })
    }

    class Before(
        context: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
        title: String?,
        handler: AbstractContainerMenu,
        focusedSlot: Slot?
    ) : ScreenRenderEvent(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot)

    class After(
        context: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
        title: String?,
        handler: AbstractContainerMenu,
        focusedSlot: Slot?
    ) : ScreenRenderEvent(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot)

    data class LineElementRenderState(
        val pipeline: RenderPipeline?,
        val pose: Matrix3x2f?,
        val scissorArea: ScreenRectangle?,
        val x0: Int,
        val y0: Int,
        val x1: Int,
        val y1: Int,
        val thiccness: Double,
        val color: RenderColor
    ) : GuiElementRenderState {
        override fun buildVertices(vertices: VertexConsumer) {
            val offset =
                Vector2d((this.x1 - this.x0).toDouble(), (this.y1 - this.y0).toDouble()).perpendicular().normalize()
                    .mul(this.thiccness * .5)

            val vColor: Int = this.color.argb
            vertices.addVertexWith2DPose(this.pose!!, (x0 + offset.x).toFloat(), (y0 + offset.y).toFloat())
                .setColor(vColor)
            vertices.addVertexWith2DPose(this.pose, (x0 - offset.x).toFloat(), (y0 - offset.y).toFloat())
                .setColor(vColor)
            vertices.addVertexWith2DPose(this.pose, (x1 - offset.x).toFloat(), (y1 - offset.y).toFloat())
                .setColor(vColor)
            vertices.addVertexWith2DPose(this.pose, (x1 + offset.x).toFloat(), (y1 + offset.y).toFloat())
                .setColor(vColor)
        }

        override fun pipeline(): RenderPipeline {
            TODO("Not yet implemented")
        }

        override fun textureSetup(): TextureSetup {
            return TextureSetup.noTexture()
        }

        override fun scissorArea(): ScreenRectangle? {
            TODO("Not yet implemented")
        }

        override fun bounds(): ScreenRectangle {
            return ScreenRectangle(this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0)
        }
    }
}