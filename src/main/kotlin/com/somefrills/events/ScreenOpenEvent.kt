package com.somefrills.events

import com.somefrills.modules.PrimaryFunction
import net.minecraft.client.gui.screens.Screen

// TODO: make this cancellable
@PrimaryFunction("onScreenOpen")
class ScreenOpenEvent(val screen: Screen) : FrillsEvent()
