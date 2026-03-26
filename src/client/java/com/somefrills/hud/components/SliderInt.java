package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.somefrills.config.SettingIntSlider;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class SliderInt extends ClickableWidget implements IWidget {
    private final NumberInt numberBox;
    private final SliderWidget slider;
    private boolean updating = false;
    private final SettingIntSlider setting;
    private Consumer<Integer> consumer = null;

    public SliderInt(int x, int y, int widthNum, int widthSlider, int height, SettingIntSlider set) {
        super(x, y, widthNum + 5 + widthSlider, height, Text.empty());
        this.setting = set;
        numberBox = new NumberInt(x, y, widthNum, height, set);
        slider = new SliderWidget(x + widthNum + 5, y, widthSlider, height);
        slider.onValueChange(newValue -> {
            updateContext(() -> {
                int v = (int) Math.round(newValue * (this.setting.max() - this.setting.min()) + this.setting.min());
                numberBox.setNumber(v);
            });
        });

        numberBox.onValueChange(newValue -> {
            updateContext(() -> {
                double v = Math.max(this.setting.min(), Math.min(this.setting.max(), numberBox.getNumber()));
                double sliderValue = (this.setting.max() == this.setting.min()) ? 0.0 : (v - this.setting.min()) / (this.setting.max() - this.setting.min());
                slider.setValue(sliderValue);
            });
        });
        // causes the slider and number box to initialize to the correct value
        numberBox.setNumber(set.value());
    }

    @Override
    protected void renderWidget(DrawContext guiGraphics, int i, int j, float f) {
        numberBox.render(guiGraphics, i, j, f);
        slider.render(guiGraphics, i, j, f);
    }

    private void updateContext(Runnable function) {
        if (updating) return;
        try {
            updating = true;
            function.run();
        } finally {
            updating = false;
            if(consumer != null) consumer.accept(numberBox.getNumber());
        }
    }

    @Override
    public void mouseMoved(double d, double e) {
        numberBox.mouseMoved(d, e);
        slider.mouseMoved(d, e);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        if (numberBox != null) {
            numberBox.setX(x);
        }
        if (slider != null) {
            slider.setX(x + numberBox.getWidth() + 5);
        }
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        if (numberBox != null) numberBox.setY(y);
        if (slider != null) slider.setY(y);
    }

    @Override
    public boolean mouseClicked(Click mouseButtonEvent, boolean bl) {
        if (numberBox.mouseClicked(mouseButtonEvent, bl)) return true;
        return slider.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseReleased(Click mouseButtonEvent) {
        boolean a = numberBox.mouseReleased(mouseButtonEvent);
        boolean b = slider.mouseReleased(mouseButtonEvent);
        return a || b;
    }

    @Override
    public boolean mouseDragged(Click mouseButtonEvent, double d, double e) {
        if (slider.mouseDragged(mouseButtonEvent, d, e)) return true;
        return numberBox.mouseDragged(mouseButtonEvent, d, e);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (numberBox.mouseScrolled(d, e, f, g)) return true;
        return slider.mouseScrolled(d, e, f, g);
    }

    @Override
    public boolean keyPressed(KeyInput keyEvent) {
        if (numberBox.keyPressed(keyEvent)) return true;
        return slider.keyPressed(keyEvent);
    }

    @Override
    public boolean keyReleased(KeyInput keyEvent) {
        if (numberBox.keyReleased(keyEvent)) return true;
        return slider.keyReleased(keyEvent);
    }

    @Override
    public boolean charTyped(CharInput characterEvent) {
        if (numberBox.charTyped(characterEvent)) return true;
        return slider.charTyped(characterEvent);
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return numberBox.isMouseOver(d, e) || slider.isMouseOver(d, e);
    }

    @Override
    public void setFocused(boolean bl) {
        numberBox.setFocused(bl);
        slider.setFocused(bl);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    @Override
    public boolean isFocused() {
        return numberBox.isFocused() || slider.isFocused();
    }

    public void onValueChange(Consumer<Integer> consumer) {
        this.consumer = consumer;
    }

    /**
     * Programmatically set the numeric value shown by this control (0..255 etc).
     * This updates both the number box and the slider, using the same internal
     * synchronization as user input.
     */
    public void setNumber(int number) {
        updateContext(() -> {
            numberBox.setNumber(number);
            double v = Math.max(this.setting.min(), Math.min(this.setting.max(), numberBox.getNumber()));
            double sliderValue = (this.setting.max() == this.setting.min()) ? 0.0 : (v - this.setting.min()) / (this.setting.max() - this.setting.min());
            slider.setValue(sliderValue);
        });
    }
}
