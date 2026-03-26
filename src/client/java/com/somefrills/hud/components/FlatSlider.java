package com.somefrills.hud.components;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static com.somefrills.Main.mc;

public class FlatSlider {
    private final EditBox box;
    private double min = 0, max = 100, step = 1;
    private double currentValue = 0;
    private java.util.function.Consumer<Double> changed = null;

    public FlatSlider(int trackColor, int sliderColor) {
        this.box = new EditBox(mc.font, 0,0,80,20, Component.empty());
        this.currentValue = 0;
    }

    public void min(double v) { this.min = v; }
    public void max(double v) { this.max = v; }
    public void stepSize(double s) { this.step = s; }
    public void horizontalSizing(Object o) { /* placeholder */ }
    public void verticalSizing(Object o) { /* placeholder */ }
    public void value(double v) { this.currentValue = v; }
    public double value() { return this.currentValue; }

    public void onChanged(Consumer<Double> c) {
        this.changed = c;
    }

    /**
     * Returns an EditBox positioned at (x,y) that is wired to this slider's value/responder.
     * This avoids accessing private widget fields and lets callers place the control freely.
     */
    public EditBox getEditBoxAt(int x, int y) {
        EditBox eb = new EditBox(mc.font, x, y, 80, 20, Component.empty());
        eb.setValue(String.valueOf(this.currentValue));
        final java.util.concurrent.atomic.AtomicBoolean busy = new java.util.concurrent.atomic.AtomicBoolean(false);
        eb.setResponder(s -> {
            if (busy.get()) return;
            try {
                busy.set(true);
                double v = Double.parseDouble(s);
                v = Math.max(min, Math.min(max, v));
                if (step > 0) v = min + Math.round((v - min) / step) * step;
                this.currentValue = v;
                if (this.changed != null) this.changed.accept(v);
            } catch (Exception ignored) {
            } finally {
                busy.set(false);
            }
        });
        return eb;
    }

    // Backwards-compatible accessor (keeps original instance, at 0,0)
    public EditBox getEditBox() { return box; }
}
