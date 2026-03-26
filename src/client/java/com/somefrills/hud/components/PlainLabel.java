package com.somefrills.hud.components;

import net.minecraft.network.chat.Component;

public class PlainLabel {
    private Component text;
    private String plainText = "";
    private String plainTooltip = "";

    public PlainLabel(Component text) { this.text = text; this.plainText = text.getString(); }

    public void setText(Component text) { this.text = text; this.plainText = text.getString(); }
    public Component getTextComponent() { return this.text; }
    public String getText() { return this.plainText; }
    public void setTooltip(Component tooltip) { this.plainTooltip = tooltip.getString().replaceAll("\n", " "); }
    public String getTooltip() { return this.plainTooltip; }
}