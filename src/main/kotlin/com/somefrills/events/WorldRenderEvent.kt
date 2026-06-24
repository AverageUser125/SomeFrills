package com.somefrills.events

import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.CompareOp
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.somefrills.misc.RenderColor
import com.somefrills.misc.RenderStyle
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.rendertype.*
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.state.level.LevelRenderState
import net.minecraft.network.chat.Component
import net.minecraft.util.LightCoordsUtil
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import com.somefrills.Main.mc
import org.joml.Matrix4f
import org.joml.Vector4f
import kotlin.math.max

class WorldRenderEvent(val camera: CameraRenderState, val matrices: PoseStack, val state: LevelRenderState?) : FrillsEvent() {
    val tickCounter: DeltaTracker = mc.getDeltaTracker()

    fun drawQuad(first: Vec3, second: Vec3, third: Vec3, fourth: Vec3, consumer: VertexConsumer, color: RenderColor) {
        val entry = this.matrices.last()
        val camPos = this.camera.pos
        consumer.addVertex(
            entry,
            (first.x() - camPos.x()).toFloat(),
            (first.y() - camPos.y()).toFloat(),
            (first.z() - camPos.z()).toFloat()
        ).setColor(color.argb)
        consumer.addVertex(
            entry,
            (second.x() - camPos.x()).toFloat(),
            (second.y() - camPos.y()).toFloat(),
            (second.z() - camPos.z()).toFloat()
        ).setColor(color.argb)
        consumer.addVertex(
            entry,
            (third.x() - camPos.x()).toFloat(),
            (third.y() - camPos.y()).toFloat(),
            (third.z() - camPos.z()).toFloat()
        ).setColor(color.argb)
        consumer.addVertex(
            entry,
            (fourth.x() - camPos.x()).toFloat(),
            (fourth.y() - camPos.y()).toFloat(),
            (fourth.z() - camPos.z()).toFloat()
        ).setColor(color.argb)
    }

    fun drawLine(start: Vec3, end: Vec3, width: Float, consumer: VertexConsumer, color: RenderColor) {
        val entry = this.matrices.last()
        val camPos = this.camera.pos
        val vector4f = Vector4f().set(start.x() - camPos.x(), start.y() - camPos.y(), start.z() - camPos.z(), 1.0)
        val vector4f2 = Vector4f().set(end.x() - camPos.x(), end.y() - camPos.y(), end.z() - camPos.z(), 1.0)
        consumer.addVertex(entry, vector4f.x, vector4f.y, vector4f.z)
            .setNormal(entry, vector4f2.x - vector4f.x, vector4f2.y - vector4f.y, vector4f2.z - vector4f.z)
            .setColor(color.argb)
            .setLineWidth(width)
        consumer.addVertex(entry, vector4f2.x, vector4f2.y, vector4f2.z)
            .setNormal(entry, vector4f2.x - vector4f.x, vector4f2.y - vector4f.y, vector4f2.z - vector4f.z)
            .setColor(color.argb)
            .setLineWidth(width)
    }

    fun drawFilled(box: AABB, throughWalls: Boolean, color: RenderColor) {
        val consumer =
            if (throughWalls) immediate.getBuffer(DEBUG_FILLED_BOX_NO_CULL) else immediate.getBuffer(RenderTypes.debugFilledBox())
        val d = box.minX
        val e = box.minY
        val f = box.minZ
        val g = box.maxX
        val h = box.maxY
        val i = box.maxZ
        this.drawQuad(Vec3(g, e, f), Vec3(g, h, f), Vec3(g, h, i), Vec3(g, e, i), consumer, color)
        this.drawQuad(Vec3(d, e, f), Vec3(d, e, i), Vec3(d, h, i), Vec3(d, h, f), consumer, color)
        this.drawQuad(Vec3(d, e, f), Vec3(d, h, f), Vec3(g, h, f), Vec3(g, e, f), consumer, color)
        this.drawQuad(Vec3(d, e, i), Vec3(g, e, i), Vec3(g, h, i), Vec3(d, h, i), consumer, color)
        this.drawQuad(Vec3(d, h, f), Vec3(d, h, i), Vec3(g, h, i), Vec3(g, h, f), consumer, color)
        this.drawQuad(Vec3(d, e, f), Vec3(g, e, f), Vec3(g, e, i), Vec3(d, e, i), consumer, color)
    }

    fun drawOutline(box: AABB, throughWalls: Boolean, color: RenderColor) {
        val consumer =
            if (throughWalls) immediate.getBuffer(LINES_TRANSLUCENT_NO_CULL) else immediate.getBuffer(RenderTypes.lines())
        val d = box.minX
        val e = box.minY
        val f = box.minZ
        val g = box.maxX
        val h = box.maxY
        val i = box.maxZ
        this.drawLine(Vec3(d, e, f), Vec3(g, e, f), 3.0f, consumer, color)
        this.drawLine(Vec3(d, e, f), Vec3(d, h, f), 3.0f, consumer, color)
        this.drawLine(Vec3(d, e, f), Vec3(d, e, i), 3.0f, consumer, color)
        this.drawLine(Vec3(g, e, f), Vec3(g, h, f), 3.0f, consumer, color)
        this.drawLine(Vec3(g, h, f), Vec3(d, h, f), 3.0f, consumer, color)
        this.drawLine(Vec3(d, h, f), Vec3(d, h, i), 3.0f, consumer, color)
        this.drawLine(Vec3(d, h, i), Vec3(d, e, i), 3.0f, consumer, color)
        this.drawLine(Vec3(d, e, i), Vec3(g, e, i), 3.0f, consumer, color)
        this.drawLine(Vec3(g, e, i), Vec3(g, e, f), 3.0f, consumer, color)
        this.drawLine(Vec3(d, h, i), Vec3(g, h, i), 3.0f, consumer, color)
        this.drawLine(Vec3(g, e, i), Vec3(g, h, i), 3.0f, consumer, color)
        this.drawLine(Vec3(g, h, f), Vec3(g, h, i), 3.0f, consumer, color)
    }

    fun drawStyled(
        box: AABB,
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

    fun drawText(pos: Vec3, text: Component, scale: Float, throughWalls: Boolean, color: RenderColor) {
        val matrices = Matrix4f()
        val camPos = this.camera.pos
        val textX = (pos.x() - camPos.x()).toFloat()
        val textY = (pos.y() - camPos.y()).toFloat()
        val textZ = (pos.z() - camPos.z()).toFloat()
        matrices.translate(textX, textY, textZ)
        matrices.rotate(camera.orientation)
        matrices.scale(scale, -scale, scale)
        mc.font.drawInBatch(
            text,
            -mc.font.width(text) / 2f,
            1.0f,
            color.argb,
            true,
            matrices,
            immediate,
            if (throughWalls) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.NORMAL,
            0,
            LightCoordsUtil.FULL_BRIGHT
        )
    }

    fun drawDistanceScaledText(
        pos: Vec3,
        text: Component,
        baseScale: Float,
        scaling: Float,
        throughWalls: Boolean,
        color: RenderColor
    ) {
        val dist = this.camera.pos.distanceTo(pos)
        val distScale = (1 + dist * scaling).toFloat()
        val scale = max(baseScale * distScale, baseScale)
        this.drawText(pos.add(0.0, dist * baseScale, 0.0), text, scale, throughWalls, color)
    }

    fun drawDistanceScaledText(
        pos: Vec3,
        text: Component,
        baseScale: Float,
        throughWalls: Boolean,
        color: RenderColor
    ) {
        this.drawDistanceScaledText(pos, text, baseScale, 0.1f, throughWalls, color)
    }

    fun drawBeam(pos: Vec3, height: Int, throughWalls: Boolean, color: RenderColor) {
        this.drawFilled(AABB.ofSize(pos, 0.5, 0.0, 0.5).expandTowards(0.0, height.toDouble(), 0.0), throughWalls, color)
    }

    fun drawFilledWithBeam(box: AABB, height: Int, throughWalls: Boolean, color: RenderColor) {
        val center = box.getCenter()
        this.drawFilled(box, throughWalls, color)
        this.drawBeam(center.add(0.0, box.maxY - center.y(), 0.0), height, throughWalls, color)
    }

    fun drawTracer(pos: Vec3, width: Float, color: RenderColor) {
        val consumer = immediate.getBuffer(LINES_TRANSLUCENT_NO_CULL)
        val point = this.camera.pos.add(Vec3.directionFromRotation(this.camera.xRot, this.camera.yRot))
        this.drawLine(point, pos, width, consumer, color)
    }

    fun drawTracer(pos: Vec3, color: RenderColor) {
        this.drawTracer(pos, 4.0f, color)
    }

    fun delta(): Float {
        return this.tickCounter.getGameTimeDeltaPartialTick(true)
    }

    fun draw() {
        immediate.endBatch()
    }

    companion object {
        private val immediate = MultiBufferSource.immediate(ByteBufferBuilder(2048))

        private val DEBUG_FILLED_BOX_NO_CULL_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation("pipeline/nofrills_debug_filled_box_no_cull")
                .withDepthStencilState(DepthStencilState(CompareOp.NOT_EQUAL, false))
                .build()
        )
        private val LINES_TRANSLUCENT_NO_CULL_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withDepthStencilState(DepthStencilState(CompareOp.NOT_EQUAL, false))
                .withLocation("pipeline/nofrills_lines_translucent_no_cull")
                .build()
        )
        private val DEBUG_FILLED_BOX_NO_CULL = RenderType.create(
            "nofrills_debug_filled_box_no_cull",
            RenderSetup.builder(DEBUG_FILLED_BOX_NO_CULL_PIPELINE)
                .sortOnUpload()
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .createRenderSetup()
        )
        private val LINES_TRANSLUCENT_NO_CULL = RenderType.create(
            "nofrills_lines_translucent_no_cull",
            RenderSetup.builder(LINES_TRANSLUCENT_NO_CULL_PIPELINE)
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                .createRenderSetup()
        )
    }
}