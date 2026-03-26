package com.somefrills.hud.components;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

import static com.somefrills.Main.mc;

public class FlatTextbox {
    private final EditBox box;
    private Consumer<String> responder = null;
    private final int width;
    private int maxLength = 256;

    public FlatTextbox(int width) {
        this.box = new EditBox(mc.font, 0,0, width, 20, Component.empty());
        this.width = width;
    }

    public void setValue(String v) { this.box.setValue(v); }
    public String getValue() { return this.box.getValue(); }
    public void setMaxLength(int l) { this.maxLength = l; this.box.setMaxLength(l); }
    public void onChanged(Consumer<String> c) { this.responder = c; }
    public void text(String t) { this.setValue(t); }
    public EditBox getEditBox() { return this.box; }

    /**
     * Return a copy of the internal edit box positioned at the supplied coordinates.
     * This avoids modifying private fields of widgets owned elsewhere.
     */
    public EditBox getEditBoxAt(int x, int y) {
        EditBox eb = new EditBox(mc.font, x, y, this.width, 20, Component.empty());
        eb.setValue(this.box.getValue());
        eb.setMaxLength(this.maxLength);
        // wire responder to the same onChanged behavior
        eb.setResponder(s -> {
            this.box.setValue(s);
            try { if (this.responder != null) this.responder.accept(s); } catch (Throwable ignored) {}
        });
        return eb;
    }
}
