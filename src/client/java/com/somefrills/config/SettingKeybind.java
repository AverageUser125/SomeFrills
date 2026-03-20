package com.somefrills.config;

import org.lwjgl.glfw.GLFW;

public class SettingKeybind extends SettingInt {
    public SettingKeybind(int defaultValue) {
        super(defaultValue);
    }

    public int key() {
        return this.value();
    }

    public boolean bound() {
        return this.value() != GLFW.GLFW_KEY_UNKNOWN;
    }

    public boolean isKey(int key) {
        return key != GLFW.GLFW_KEY_UNKNOWN && key == this.value();
    }
}