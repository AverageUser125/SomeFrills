package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.ButtonWidget;
import com.somefrills.config.SettingKeybind;
import net.minecraft.client.input.CharInput;
import net.minecraft.text.Text;

public class KeybindButton extends ButtonWidget {
    private SettingKeybind key;
    private boolean waitingForKey = false;

    public KeybindButton(int x, int y, int width, int height, SettingKeybind key) {
        super(x, y, width, height, key.getLabel(), button -> {
            var btn = (KeybindButton) button;
            btn.waitingForKey = true;
            btn.setMessage(net.minecraft.text.Text.literal("Press a key..."));
        }, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
        this.key = key;
    }

    @Override
    public boolean charTyped(CharInput characterEvent) {
        if (!waitingForKey) return false;
        int keyCode = characterEvent.codepoint();
        setKey(keyCode);
        waitingForKey = false;
        return true;
    }

    private void setKey(int keyCode) {
        key.set(keyCode);
        this.setMessage(key.getLabel());
    }

    public int getKey() {
        return key.value();
    }
}
