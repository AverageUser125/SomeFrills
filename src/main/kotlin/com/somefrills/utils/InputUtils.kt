package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

object InputUtils {
    fun matchesKeyInternal(
        binding: KeyMapping,
        keyInput: KeyEvent?,
        mouseInput: MouseButtonEvent?
    ): Boolean {
        return (keyInput != null && binding.matches(keyInput)) ||
                (mouseInput != null && binding.matchesMouse(mouseInput))
    }
}

// ========== KeyMapping Extension Functions ==========

fun KeyMapping.matches(keyInput: KeyEvent?): Boolean {
    return InputUtils.matchesKeyInternal(this, keyInput, null)
}

fun KeyMapping.matches(mouseInput: MouseButtonEvent?): Boolean {
    return InputUtils.matchesKeyInternal(this, null, mouseInput)
}

fun KeyMapping.matches(keyInput: KeyEvent?, mouseInput: MouseButtonEvent?): Boolean {
    return InputUtils.matchesKeyInternal(this, keyInput, mouseInput)
}


// ========== Input State Extensions ==========

val isLeftMouseDown: Boolean
    get() = mc.options.keyAttack.isDown

val isRightMouseDown: Boolean
    get() = mc.options.keyUse.isDown

val isForwardPressed: Boolean
    get() = mc.options.keyUp.isDown

val isBackwardPressed: Boolean
    get() = mc.options.keyDown.isDown

val isLeftPressed: Boolean
    get() = mc.options.keyLeft.isDown

val isRightPressed: Boolean
    get() = mc.options.keyRight.isDown

val isJumpPressed: Boolean
    get() = mc.options.keyJump.isDown

val isShiftPressed: Boolean
    get() = mc.options.keyShift.isDown

val isCtrlPressed: Boolean
    get() = mc.options.keySprint.isDown