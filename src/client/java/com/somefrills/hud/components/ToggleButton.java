package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.ButtonWidget;
import com.somefrills.config.SettingBool;
import net.minecraft.text.Text;

public class ToggleButton extends ButtonWidget {
    private SettingBool state;

    public ToggleButton(int x, int y, int width, int height, SettingBool state) {
        super(x, y, width, height, boolToStr(state.value()), button -> {
            var btn = (ToggleButton) button;
            btn.toggle();
        }, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.state = state;
    }

    private static net.minecraft.text.Text boolToStr(boolean b) {
        return b ? net.minecraft.text.Text.literal("ON") : net.minecraft.text.Text.literal("OFF");
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
