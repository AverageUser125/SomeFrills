package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.somefrills.config.SettingInt;
import com.somefrills.config.SettingIntSlider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class SliderInt extends AbstractWidget implements IWidget {
    private final NumberInt numberBox;
    private final SliderWidget slider;
    private boolean updating = false;
    SettingIntSlider setting;
    public SliderInt(int x, int y, int widthNum, int widthSlider, int height, SettingIntSlider set) {
        super(x, y, widthNum + 5 + widthSlider, height, Component.empty());
        numberBox = new NumberInt(x, y, widthNum, height, setting);
        slider = new SliderWidget(x + widthNum + 5, y, widthSlider, height);
        slider.onValueChange(newValue -> {
            updateContext(() -> {
                int v = (int) Math.round(newValue * (setting.max() - setting.min()) + setting.min());
                numberBox.setNumber(v);
            });
        });

        numberBox.onValueChange(newValue -> {
            updateContext(() -> {
                double v = Math.max(setting.min(), Math.min(setting.max(), numberBox.getNumber()));
                double sliderValue = (setting.max() == setting.min()) ? 0.0 : (v - setting.min()) / (setting.max() - setting.min());
                slider.setValue(sliderValue);
            });
        });
        setting = set;
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
