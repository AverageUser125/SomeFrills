package com.somefrills.misc

import org.lwjgl.glfw.GLFW

enum class KeyAction {
    Press,
    Repeat,
    Release;

    companion object {
        @JvmStatic
        fun get(action: Int): KeyAction {
            return when (action) {
                GLFW.GLFW_PRESS -> KeyAction.Press
                GLFW.GLFW_RELEASE -> KeyAction.Release
                else -> KeyAction.Repeat
            }
        }
    }
}