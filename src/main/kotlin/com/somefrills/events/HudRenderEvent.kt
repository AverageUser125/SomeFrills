package com.somefrills.events

import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor

class HudRenderEvent(val context: GuiGraphicsExtractor, val textRenderer: Font, val tickCounter: DeltaTracker) :
    FrillsEvent()
