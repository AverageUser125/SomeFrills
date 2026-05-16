package com.somefrills.events

import com.somefrills.misc.KeyAction
import net.minecraft.client.gui.Click
import net.minecraft.client.input.MouseInput

class MouseClickEvent(var click: Click, @JvmField var action: KeyAction) : Cancellable() {
    var input: MouseInput = click.buttonInfo()

    fun button(): Int {
        return this.input.button()
    }
}
