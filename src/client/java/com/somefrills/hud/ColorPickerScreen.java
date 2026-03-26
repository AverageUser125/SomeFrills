package com.somefrills.hud;

import com.daqem.uilib.gui.AbstractScreen;
import com.somefrills.config.SettingColor;
import com.somefrills.config.SettingIntSlider;
import com.somefrills.hud.components.ColorSquare;
import com.somefrills.hud.components.SliderInt;
import com.somefrills.hud.components.SliderWidget;
import com.somefrills.misc.RenderColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static com.somefrills.Main.mc;

public class ColorPickerScreen extends AbstractScreen {
    private final SettingColor color;
    private final Screen previous;
    private ColorSquare square;
    private SliderWidget sliderHue;
    private SliderInt sliderR, sliderG, sliderB;
    private boolean updating = false;

    public ColorPickerScreen(SettingColor color, Screen previous) {
        super(Text.of("Pick a color"));
        this.color = color;
        this.previous = previous;
    }

    @Override
    protected void init() {
        super.init();
        square = new ColorSquare(10, 10, 200, 200, color.value());
        sliderHue = new SliderWidget(220, 10, 20, 200);
        sliderHue.onValueChange(newValue -> {
            updateContext(() -> {
                float hue = newValue.floatValue();
                square.setHuePercentage(hue);
                // update current color using the square's selected saturation/value
                int rgb = square.getSelectedColor();
                RenderColor rColor = RenderColor.fromArgb(rgb);
                // update R/G/B sliders to match new hue selection
                sliderR.setNumber((int) Math.round(rColor.r * 255f));
                sliderG.setNumber((int) Math.round(rColor.g * 255f));
                sliderB.setNumber((int) Math.round(rColor.b * 255f));
            });
        });

        sliderHue.setValue(square.getHue());
        sliderR = new SliderInt(10, 220, 30, 170, 20, new SettingIntSlider((int) (color.value().r * 255f), 0, 255));
        sliderG = new SliderInt(10, 250, 30, 170, 20, new SettingIntSlider((int) (color.value().g * 255f), 0, 255));
        sliderB = new SliderInt(10, 290, 30, 170, 20, new SettingIntSlider((int) (color.value().b * 255f), 0, 255));
        sliderR.onValueChange(newValue -> {
            updateContext(() -> {
                // SliderInt provides 0..255 integers; convert to 0..1 float for RenderColor
                color.set(color.value().withRed(newValue / 255.0f));
                RenderColor rColor = color.value();
                float[] hsv = ColorSquare.rgbToHsv(rColor.r, rColor.g, rColor.b);
                sliderHue.setValue(hsv[0]);
                square.setHuePercentage(hsv[0]);
                square.setSelection(hsv[1], hsv[2]);
            });
        });
        sliderB.onValueChange(newValue -> {
            updateContext(() -> {
                color.set(color.value().withBlue(newValue / 255.0f));
                RenderColor rColor = color.value();
                float[] hsv = ColorSquare.rgbToHsv(rColor.r, rColor.g, rColor.b);
                sliderHue.setValue(hsv[0]);
                square.setHuePercentage(hsv[0]);
                square.setSelection(hsv[1], hsv[2]);
            });
        });
        sliderG.onValueChange(newValue -> {
            updateContext(() -> {
                color.set(color.value().withGreen(newValue / 255.0f));
                RenderColor rColor = color.value();
                float[] hsv = ColorSquare.rgbToHsv(rColor.r, rColor.g, rColor.b);
                sliderHue.setValue(hsv[0]);
                square.setHuePercentage(hsv[0]);
                square.setSelection(hsv[1], hsv[2]);
            });
        });

        // When the user clicks/drags the color square, update the setting and sync other controls
        square.setSelectionListener(selectedArgb -> updateContext(() -> {
            // selectedArgb is an ARGB int; SettingGeneric.parse accepts numbers
            color.set(selectedArgb);
            RenderColor rColor = color.value();
            float[] hsv = ColorSquare.rgbToHsv(rColor.r, rColor.g, rColor.b);
            sliderHue.setValue(hsv[0]);
            // ensure the square and sliders reflect the selected color
            square.setHuePercentage(hsv[0]);
            square.setSelection(hsv[1], hsv[2]);
            sliderR.setNumber((int) Math.round(rColor.r * 255f));
            sliderG.setNumber((int) Math.round(rColor.g * 255f));
            sliderB.setNumber((int) Math.round(rColor.b * 255f));
        }));

        addWidget(sliderR);
        addWidget(sliderG);
        addWidget(sliderB);
        addWidget(sliderHue);
        addWidget(square);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        color.set(square.getSelectedColor());
        square.dispose();
        mc.setScreen(previous);
    }

    private void updateContext(Runnable function) {
        if (updating) return;
        try {
            updating = true;
            function.run();
        } finally {
            updating = false;
        }
    }
}
