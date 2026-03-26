package com.somefrills.hud.components;

import com.daqem.uilib.api.widget.IWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

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
}
