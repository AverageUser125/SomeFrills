package com.somefrills.events

import net.minecraft.text.Text

class OverlayMsgEvent(val message: Text, messagePlain: String) : Cancellable()
