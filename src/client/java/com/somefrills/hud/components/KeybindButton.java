package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.ButtonWidget;
import com.somefrills.config.SettingKeybind;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;

public class KeybindButton extends ButtonWidget {
    private SettingKeybind key;
    private boolean waitingForKey = false;

    public KeybindButton(int x, int y, int width, int height, SettingKeybind key) {
        super(x, y, width, height, key.getLabel(), button -> {
            var btn = (KeybindButton) button;
            btn.waitingForKey = true;
            btn.setMessage(Component.literal("Press a key..."));
        }, Button.DEFAULT_NARRATION);
        this.key = key;
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
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
