package com.somefrills.events

import com.somefrills.misc.KeyAction
import io.github.notenoughupdates.moulconfig.gui.MouseEvent

class MouseClickEvent(val click: MouseEvent.Click, @JvmField val action: KeyAction) : Cancellable() {
    fun button(): Int {
        return click.mouseButton
    }
}
