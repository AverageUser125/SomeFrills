package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.ButtonWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ToggleButton extends ButtonWidget {
    private boolean state;

    public ToggleButton(int x, int y, int width, int height, boolean state) {
        super(x, y, width, height, boolToStr(state), button -> {
            var btn = (ToggleButton) button;
            btn.toggle();
        }, Button.DEFAULT_NARRATION);
        this.state = state;
    }

    private static Component boolToStr(boolean b) {
        return b ? Component.literal("ON") : Component.literal("OFF");
    }

    public void toggle() {
        this.state = !this.state;
        this.setMessage(boolToStr(this.state));
    }

    public boolean getToggle() {
        return this.state;
    }

    public void setToggle(boolean toggle) {
        this.state = toggle;
        this.setMessage(boolToStr(toggle));
    }
}
