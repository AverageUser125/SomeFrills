package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.somefrills.misc.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
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

    public double getValue() { return value; }

    public void setValue(double v) { this.value = Math.max(0.0, Math.min(1.0, v)); }

    public void onValueChange(Consumer<Double> cb) { this.onValueChange = cb; }

    private void notifyIfChanged() {
        if (onValueChange == null) return;
        if (Double.isNaN(lastNotified) || Math.abs(value - lastNotified) > 1e-6) {
            lastNotified = value;
            try { onValueChange.accept(value); } catch (Throwable ignored) {}
        }
    }

    // determine if the slider should be vertical
    private boolean isVertical() { return this.height > this.width; }

    // compute normalized value from mouse position
    private double valueFromMouse(double mx, double my) {
        if (isVertical()) {
            double rel = my - this.getY();
            double ratio = rel / Math.max(1, this.height);
            return Math.max(0.0, Math.min(1.0, 1.0 - ratio)); // invert for top = max
        } else {
            double rel = mx - this.getX();
            double ratio = rel / Math.max(1, this.width);
            return Math.max(0.0, Math.min(1.0, ratio));
        }
    }

    @Override
    public boolean mouseClicked(Click event, boolean bl) {
        if (!this.isMouseOver(event.x(), event.y())) return false;
        try {
            Vector2d pos = Utils.getMousePos();
            this.value = valueFromMouse(pos.x, pos.y);
            this.dragging = true;
            notifyIfChanged();
            return true;
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public boolean mouseDragged(Click event, double dx, double dy) {
        if (!dragging) return false;
        try {
            Vector2d pos = Utils.getMousePos();
            this.value = valueFromMouse(pos.x, pos.y);
            notifyIfChanged();
            return true;
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public boolean mouseReleased(Click event) {
        if (!dragging) return false;
        dragging = false;
        notifyIfChanged();
        return true;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, Text.literal(String.format("Slider, value %.0f%%", value * 100)));
    }

    @Override
    protected void renderWidget(DrawContext guiGraphics, int mouseX, int mouseY, float deltaTicks) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        if (isVertical()) {
            // vertical slider track
            int trackX = x + w / 2 - 3;
            guiGraphics.fill(trackX, y, trackX + 6, y + h, 0xff444444);
            // knob
            int knobY = y + (int) Math.round((1.0 - value) * (h - 8));
            guiGraphics.fill(x, knobY, x + w, knobY + 8, 0xffffffff);
        } else {
            // horizontal slider track
            int trackY = y + h / 2 - 3;
            guiGraphics.fill(x, trackY, x + w, trackY + 6, 0xff444444);
            // knob
            int knobX = x + (int) Math.round(value * (w - 8));
            guiGraphics.fill(knobX, y, knobX + 8, y + h, 0xffffffff);
        }
    }
}