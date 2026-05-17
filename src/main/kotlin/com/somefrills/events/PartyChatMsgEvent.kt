package com.somefrills.events

import com.somefrills.Main.mc

class PartyChatMsgEvent(val message: String, val sender: String) : Cancellable() {
    val self get() = sender.equals(mc.session?.username, ignoreCase = true)
}
