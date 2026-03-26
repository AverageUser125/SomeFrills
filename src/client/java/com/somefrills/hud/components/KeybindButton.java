package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.ButtonWidget;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fully implemented keybind button that shows the current binding and supports a "listening" mode.
 * When pressed it enters binding mode; the surrounding UI (Settings.keyPressed) will detect that
 * a KeybindButton is binding and will call {@link #bind(int)} with the next key. Listeners can
 * subscribe via {@link #onBound()}.
 */
public class KeybindButton {
    // Public flag checked by Settings.findKeybindButton
    public boolean isBinding = false;

    private int boundKey = GLFW.GLFW_KEY_UNKNOWN;
    private final List<Consumer<Integer>> listeners = new ArrayList<>();
    private ButtonWidget buttonWidget = null;

    public KeybindButton() {}

    // Creates a visible ButtonWidget to add to vanilla/uilib UIs. The caller may call this when
    // laying out the screen; Settings currently calls `this.child(this.button)` so the UI
    // adapter is expected to handle KeybindButton specially or call createButton when rendering.
    public ButtonWidget createButton(int x, int y, int width, int height) {
        this.buttonWidget = new ButtonWidget(x, y, width, height, labelFor(boundKey), btn -> {
            // Enter binding mode: next key press will be captured by Settings and forwarded to bind()
            this.isBinding = true;
            btn.setMessage(Component.literal("Press a key...").withStyle(net.minecraft.ChatFormatting.YELLOW));
        });
        return this.buttonWidget;
    }

    // Called by Settings when a key press should finalize the binding (or by callers to programmatically set)
    public void bind(int key) {
        this.boundKey = key;
        this.isBinding = false;
        if (this.buttonWidget != null) {
            this.buttonWidget.setMessage(labelFor(key));
        }
        // Notify listeners
        for (Consumer<Integer> l : listeners) {
            try { l.accept(key); } catch (Throwable ignored) {}
        }
    }

    // Programmatic utility used by Settings to initialize / reset the shown binding
    public void setBoundKey(int key) {
        this.boundKey = key;
        if (this.buttonWidget != null) this.buttonWidget.setMessage(labelFor(key));
    }

    public int getBoundKey() { return this.boundKey; }

    // Simple event-style accessor used by callers: onBound().subscribe(k -> ...)
    public BoundEvent onBound() { return new BoundEvent(); }

    public class BoundEvent {
        public void subscribe(Consumer<Integer> listener) { listeners.add(listener); }
    }

    private static Component labelFor(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return com.somefrills.config.SettingKeybind.staticGetKeyLabel(key);
        return com.somefrills.config.SettingKeybind.staticGetKeyLabel(key);
    }
}
