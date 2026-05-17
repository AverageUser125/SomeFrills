package com.somefrills.events

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter

class HudRenderEvent(val context: DrawContext, val textRenderer: TextRenderer, val tickCounter: RenderTickCounter) :
    FrillsEvent()
