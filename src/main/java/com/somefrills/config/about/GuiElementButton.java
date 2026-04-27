package com.somefrills.config.about;

import io.github.notenoughupdates.moulconfig.common.RenderContext;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;

import java.awt.*;

public class GuiElementButton {

    public static final int HEIGHT = 18 + 5;
    public int width = -1;
    private String text = "";

    public GuiElementButton() {
    }

    public int getWidth(RenderContext context) {
        var fr = context.getMinecraft().getDefaultFontRenderer();
        return fr.getStringWidth(text) + 10;
    }

    public void render(RenderContext context, int x, int y) {
        context.drawColoredRect(x, y, x + width, y + 18, Color.WHITE.getRGB());
        context.drawColoredRect(x + 1, y + 1, x + width - 1, y + 18 - 1, Color.BLACK.getRGB());
        var fr = context.getMinecraft().getDefaultFontRenderer();
        context.drawString(fr, StructuredText.of(text), x + 5, y + 5, -1, true);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

