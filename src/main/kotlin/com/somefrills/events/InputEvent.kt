package com.somefrills.events

import com.somefrills.misc.KeyAction
import net.minecraft.client.input.KeyInput

class InputEvent(input: KeyInput, @JvmField var action: KeyAction) : Cancellable() {
    @JvmField
    var key: Int = input.key()
    var modifiers: Int = input.modifiers()
    var keyInput: KeyInput? = input
}
