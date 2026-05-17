package com.somefrills.events

import com.somefrills.misc.KeyAction
import net.minecraft.client.gui.Click
import net.minecraft.client.input.MouseInput

class MouseClickEvent(val click: Click, @JvmField val action: KeyAction) : Cancellable() {
    val input: MouseInput get() = click.buttonInfo()

    fun button(): Int {
        return this.input.button()
    }
}
