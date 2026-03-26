package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.daqem.uilib.gui.widget.EditBoxWidget;
import com.somefrills.config.SettingString;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.somefrills.Main.mc;

public class StringButton extends EditBoxWidget implements IWidget {
    private final SettingString setting;
    public StringButton(int x, int y, int width, int height, SettingString setting) {
        super(mc.textRenderer, x, y, width, height, Text.literal(setting.value()));
        this.setting = setting;
    }

    @Override
    public List<Text> validateInput(String input) {
        List<Text> errors = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            errors.add(Text.literal("Please enter a string"));
            return errors;
        }
        setting.set(input);
        return errors;
    }
}
