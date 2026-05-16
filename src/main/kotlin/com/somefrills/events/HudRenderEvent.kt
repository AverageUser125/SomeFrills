package com.somefrills.events

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter

class HudRenderEvent(var context: DrawContext, var textRenderer: TextRenderer, var tickCounter: RenderTickCounter) :
    FrillsEvent()
