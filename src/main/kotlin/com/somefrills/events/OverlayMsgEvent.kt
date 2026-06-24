package com.somefrills.events

import net.minecraft.network.chat.Component
import com.somefrills.events.FrillsEvent.Cancellable

class OverlayMsgEvent(val message: Component, messagePlain: String) : FrillsEvent(), Cancellable
