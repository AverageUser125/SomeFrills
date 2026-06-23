package com.somefrills.events

import net.minecraft.network.chat.Component


class ChatMsgEvent(var message: Component, messagePlain: String) : Cancellable() {
    var plainMessage: String = messagePlain
}
