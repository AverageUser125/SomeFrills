package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.EditBoxWidget;
import com.somefrills.config.SettingDouble;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.somefrills.Main.mc;

/**
 * Simple integer-only edit box widget. Uses UILib's EditBoxWidget so it
 * integrates with the validation tooltip machinery. The widget exposes
 * getNumber/setNumber and validates input to ensure only integers are accepted.
 */
public class NumberDouble extends EditBoxWidget {
    private SettingDouble num;

    public NumberDouble(SettingDouble number) {
        this(0, 0, number);
    }

    public NumberDouble(int x, int y, SettingDouble number) {
        this(x, y, ButtonWidget.DEFAULT_WIDTH, number);
    }

    public NumberDouble(int x, int y, int width, SettingDouble number) {
        this(x, y, width, ButtonWidget.DEFAULT_HEIGHT, number);
    }

    public NumberDouble(int x, int y, int width, int height, SettingDouble number) {
        super(mc.textRenderer, x, y, width, height, Text.empty());
        this.num = number;
        this.setText(Double.toString(number.value()));
    }

    public double getNumber() {
        return this.num.value();
    }

    public void setNumber(double number) {
        this.num.set( number);
        this.setText(Double.toString(number));
    }

    void onValueChange(Consumer<Double> callback) {
        this.setChangedListener(str -> {
            List<Text> errors = validateInput(str);
            if (errors.isEmpty()) {
                callback.accept(Double.parseDouble(str.trim()));
            }
        });
    }

    /**
     * Validate that the current text is an integer. Returns a non-empty list
     * of components when validation fails so the UILib tooltip will appear.
     */
    @Override
    public List<Text> validateInput(String input) {
        List<Text> errors = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            errors.add(Text.literal("Please enter a number"));
            return errors;
        }
        try {
            Double.parseDouble(input.trim());
        } catch (NumberFormatException ex) {
            errors.add(Text.literal("Not a valid integer"));
        }
        return errors;
    }
}
