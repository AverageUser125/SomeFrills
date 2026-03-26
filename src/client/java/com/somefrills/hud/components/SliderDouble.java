package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class SliderDouble extends AbstractWidget implements IWidget {
    private final NumberDouble numberBox;
    private final SliderWidget slider;
    private final double minValue;
    private final double maxValue;
    private boolean updating = false;

    public SliderDouble(int x, int y, int widthNum, int widthSlider, int height, double min, double max) {
        super(x, y, widthNum + 5 + widthSlider, height, Component.empty());
        numberBox = new NumberDouble(x, y, widthNum, height, 0);
        slider = new SliderWidget(x + widthNum + 5, y, widthSlider, height);
        this.minValue = min;
        this.maxValue = max;
        slider.onValueChange(newValue -> {
            updateContext(() -> {
                double value = newValue * (maxValue - minValue) + minValue;
                numberBox.setNumber(value);
            });
        });

        numberBox.onValueChange(newValue -> {
            updateContext(() -> {
                double v = Math.max(minValue, Math.min(maxValue, numberBox.getNumber()));
                double sliderValue = (maxValue == minValue) ? 0.0 : (v - minValue) / (maxValue - minValue);
                slider.setValue(sliderValue);
            });
        });
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        numberBox.render(guiGraphics, i, j, f);
        slider.render(guiGraphics, i, j, f);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        numberBox.updateNarration(narrationElementOutput);
        slider.updateNarration(narrationElementOutput);
    }

    private void updateContext(Runnable function){
        if(updating) return;
        try {
            updating = true;
            function.run();
        } finally {
            updating = false;
        }
    }
}
