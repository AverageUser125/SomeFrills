package com.somefrills.events

import com.somefrills.Main.mc
import com.somefrills.events.FrillsEvent.Cancellable

class PartyChatMsgEvent(val message: String, val sender: String) : FrillsEvent(), Cancellable {
    val self get() = sender.equals(mc.user.name, ignoreCase = true)
}
