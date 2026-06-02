package com.somefrills.misc

import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW

object Input {
    private val keys = BooleanArray(512)
    private val buttons = BooleanArray(16)

    private val keyConsumed = BooleanArray(512)
    private val buttonConsumed = BooleanArray(16)

    private const val RELEASE_DELAY_MS = 50L

    private val keyReleaseAt = LongArray(512)
    private val keyReleasePending = BooleanArray(512)

    private val buttonReleaseAt = LongArray(16)
    private val buttonReleasePending = BooleanArray(16)

    @JvmStatic
    fun update() {
        val now = System.currentTimeMillis()

        for (i in keys.indices) {
            if (keyReleasePending[i] && now >= keyReleaseAt[i]) {
                keys[i] = false
                keyConsumed[i] = false // allow next real press
                keyReleasePending[i] = false
            }
        }

        for (i in buttons.indices) {
            if (buttonReleasePending[i] && now >= buttonReleaseAt[i]) {
                buttons[i] = false
                buttonConsumed[i] = false
                buttonReleasePending[i] = false
            }
        }
    }

    @JvmStatic
    fun setKeyState(key: Int, pressed: Boolean) {
        if (key < 0 || key >= keys.size) return

        val now = System.currentTimeMillis()

        if (pressed) {
            // first press only
            if (!keys[key] && !keyConsumed[key]) {
                KeybindManager.onKeyPressed(key)
                keyConsumed[key] = true
            }

            keys[key] = true

            // cancel pending delayed release
            keyReleasePending[key] = false
        } else {
            // schedule release
            keyReleaseAt[key] = now + RELEASE_DELAY_MS
            keyReleasePending[key] = true
        }
    }

    @JvmStatic
    fun setButtonState(button: Int, pressed: Boolean) {
        if (button < 0 || button >= buttons.size) return

        val now = System.currentTimeMillis()

        if (pressed) {
            if (!buttons[button] && !buttonConsumed[button]) {
                // KeybindManager.onMousePressed(button);
                buttonConsumed[button] = true
            }

            buttons[button] = true
            buttonReleasePending[button] = false
        } else {
            buttonReleaseAt[button] = now + RELEASE_DELAY_MS
            buttonReleasePending[button] = true
        }
    }

    @JvmStatic
    fun getKey(bind: KeyBinding): Int {
        return bind.boundKey.code
    }

    fun setKeyState(bind: KeyBinding, pressed: Boolean) {
        setKeyState(getKey(bind), pressed)
    }

    @JvmStatic
    fun isPressed(bind: KeyBinding): Boolean {
        return isKeyPressed(getKey(bind)) || isButtonPressed(getKey(bind))
    }

    @JvmStatic
    fun isKeyPressed(key: Int): Boolean {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return false
        return key >= 0 && key < keys.size && keys[key]
    }

    fun isButtonPressed(button: Int): Boolean {
        if (button == -1) return false
        return button >= 0 && button < buttons.size && buttons[button]
    }

    @JvmStatic
    fun getModifier(key: Int): Int {
        return when (key) {
            GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> GLFW.GLFW_MOD_SHIFT
            GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> GLFW.GLFW_MOD_CONTROL
            GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> GLFW.GLFW_MOD_ALT
            GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER -> GLFW.GLFW_MOD_SUPER
            else -> 0
        }
    }
}