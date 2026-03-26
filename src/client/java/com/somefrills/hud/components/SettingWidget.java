package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import static com.somefrills.Main.mc;

public class SettingWidget extends AbstractWidget implements IWidget {
    private final Component label;
    private final IWidget settingWidget;

    public SettingWidget(int x, int y, int textWidth, int height, String label, String description, IWidget settingWidget) {
        super(x, y, textWidth, height, Component.literal(label));
        this.label = Component.literal(label);
        settingWidget.setX(x + textWidth + 5);
        settingWidget.setY(y);
        setTooltip(Tooltip.create(Component.literal(description)));
        this.settingWidget = settingWidget;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        int textY = getY() + (getHeight() - mc.font.lineHeight) / 2;
        guiGraphics.drawString(mc.font, label, getX(), textY, 0xffffffff);
        settingWidget.render(guiGraphics, i, j, f);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        settingWidget.updateNarration(narrationElementOutput);
    }

    @Override
    public int getWidth() {
        return super.getWidth() + settingWidget.getWidth() + 5;
    }

    @Override
    public void mouseMoved(double d, double e) {
        settingWidget.mouseMoved(d, e);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        return settingWidget.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        return settingWidget.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
        return settingWidget.mouseDragged(mouseButtonEvent, d, e);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        return settingWidget.mouseScrolled(d, e, f, g);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        return settingWidget.keyPressed(keyEvent);
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        return settingWidget.keyReleased(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        return settingWidget.charTyped(characterEvent);
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return settingWidget.nextFocusPath(focusNavigationEvent);
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return settingWidget.isMouseOver(d, e);
    }

    @Override
    public void setFocused(boolean bl) {
        settingWidget.setFocused(bl);
    }

    @Override
    public boolean isFocused() {
        return settingWidget.isFocused();
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return settingWidget.shouldTakeFocusAfterInteraction();
    }

    @Override
    public @Nullable ComponentPath getCurrentFocusPath() {
        return settingWidget.getCurrentFocusPath();
    }

    @Override
    public ScreenRectangle getBorderForArrowNavigation(ScreenDirection screenDirection) {
        return settingWidget.getBorderForArrowNavigation(screenDirection);
    }
}
