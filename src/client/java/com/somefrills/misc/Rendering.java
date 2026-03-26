package com.somefrills.misc;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.OptionalDouble;

import static com.somefrills.Main.mc;

public final class Rendering {
    /**
     * Draws a filled box for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawFilled(PoseStack matrices, MultiBufferSource.BufferSource consumer, Camera camera, AABB box, boolean throughWalls, RenderColor color) {
        matrices.pushPose();
        Vec3 camPos = camera.getPosition().reverse();
        matrices.translate(camPos.x, camPos.y, camPos.z);
        VertexConsumer buffer = throughWalls ? consumer.getBuffer(Layers.BoxFilledNoCull) : consumer.getBuffer(Layers.BoxFilled);
        ShapeRenderer.addChainedFilledBoxVertices(matrices, buffer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color.r, color.g, color.b, color.a);
        matrices.popPose();
    }

    /**
     * Draws an outline box for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawOutline(PoseStack matrices, MultiBufferSource.BufferSource consumer, Camera camera, AABB box, boolean throughWalls, RenderColor color) {
        matrices.pushPose();
        Vec3 camPos = camera.getPosition().reverse();
        matrices.translate(camPos.x, camPos.y, camPos.z);
        VertexConsumer buffer = throughWalls ? consumer.getBuffer(Layers.BoxOutlineNoCull) : consumer.getBuffer(Layers.BoxOutline);
        ShapeRenderer.renderLineBox(matrices.last(), buffer, box, color.r, color.g, color.b, color.a);
        matrices.popPose();
    }

    /**
     * Draws text within the world for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawText(MultiBufferSource.BufferSource consumer, Camera camera, Vec3 pos, Component text, float scale, boolean throughWalls, RenderColor color) {
        Matrix4f matrices = new Matrix4f();
        Vec3 camPos = camera.getPosition();
        float textX = (float) (pos.x() - camPos.x());
        float textY = (float) (pos.y() - camPos.y());
        float textZ = (float) (pos.z() - camPos.z());
        matrices.translate(textX, textY, textZ);
        matrices.rotate(camera.rotation());
        matrices.scale(scale, -scale, scale);
        mc.font.drawInBatch(text, -mc.font.width(text) / 2f, 1.0f, color.argb, true, matrices, consumer, throughWalls ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    }

    /**
     * Draws a simulated beacon beam for the current frame. Automatically performs the required matrix stack translation.
     */
    public static void drawBeam(PoseStack matrices, MultiBufferSource.BufferSource consumer, Camera camera, Vec3 pos, int height, boolean throughWalls, RenderColor color) {
        drawFilled(matrices, consumer, camera, AABB.ofSize(pos, 0.5, 0, 0.5).expandTowards(0, height, 0), throughWalls, color);
    }

    /**
     * Draws a tracer going from the center of the screen to the provided coordinate. Automatically performs the required matrix stack translation.
     */
    public static void drawTracer(PoseStack matrices, MultiBufferSource.BufferSource consumer, Camera camera, Vec3 pos, RenderColor color) {
        Vec3 camPos = camera.getPosition();
        matrices.pushPose();
        matrices.translate(-camPos.x(), -camPos.y(), -camPos.z());
        PoseStack.Pose entry = matrices.last();
        VertexConsumer buffer = consumer.getBuffer(Layers.GuiLine);
        Vec3 point = camPos.add(Vec3.directionFromRotation(camera.getXRot(), camera.getYRot())); // taken from Skyblocker's RenderHelper, my brain cannot handle OpenGL
        Vector3f normal = pos.toVector3f().sub((float) point.x(), (float) point.y(), (float) point.z()).normalize(new Vector3f(1.0f, 1.0f, 1.0f));
        buffer.addVertex(entry, (float) point.x(), (float) point.y(), (float) point.z()).setColor(color.r, color.g, color.b, color.a).setNormal(entry, normal);
        buffer.addVertex(entry, (float) pos.x(), (float) pos.y(), (float) pos.z()).setColor(color.r, color.g, color.b, color.a).setNormal(entry, normal);
        matrices.popPose();
    }

    public static void drawBorder(GuiGraphics context, int x, int y, int width, int height, RenderColor color) {
        drawBorder(context, x, y, width, height, color.argb);
    }

    public static void drawBorder(GuiGraphics context, int x, int y, int width, int height, int argb) {
        context.fill(x, y, x + width, y + 1, argb);
        context.fill(x, y + height - 1, x + width, y + height, argb);
        context.fill(x, y + 1, x + 1, y + height - 1, argb);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, argb);
    }

    public static class Pipelines {
        public static final RenderPipeline.Snippet filledSnippet = RenderPipelines.DEBUG_FILLED_SNIPPET;
        public static final RenderPipeline.Snippet outlineSnippet = RenderPipelines.LINES_SNIPPET;

        public static final RenderPipeline filledNoCull = RenderPipelines.register(RenderPipeline.builder(filledSnippet)
                .withLocation(ResourceLocation.fromNamespaceAndPath("com.somefrills", "pipeline/com.somefrills_filled_no_cull"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build());
        public static final RenderPipeline filledCull = RenderPipelines.register(RenderPipeline.builder(filledSnippet)
                .withLocation(ResourceLocation.fromNamespaceAndPath("com.somefrills", "pipeline/com.somefrills_filled_cull"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                .build());
        public static final RenderPipeline outlineNoCull = RenderPipelines.register(RenderPipeline.builder(outlineSnippet)
                .withLocation(ResourceLocation.fromNamespaceAndPath("com.somefrills", "pipeline/com.somefrills_outline_no_cull"))
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build());
        public static final RenderPipeline outlineCull = RenderPipelines.register(RenderPipeline.builder(outlineSnippet)
                .withLocation(ResourceLocation.fromNamespaceAndPath("com.somefrills", "pipeline/com.somefrills_outline_cull"))
                .build());
        public static final RenderPipeline lineNoCull = RenderPipelines.register(RenderPipeline.builder(outlineSnippet)
                .withLocation(ResourceLocation.fromNamespaceAndPath("com.somefrills", "pipeline/com.somefrills_line_no_cull"))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP)
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .build());
    }

    public static class Parameters {
        public static final RenderType.CompositeState.CompositeStateBuilder filled = RenderType.CompositeState.builder()
                .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING);
        public static final RenderType.CompositeState.CompositeStateBuilder lines = RenderType.CompositeState.builder()
                .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3.0)));
    }

    public static class Layers {
        public static final RenderType.CompositeRenderType BoxFilled = RenderType.create(
                "com.somefrills_box_filled",
                RenderType.TRANSIENT_BUFFER_SIZE,
                false,
                true,
                Pipelines.filledCull,
                Parameters.filled.createCompositeState(false)
        );
        public static final RenderType.CompositeRenderType BoxFilledNoCull = RenderType.create(
                "com.somefrills_box_filled_no_cull",
                RenderType.TRANSIENT_BUFFER_SIZE,
                false,
                true,
                Pipelines.filledNoCull,
                Parameters.filled.createCompositeState(false)
        );
        public static final RenderType.CompositeRenderType BoxOutline = RenderType.create(
                "com.somefrills_box_outline",
                RenderType.TRANSIENT_BUFFER_SIZE,
                false,
                false,
                Pipelines.outlineCull,
                Parameters.lines.createCompositeState(false)
        );
        public static final RenderType.CompositeRenderType BoxOutlineNoCull = RenderType.create(
                "com.somefrills_box_outline_no_cull",
                RenderType.TRANSIENT_BUFFER_SIZE,
                false,
                false,
                Pipelines.outlineNoCull,
                Parameters.lines.createCompositeState(false)
        );
        public static final RenderType.CompositeRenderType GuiLine = RenderType.create(
                "com.somefrills_gui_line",
                RenderType.TRANSIENT_BUFFER_SIZE,
                false,
                false,
                Pipelines.lineNoCull,
                Parameters.lines.createCompositeState(false)
        );
    }
}
