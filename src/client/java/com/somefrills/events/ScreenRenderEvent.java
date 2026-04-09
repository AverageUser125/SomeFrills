package com.somefrills.events;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.somefrills.misc.RenderColor;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.joml.Matrix3x2f;
import org.joml.Vector2d;

import java.util.Optional;

import static com.somefrills.Main.mc;

public class ScreenRenderEvent {
    public DrawContext context;
    public int mouseX;
    public int mouseY;
    public float deltaTicks;
    public String title;
    public ScreenHandler handler;
    public Slot focusedSlot;

    public ScreenRenderEvent(DrawContext context, int mouseX, int mouseY, float deltaTicks, String title, ScreenHandler handler, Slot focusedSlot) {
        this.context = context;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.deltaTicks = deltaTicks;
        this.title = title;
        this.handler = handler;
        this.focusedSlot = focusedSlot;
    }

    private Optional<Slot> getSlot(int slotId) {
        if (slotId < 0 || slotId >= this.handler.slots.size()) {
            return Optional.empty();
        }
        return Optional.of(this.handler.getSlot(slotId));
    }

    public void drawLine(int firstId, int secondId, double width, RenderColor color) {
        Optional<Slot> slot1 = this.getSlot(firstId);
        Optional<Slot> slot2 = this.getSlot(secondId);
        if (slot1.isPresent() && slot2.isPresent()) {
            Slot first = slot1.get();
            Slot second = slot2.get();
            this.drawLine(RenderPipelines.GUI, first.x + 8, first.y + 8, second.x + 8, second.y + 8, width, RenderColor.ofArgb(color.argb));
        }
    }

    public void drawLine(RenderPipeline pipeline, int x1, int y1, int x2, int y2, double width, RenderColor color) {
        this.context.state.addSimpleElement(new LineElementRenderState(
                pipeline,
                new Matrix3x2f(context.getMatrices()),
                context.scissorStack.peekLast(),
                x1, y1, x2, y2,
                width,
                color
        ));
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int argb) {
        context.fill(x, y, x + width, y + 1, argb);
        context.fill(x, y + height - 1, x + width, y + height, argb);
        context.fill(x, y + 1, x + 1, y + height - 1, argb);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, argb);
    }

    public void drawBorder(int slotId, RenderColor color) {
        this.getSlot(slotId).ifPresent(slot -> {
            drawBorder(this.context, slot.x, slot.y, 16, 16, color.argb);
        });
    }

    public void drawLabel(int slotId, Text text) {
        this.getSlot(slotId).ifPresent(slot -> this.context.drawCenteredTextWithShadow(mc.textRenderer, text, slot.x + 8, slot.y + 4, RenderColor.white.argb));
    }

    public void drawFill(int slotId, RenderColor color) {
        this.getSlot(slotId).ifPresent(slot -> this.context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color.argb));
    }

    public static class Before extends ScreenRenderEvent {
        public Before(DrawContext context, int mouseX, int mouseY, float deltaTicks, String title, ScreenHandler handler, Slot focusedSlot) {
            super(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot);
        }
    }

    public static class After extends ScreenRenderEvent {
        public After(DrawContext context, int mouseX, int mouseY, float deltaTicks, String title, ScreenHandler handler, Slot focusedSlot) {
            super(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot);
        }
    }

    public record LineElementRenderState(
            RenderPipeline pipeline,
            Matrix3x2f pose,
            ScreenRect scissorArea,
            int x0,
            int y0,
            int x1,
            int y1,
            double thiccness,
            RenderColor color
    ) implements SimpleGuiElementRenderState {
        public void setupVertices(VertexConsumer vertices) {
            var offset = new Vector2d(this.x1 - this.x0, this.y1 - this.y0).perpendicular().normalize().mul(this.thiccness * .5d);

            int vColor = this.color.argb;
            vertices.vertex(this.pose, (float) (x0 + offset.x), (float) (y0 + offset.y)).color(vColor);
            vertices.vertex(this.pose, (float) (x0 - offset.x), (float) (y0 - offset.y)).color(vColor);
            vertices.vertex(this.pose, (float) (x1 - offset.x), (float) (y1 - offset.y)).color(vColor);
            vertices.vertex(this.pose, (float) (x1 + offset.x), (float) (y1 + offset.y)).color(vColor);
        }

        public TextureSetup textureSetup() {
            return TextureSetup.empty();
        }

        public ScreenRect bounds() {
            return new ScreenRect(this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0);
        }
    }
}