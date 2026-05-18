package com.somefrills.utils

import com.somefrills.Main.mc
import net.minecraft.client.input.KeyInput
import net.minecraft.client.input.MouseInput
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.gui.Click
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket
import net.minecraft.util.Util

object InputUtils {
    fun matchesKeyInternal(binding: KeyBinding, keyInput: KeyInput?, mouseInput: MouseInput?): Boolean {
        return (keyInput != null && binding.matchesKey(keyInput)) ||
               (mouseInput != null && binding.matchesMouse(Click(0.0, 0.0, mouseInput)))
    }
}

// ========== KeyBinding Extension Functions ==========

fun KeyBinding.matches(keyInput: KeyInput?): Boolean {
    return InputUtils.matchesKeyInternal(this, keyInput, null)
}

fun KeyBinding.matches(mouseInput: MouseInput?): Boolean {
    return InputUtils.matchesKeyInternal(this, null, mouseInput)
}

fun KeyBinding.matches(keyInput: KeyInput?, mouseInput: MouseInput?): Boolean {
    return InputUtils.matchesKeyInternal(this, keyInput, mouseInput)
}

// ========== Input State Extensions ==========

val isLeftMouseDown: Boolean
    get() = mc.options.attackKey.isPressed

val isRightMouseDown: Boolean
    get() = mc.options.useKey.isPressed

val isForwardPressed: Boolean
    get() = mc.options.forwardKey.isPressed

val isBackwardPressed: Boolean
    get() = mc.options.backKey.isPressed

val isLeftPressed: Boolean
    get() = mc.options.leftKey.isPressed

val isRightPressed: Boolean
    get() = mc.options.rightKey.isPressed

val isJumpPressed: Boolean
    get() = mc.options.jumpKey.isPressed

val isShiftPressed: Boolean
    get() = mc.options.sneakKey.isPressed

val isCtrlPressed: Boolean
    get() = mc.options.sprintKey.isPressed



