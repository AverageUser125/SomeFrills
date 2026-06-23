package com.somefrills.events

import net.minecraft.network.chat.Component

class OverlayMsgEvent(val message: Component, messagePlain: String) : Cancellable()
