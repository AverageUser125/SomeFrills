package com.somefrills.events

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.somefrills.Main.mc
import com.somefrills.misc.RenderColor
import com.somefrills.misc.RenderStyle
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.*
import net.minecraft.client.render.VertexConsumerProvider.Immediate
import net.minecraft.client.render.WorldRenderer.Gizmos
import net.minecraft.client.render.state.WorldRenderState
import net.minecraft.client.util.BufferAllocator
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.joml.Vector4f

class WorldRenderEvent(
    @JvmField var tickCounter: RenderTickCounter?,
    @JvmField var camera: Camera,
    var matrices: MatrixStack,
    var gizmos: Gizmos,
    var state: WorldRenderState
) {
    private fun drawQuad(
        first: Vec3d,
        second: Vec3d,
        third: Vec3d,
        fourth: Vec3d,
        consumer: VertexConsumer,
        color: RenderColor
    ) {
        val entry = this.matrices.peek()
        val camPos = this.camera.cameraPos
        consumer.vertex(
            entry,
            (first.getX() - camPos.getX()).toFloat(),
            (first.getY() - camPos.getY()).toFloat(),
            (first.getZ() - camPos.getZ()).toFloat()
        ).color(color.argb)
        consumer.vertex(
            entry,
            (second.getX() - camPos.getX()).toFloat(),
            (second.getY() - camPos.getY()).toFloat(),
            (second.getZ() - camPos.getZ()).toFloat()
        ).color(color.argb)
        consumer.vertex(
            entry,
            (third.getX() - camPos.getX()).toFloat(),
            (third.getY() - camPos.getY()).toFloat(),
            (third.getZ() - camPos.getZ()).toFloat()
        ).color(color.argb)
        consumer.vertex(
            entry,
            (fourth.getX() - camPos.getX()).toFloat(),
            (fourth.getY() - camPos.getY()).toFloat(),
            (fourth.getZ() - camPos.getZ()).toFloat()
        ).color(color.argb)
    }

    private fun drawLine(start: Vec3d, end: Vec3d, width: Float, consumer: VertexConsumer, color: RenderColor) {
        val entry = this.matrices.peek()
        val camPos = this.camera.cameraPos
        val vector4f = Vector4f()
        val vector4f2 = Vector4f()
        vector4f.set(start.getX() - camPos.getX(), start.getY() - camPos.getY(), start.getZ() - camPos.getZ(), 1.0)
        vector4f2.set(end.getX() - camPos.getX(), end.getY() - camPos.getY(), end.getZ() - camPos.getZ(), 1.0)
        consumer.vertex(entry, vector4f.x, vector4f.y, vector4f.z)
            .normal(entry, vector4f2.x - vector4f.x, vector4f2.y - vector4f.y, vector4f2.z - vector4f.z)
            .color(color.argb)
            .lineWidth(width)
        consumer.vertex(entry, vector4f2.x, vector4f2.y, vector4f2.z)
            .normal(entry, vector4f2.x - vector4f.x, vector4f2.y - vector4f.y, vector4f2.z - vector4f.z)
            .color(color.argb)
            .lineWidth(width)
    }

    fun drawFilled(box: Box, throughWalls: Boolean, color: RenderColor) {
        val consumer: VertexConsumer =
            if (throughWalls) immediate.getBuffer(DEBUG_FILLED_BOX_NO_CULL) else immediate.getBuffer(RenderLayers.debugFilledBox())
        val d = box.minX
        val e = box.minY
        val f = box.minZ
        val g = box.maxX
        val h = box.maxY
        val i = box.maxZ
        this.drawQuad(Vec3d(g, e, f), Vec3d(g, h, f), Vec3d(g, h, i), Vec3d(g, e, i), consumer, color)
        this.drawQuad(Vec3d(d, e, f), Vec3d(d, e, i), Vec3d(d, h, i), Vec3d(d, h, f), consumer, color)
        this.drawQuad(Vec3d(d, e, f), Vec3d(d, h, f), Vec3d(g, h, f), Vec3d(g, e, f), consumer, color)
        this.drawQuad(Vec3d(d, e, i), Vec3d(g, e, i), Vec3d(g, h, i), Vec3d(d, h, i), consumer, color)
        this.drawQuad(Vec3d(d, h, f), Vec3d(d, h, i), Vec3d(g, h, i), Vec3d(g, h, f), consumer, color)
        this.drawQuad(Vec3d(d, e, f), Vec3d(g, e, f), Vec3d(g, e, i), Vec3d(d, e, i), consumer, color)
    }

    fun drawOutline(box: Box, throughWalls: Boolean, color: RenderColor) {
        val consumer: VertexConsumer =
            if (throughWalls) immediate.getBuffer(LINES_TRANSLUCENT_NO_CULL) else immediate.getBuffer(RenderLayers.lines())
        val d = box.minX
        val e = box.minY
        val f = box.minZ
        val g = box.maxX
        val h = box.maxY
        val i = box.maxZ
        this.drawLine(Vec3d(d, e, f), Vec3d(g, e, f), 3.0f, consumer, color)
        this.drawLine(Vec3d(d, e, f), Vec3d(d, h, f), 3.0f, consumer, color)
        this.drawLine(Vec3d(d, e, f), Vec3d(d, e, i), 3.0f, consumer, color)
        this.drawLine(Vec3d(g, e, f), Vec3d(g, h, f), 3.0f, consumer, color)
        this.drawLine(Vec3d(g, h, f), Vec3d(d, h, f), 3.0f, consumer, color)
        this.drawLine(Vec3d(d, h, f), Vec3d(d, h, i), 3.0f, consumer, color)
        this.drawLine(Vec3d(d, h, i), Vec3d(d, e, i), 3.0f, consumer, color)
        this.drawLine(Vec3d(d, e, i), Vec3d(g, e, i), 3.0f, consumer, color)
        this.drawLine(Vec3d(g, e, i), Vec3d(g, e, f), 3.0f, consumer, color)
        this.drawLine(Vec3d(d, h, i), Vec3d(g, h, i), 3.0f, consumer, color)
        this.drawLine(Vec3d(g, e, i), Vec3d(g, h, i), 3.0f, consumer, color)
        this.drawLine(Vec3d(g, h, f), Vec3d(g, h, i), 3.0f, consumer, color)
    }

    fun drawStyled(
        box: Box,
        style: RenderStyle,
        throughWalls: Boolean,
        outlineColor: RenderColor,
        filledColor: RenderColor
    ) {
        if (style != RenderStyle.Outline) {
            this.drawFilled(box, throughWalls, filledColor)
        }
        if (style != RenderStyle.Filled) {
            this.drawOutline(box, throughWalls, outlineColor)
        }
    }

    fun drawText(pos: Vec3d, text: Text?, scale: Float, throughWalls: Boolean, color: RenderColor) {
        if (mc.textRenderer == null) return
        val matrices = Matrix4f()
        val camPos = this.camera.cameraPos
        val textX = (pos.getX() - camPos.getX()).toFloat()
        val textY = (pos.getY() - camPos.getY()).toFloat()
        val textZ = (pos.getZ() - camPos.getZ()).toFloat()
        matrices.translate(textX, textY, textZ)
        matrices.rotate(camera.rotation)
        matrices.scale(scale, -scale, scale)
        mc.textRenderer.draw(
            text,
            -mc.textRenderer.getWidth(text) / 2f,
            1.0f,
            color.argb,
            true,
            matrices,
            immediate,
            if (throughWalls) TextRenderer.TextLayerType.SEE_THROUGH else TextRenderer.TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        )
    }

    fun drawBeam(pos: Vec3d, height: Int, throughWalls: Boolean, color: RenderColor) {
        this.drawFilled(Box.of(pos, 0.5, 0.0, 0.5).stretch(0.0, height.toDouble(), 0.0), throughWalls, color)
    }

    fun drawFilledWithBeam(box: Box, height: Int, throughWalls: Boolean, color: RenderColor) {
        val center = box.center
        this.drawFilled(box, throughWalls, color)
        this.drawBeam(center.add(0.0, box.maxY - center.getY(), 0.0), height, throughWalls, color)
    }

    fun drawTracer(pos: Vec3d, width: Float, color: RenderColor) {
        val consumer: VertexConsumer = immediate.getBuffer(LINES_TRANSLUCENT_NO_CULL)
        val point = this.camera.cameraPos.add(Vec3d.fromPolar(this.camera.pitch, this.camera.yaw))
        this.drawLine(point, pos, width, consumer, color)
    }

    fun drawTracer(pos: Vec3d, color: RenderColor) {
        this.drawTracer(pos, 4.0f, color)
    }

    companion object {
        @JvmField
        val immediate: Immediate = VertexConsumerProvider.immediate(BufferAllocator(2048))

        private val DEBUG_FILLED_BOX_NO_CULL_PIPELINE: RenderPipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                .withLocation("pipeline/somefrills_debug_filled_box_no_cull")
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build()
        )
        private val LINES_TRANSLUCENT_NO_CULL_PIPELINE: RenderPipeline = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
                .withDepthWrite(false)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withLocation("pipeline/somefrills_lines_translucent_no_cull")
                .build()
        )
        private val DEBUG_FILLED_BOX_NO_CULL: RenderLayer = RenderLayer.of(
            "somefrills_debug_filled_box_no_cull",
            RenderSetup.builder(DEBUG_FILLED_BOX_NO_CULL_PIPELINE)
                .translucent()
                .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .build()
        )
        private val LINES_TRANSLUCENT_NO_CULL: RenderLayer = RenderLayer.of(
            "somefrills_lines_translucent_no_cull",
            RenderSetup.builder(LINES_TRANSLUCENT_NO_CULL_PIPELINE)
                .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .outputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                .build()
        )
    }
}