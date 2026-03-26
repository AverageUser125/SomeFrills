package com.somefrills.hud.components;

import net.minecraft.network.chat.Component;
import com.daqem.uilib.gui.widget.ButtonWidget;
import java.util.ArrayList;
import java.util.List;

public class ToggleButton {
    private boolean toggle;
    private final List<ToggleChanged> listeners = new ArrayList<>();
    private ButtonWidget widget = null;

    public ToggleButton(boolean initial) {
        this.toggle = initial;
    }

    public ButtonWidget createButton(int x, int y, int width, int height) {
        this.widget = new ButtonWidget(x, y, width, height, Component.literal(this.toggle ? "Enabled" : "Disabled"), (b) -> {
            this.setToggle(!this.toggle);
        });
        return this.widget;
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
        if (this.widget != null) this.widget.setMessage(Component.literal(this.toggle ? "Enabled" : "Disabled"));
        for (ToggleChanged l : listeners) l.onToggle(toggle);
    }

    public void addListener(ToggleChanged l) { listeners.add(l); }

    public interface ToggleChanged { void onToggle(boolean newValue); }
}
