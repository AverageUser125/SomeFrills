package com.somefrills.events

import com.somefrills.Main.mc

class PartyChatMsgEvent(var message: String, var sender: String) : Cancellable() {
    var self: Boolean = sender.equals(mc.session?.username, ignoreCase = true)
}
