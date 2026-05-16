package com.somefrills.events

import net.minecraft.text.Text

class OverlayMsgEvent(var message: Text, messagePlain: String) : Cancellable()
