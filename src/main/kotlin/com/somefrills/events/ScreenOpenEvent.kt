package com.somefrills.events

import net.minecraft.client.gui.screens.Screen

// TODO: make this cancellable
class ScreenOpenEvent(val screen: Screen) : FrillsEvent()
