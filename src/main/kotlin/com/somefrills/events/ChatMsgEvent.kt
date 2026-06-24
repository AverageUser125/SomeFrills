package com.somefrills.events

import net.minecraft.network.chat.Component
import com.somefrills.events.FrillsEvent.Cancellable


class ChatMsgEvent(var message: Component, messagePlain: String) : FrillsEvent(), Cancellable {
    var plainMessage: String = messagePlain
}
