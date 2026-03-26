package com.somefrills.config;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

    // Returns a human-readable label for the current binding (never a raw number)
    public Text getLabel() {
        int k = this.value();
        if (k == GLFW.GLFW_KEY_UNKNOWN)
            return Text.literal("Not Bound").formatted(Formatting.WHITE);
        return staticGetKeyLabel(k);
    }

    public static Text staticGetKeyLabel(int keycode) {
        if (keycode >= GLFW.GLFW_KEY_A && keycode <= GLFW.GLFW_KEY_Z) {
            char c = (char) ('A' + (keycode - GLFW.GLFW_KEY_A));
            return Text.literal(String.valueOf(c)).formatted(Formatting.WHITE);
        }
        if (keycode >= GLFW.GLFW_KEY_0 && keycode <= GLFW.GLFW_KEY_9) {
            char c = (char) ('0' + (keycode - GLFW.GLFW_KEY_0));
            return Text.literal(String.valueOf(c)).formatted(Formatting.WHITE);
        }
        if (keycode >= GLFW.GLFW_KEY_KP_0 && keycode <= GLFW.GLFW_KEY_KP_9) {
            int n = keycode - GLFW.GLFW_KEY_KP_0;
            return Text.literal("Num " + n).formatted(Formatting.WHITE);
        }
        switch (keycode) {
            case GLFW.GLFW_KEY_LEFT:
                return Text.literal("Left").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_RIGHT:
                return Text.literal("Right").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_UP:
                return Text.literal("Up").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_DOWN:
                return Text.literal("Down").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_PAGE_UP:
                return Text.literal("Page Up").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_PAGE_DOWN:
                return Text.literal("Page Down").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_HOME:
                return Text.literal("Home").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_END:
                return Text.literal("End").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_INSERT:
                return Text.literal("Insert").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_DELETE:
                return Text.literal("Delete").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_BACKSPACE:
                return Text.literal("Backspace").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_ENTER:
                return Text.literal("Enter").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_TAB:
                return Text.literal("Tab").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_SPACE:
                return Text.literal("Space").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_ESCAPE:
                return Text.literal("Escape").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_GRAVE_ACCENT:
                return Text.literal("` / ~").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_COMMA:
                return Text.literal(",").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_PERIOD:
                return Text.literal(".").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_SLASH:
                return Text.literal("/").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_SEMICOLON:
                return Text.literal(";").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_APOSTROPHE:
                return Text.literal("'").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_LEFT_BRACKET:
                return Text.literal("[").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_RIGHT_BRACKET:
                return Text.literal("]").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_MINUS:
                return Text.literal("-").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_EQUAL:
                return Text.literal("=").formatted(Formatting.WHITE);
            case GLFW.GLFW_KEY_BACKSLASH:
                return Text.literal("\\").formatted(Formatting.WHITE);
        }
        try {
            String name = org.lwjgl.glfw.GLFW.glfwGetKeyName(keycode, 0);
            if (name != null && !name.isEmpty()) {
                if (name.matches("\\d+")) {
                } else {
                    if (name.length() == 1)
                        return Text.literal(name.toUpperCase()).formatted(Formatting.WHITE);
                    return Text.literal(name.replace('_', ' ')).formatted(Formatting.WHITE);
                }
            }
        } catch (Throwable ignored) {
        }
        return Text.literal("Unknown").formatted(Formatting.WHITE);
    }

    private static String staticCapitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] parts = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            if (i < parts.length - 1) sb.append(' ');
        }
        return sb.toString();
    }
}