package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.somefrills.config.SettingIntSlider;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class SliderInt extends AbstractWidget implements IWidget {
    private final NumberInt numberBox;
    private final SliderWidget slider;
    private boolean updating = false;
    private final SettingIntSlider setting;
    public SliderInt(int x, int y, int widthNum, int widthSlider, int height, SettingIntSlider set) {
        super(x, y, widthNum + 5 + widthSlider, height, Component.empty());
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
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (numberBox.mouseClicked(mouseButtonEvent, bl)) return true;
        return slider.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        boolean a = numberBox.mouseReleased(mouseButtonEvent);
        boolean b = slider.mouseReleased(mouseButtonEvent);
        return a || b;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
        if (slider.mouseDragged(mouseButtonEvent, d, e)) return true;
        return numberBox.mouseDragged(mouseButtonEvent, d, e);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        if (numberBox.mouseScrolled(d, e, f, g)) return true;
        return slider.mouseScrolled(d, e, f, g);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (numberBox.keyPressed(keyEvent)) return true;
        return slider.keyPressed(keyEvent);
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        if (numberBox.keyReleased(keyEvent)) return true;
        return slider.keyReleased(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        if (numberBox.charTyped(characterEvent)) return true;
        return slider.charTyped(characterEvent);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        var p = numberBox.nextFocusPath(focusNavigationEvent);
        if (p != null) return p;
        return slider.nextFocusPath(focusNavigationEvent);
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
    public boolean isFocused() {
        return numberBox.isFocused() || slider.isFocused();
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return numberBox.shouldTakeFocusAfterInteraction() || slider.shouldTakeFocusAfterInteraction();
    }

    @Override
    public @Nullable ComponentPath getCurrentFocusPath() {
        var p = numberBox.getCurrentFocusPath();
        if (p != null) return p;
        return slider.getCurrentFocusPath();
    }

    @Override
    public ScreenRectangle getBorderForArrowNavigation(ScreenDirection screenDirection) {
        ScreenRectangle r = numberBox.getBorderForArrowNavigation(screenDirection);
        return (r != null) ? r : slider.getBorderForArrowNavigation(screenDirection);
    }
}
