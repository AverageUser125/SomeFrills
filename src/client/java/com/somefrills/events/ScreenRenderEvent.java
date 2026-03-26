package com.somefrills.events;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.somefrills.misc.RenderColor;
import com.somefrills.misc.Rendering;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.joml.Matrix3x2f;
import org.joml.Vector2d;

import java.util.Optional;

import static com.somefrills.Main.mc;

public class ScreenRenderEvent {
    public GuiGraphics context;
    public int mouseX;
    public int mouseY;
    public float deltaTicks;
    public String title;
    public AbstractContainerMenu handler;
    public Slot focusedSlot;

    public ScreenRenderEvent(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, String title, AbstractContainerMenu handler, Slot focusedSlot) {
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
            this.drawLine(RenderPipelines.GUI, first.x + 8, first.y + 8, second.x + 8, second.y + 8, width, color);
        }
    }

    public void drawLine(RenderPipeline pipeline, int x1, int y1, int x2, int y2, double width, RenderColor color) {
        this.context.guiRenderState.submitGuiElement(new LineElementRenderState(
                pipeline,
                new Matrix3x2f(context.pose()),
                context.scissorStack.peek(),
                x1, y1, x2, y2,
                width,
                color
        ));
    }

    public void drawBorder(int slotId, RenderColor color) {
        this.getSlot(slotId).ifPresent(slot -> Rendering.drawBorder(this.context, slot.x, slot.y, 16, 16, color.argb));
    }

    public void drawLabel(int slotId, Component text) {
        this.getSlot(slotId).ifPresent(slot -> this.context.drawCenteredString(mc.font, text, slot.x + 8, slot.y + 4, RenderColor.white.argb));
    }

    public void drawFill(int slotId, RenderColor color) {
        this.getSlot(slotId).ifPresent(slot -> this.context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color.argb));
    }

    public static class Before extends ScreenRenderEvent {
        public Before(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, String title, AbstractContainerMenu handler, Slot focusedSlot) {
            super(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot);
        }
    }

    public static class After extends ScreenRenderEvent {
        public After(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, String title, AbstractContainerMenu handler, Slot focusedSlot) {
            super(context, mouseX, mouseY, deltaTicks, title, handler, focusedSlot);
        }
    }

    public record LineElementRenderState(
            RenderPipeline pipeline,
            Matrix3x2f pose,
            ScreenRectangle scissorArea,
            int x0,
            int y0,
            int x1,
            int y1,
            double thiccness,
            RenderColor color
    ) implements GuiElementRenderState {
        @Override
        public void buildVertices(VertexConsumer vertices) {
            var offset = new Vector2d(this.x1 - this.x0, this.y1 - this.y0).perpendicular().normalize().mul(this.thiccness * .5d);

            int vColor = color.argb;
            vertices.addVertexWith2DPose(this.pose, (float) (x0 + offset.x), (float) (y0 + offset.y)).setColor(vColor);
            vertices.addVertexWith2DPose(this.pose, (float) (x0 - offset.x), (float) (y0 - offset.y)).setColor(vColor);
            vertices.addVertexWith2DPose(this.pose, (float) (x1 - offset.x), (float) (y1 - offset.y)).setColor(vColor);
            vertices.addVertexWith2DPose(this.pose, (float) (x1 + offset.x), (float) (y1 + offset.y)).setColor(vColor);
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }

        @Override
        public ScreenRectangle bounds() {
            return new ScreenRectangle(this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0);
        }
    }
}
