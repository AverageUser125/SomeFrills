package com.somefrills.events

import com.somefrills.misc.KeyAction
import net.minecraft.client.input.MouseButtonEvent
import com.somefrills.events.FrillsEvent.Cancellable

class MouseClickEvent(val click: MouseButtonEvent, @JvmField val action: KeyAction) : FrillsEvent(), Cancellable {
    fun button(): Int {
        return click.button()
    }
}
