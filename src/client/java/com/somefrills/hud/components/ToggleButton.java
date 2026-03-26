package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.ButtonWidget;
import com.somefrills.config.SettingBool;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ToggleButton extends ButtonWidget {
    private SettingBool state;

    public ToggleButton(int x, int y, int width, int height, SettingBool state) {
        super(x, y, width, height, boolToStr(state.value()), button -> {
            var btn = (ToggleButton) button;
            btn.toggle();
        }, Button.DEFAULT_NARRATION);
        this.state = state;
    }

    private static Component boolToStr(boolean b) {
        return b ? Component.literal("ON") : Component.literal("OFF");
    }

    public void toggle() {
        this.state.set(!this.state.value());
        this.setMessage(boolToStr(this.state.value()));
    }

    public boolean getToggle() {
        return this.state.value();
    }

    public void setToggle(boolean toggle) {
        this.state.set(toggle);
        this.setMessage(boolToStr(toggle));
    }
}
