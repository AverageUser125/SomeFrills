package com.somefrills.events;

import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class HudRenderEvent {
    public DrawContext context;
    public TextRenderer textRenderer;
    public RenderTickCounter tickCounter;

    public HudRenderEvent(DrawContext context, TextRenderer textRenderer, RenderTickCounter tickCounter) {
        this.context = context;
        this.textRenderer = textRenderer;
        this.tickCounter = tickCounter;
    }
}
