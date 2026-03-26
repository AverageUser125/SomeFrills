package com.somefrills.hud.components;

import com.daqem.uilib.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

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
    private double num;

    public NumberDouble(double number) {
        this(0, 0, number);
    }

    public NumberDouble(int x, int y, double number) {
        this(x, y, Button.DEFAULT_WIDTH, number);
    }

    public NumberDouble(int x, int y, int width, double number) {
        this(x, y, width, Button.DEFAULT_HEIGHT, number);
    }

    public NumberDouble(int x, int y, int width, int height, double number) {
        super(mc.font, x, y, width, height, Component.empty());
        this.num = number;
        this.setValue(Double.toString(number));
    }

    public double getNumber() {
        return this.num;
    }

    public void setNumber(double number) {
        this.num = number;
        this.setValue(Double.toString(number));
    }

    void onValueChange(Consumer<Double> callback) {
        this.setResponder(str -> {
            List<Component> errors = validateInput(str);
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
    public List<Component> validateInput(String input) {
        List<Component> errors = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            errors.add(Component.literal("Please enter a number"));
            return errors;
        }
        try {
            Double.parseDouble(input.trim());
        } catch (NumberFormatException ex) {
            errors.add(Component.literal("Not a valid integer"));
        }
        return errors;
    }
}
