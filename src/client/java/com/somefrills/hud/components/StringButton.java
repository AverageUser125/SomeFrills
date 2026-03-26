package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.daqem.uilib.gui.widget.EditBoxWidget;
import com.somefrills.config.SettingString;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.somefrills.Main.mc;

public class StringButton extends EditBoxWidget implements IWidget {
    private final SettingString setting;
    public StringButton(int x, int y, int width, int height, SettingString setting) {
        super(mc.font, x, y, width, height, Component.literal(setting.value()));
        this.setting = setting;
    }

    @Override
    public List<Component> validateInput(String input) {
        List<Component> errors = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            errors.add(Component.literal("Please enter a string"));
            return errors;
        }
        setting.set(input);
        return errors;
    }
}
