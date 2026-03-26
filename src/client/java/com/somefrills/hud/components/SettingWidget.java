package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import com.daqem.uilib.gui.component.text.TextComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.Click;
import net.minecraft.text.Text;

import static com.somefrills.Main.mc;

public class SettingWidget extends ClickableWidget implements IWidget {
    private final TextComponent label;
    private final IWidget settingWidget;

    public SettingWidget(int x, int y, int textWidth, int height, String str, String description, IWidget settingWidget) {
        super(x, y, textWidth, height, Text.literal(str));
        this.label = new TextComponent(x, y, Text.literal(str));
        settingWidget.setX(x + textWidth + 5);
        settingWidget.setY(y);
        setTooltip(Tooltip.of(Text.literal(description)));
        this.settingWidget = settingWidget;
    }

    @Override
    protected void renderWidget(DrawContext guiGraphics, int i, int j, float f) {
        label.render(guiGraphics, i, j, f);
        settingWidget.render(guiGraphics, i, j, f);
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
    public boolean mouseClicked(Click mouseButtonEvent, boolean bl) {
        return settingWidget.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseReleased(Click mouseButtonEvent) {
        return settingWidget.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseDragged(Click mouseButtonEvent, double d, double e) {
        return settingWidget.mouseDragged(mouseButtonEvent, d, e);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g) {
        return settingWidget.mouseScrolled(d, e, f, g);
    }

    @Override
    public boolean keyPressed(KeyInput keyEvent) {
        return settingWidget.keyPressed(keyEvent);
    }

    @Override
    public boolean keyReleased(KeyInput keyEvent) {
        return settingWidget.keyReleased(keyEvent);
    }

    @Override
    public boolean charTyped(CharInput characterEvent) {
        return settingWidget.charTyped(characterEvent);
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
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public boolean isFocused() {
        return settingWidget.isFocused();
    }
}
