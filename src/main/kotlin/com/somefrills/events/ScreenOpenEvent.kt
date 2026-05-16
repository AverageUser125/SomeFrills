package com.somefrills.events

import net.minecraft.client.gui.screen.Screen

// TODO: make this cancellable
class ScreenOpenEvent(var screen: Screen) : FrillsEvent()
