package com.somefrills.events

import com.somefrills.misc.KeyAction
import net.minecraft.client.input.MouseButtonEvent

class MouseClickEvent(val click: MouseButtonEvent, @JvmField val action: KeyAction) : Cancellable() {
    fun button(): Int {
        return click.button()
    }
}
