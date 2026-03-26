package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

import java.util.function.Consumer;

public class SliderWidget extends ClickableWidget implements IWidget {
    private double value = 0.0; // normalized 0..1
    private boolean dragging = false;
    private Consumer<Double> onValueChange = null;
    private double lastNotified = Double.NaN;

    public SliderWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
    }

    public double getValue() {
        return value;
    }

    public void setValue(double v) {
        this.value = Math.max(0.0, Math.min(1.0, v));
    }

    public void onValueChange(Consumer<Double> cb) {
        this.onValueChange = cb;
    }

    private void notifyIfChanged() {
        if (onValueChange == null) return;
        if (Double.isNaN(lastNotified) || Math.abs(value - lastNotified) > 1e-6) {
            lastNotified = value;
            try {
                onValueChange.accept(value);
            } catch (Throwable ignored) {
            }
        }
    }

    // compute normalized value from absolute mouse X
    private double valueFromMouse(double mx) {
        double rel = mx - this.getX();
        double ratio = rel / Math.max(1, this.width);
        if (ratio < 0) ratio = 0;
        if (ratio > 1) ratio = 1;
        return ratio;
    }

    @Override
    public boolean mouseClicked(Click event, boolean bl) {
        // rely on this.isMouseOver() per your instruction
        if (!this.isMouseOver(event.x(), event.y())) return false;
        try {
            Vector2d pos = Utils.getMousePos();
            this.value = valueFromMouse(pos.x);
            this.dragging = true;
            notifyIfChanged();
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public boolean mouseReleased(Click mouseButtonEvent) {
        if (!this.dragging) return false;
        this.dragging = false;
        notifyIfChanged();
        return true;
    }

    @Override
    public boolean mouseDragged(Click mouseButtonEvent, double dx, double dy) {
        if (!this.dragging) return false;
        try {
            Vector2d pos = Utils.getMousePos();
            this.value = valueFromMouse(pos.x);
            notifyIfChanged();
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, Text.literal(String.format("Slider, value %.0f%%", value * 100)));
    }

    @Override
    protected void renderWidget(DrawContext guiGraphics, int i, int j, float f) {
        int trackY = this.getY() + this.height / 2 - 3;
        guiGraphics.fill(this.getX(), trackY, this.getX() + this.width, trackY + 6, 0xff444444);
        // knob
        int kx = this.getX() + (int) Math.round(this.value * (this.width - 8));
        int ky = this.getY() + 3;
        guiGraphics.fill(kx, ky, kx + 8, ky + this.height - 6, 0xffffffff);
    }

}
