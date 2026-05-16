package com.somefrills.events

import net.minecraft.text.Text

class ChatMsgEvent(var message: Text, messagePlain: String) : Cancellable() {
    var plainMessage: String = messagePlain
}
