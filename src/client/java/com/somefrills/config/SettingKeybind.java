package com.somefrills.config;

import net.minecraft.network.chat.Component;
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
    public Component getLabel() {
        int k = this.value();
        if (k == GLFW.GLFW_KEY_UNKNOWN)
            return Component.literal("Not Bound").withStyle(net.minecraft.ChatFormatting.WHITE);
        return staticGetKeyLabel(k);
    }

    // Inlined helper from previous KeybindButton to avoid UI dependency
    public static Component staticGetKeyLabel(int keycode) {
        if (keycode >= GLFW.GLFW_KEY_A && keycode <= GLFW.GLFW_KEY_Z) {
            char c = (char) ('A' + (keycode - GLFW.GLFW_KEY_A));
            return Component.literal(String.valueOf(c)).withStyle(net.minecraft.ChatFormatting.WHITE);
        }
        if (keycode >= GLFW.GLFW_KEY_0 && keycode <= GLFW.GLFW_KEY_9) {
            char c = (char) ('0' + (keycode - GLFW.GLFW_KEY_0));
            return Component.literal(String.valueOf(c)).withStyle(net.minecraft.ChatFormatting.WHITE);
        }
        if (keycode >= GLFW.GLFW_KEY_KP_0 && keycode <= GLFW.GLFW_KEY_KP_9) {
            int n = keycode - GLFW.GLFW_KEY_KP_0;
            return Component.literal("Num " + n).withStyle(net.minecraft.ChatFormatting.WHITE);
        }
        switch (keycode) {
            case GLFW.GLFW_KEY_LEFT:
                return Component.literal("Left").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_RIGHT:
                return Component.literal("Right").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_UP:
                return Component.literal("Up").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_DOWN:
                return Component.literal("Down").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_PAGE_UP:
                return Component.literal("Page Up").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_PAGE_DOWN:
                return Component.literal("Page Down").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_HOME:
                return Component.literal("Home").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_END:
                return Component.literal("End").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_INSERT:
                return Component.literal("Insert").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_DELETE:
                return Component.literal("Delete").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_BACKSPACE:
                return Component.literal("Backspace").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_ENTER:
                return Component.literal("Enter").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_TAB:
                return Component.literal("Tab").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_SPACE:
                return Component.literal("Space").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_ESCAPE:
                return Component.literal("Escape").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_GRAVE_ACCENT:
                return Component.literal("` / ~").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_COMMA:
                return Component.literal(",").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_PERIOD:
                return Component.literal(".").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_SLASH:
                return Component.literal("/").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_SEMICOLON:
                return Component.literal(";").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_APOSTROPHE:
                return Component.literal("'").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_LEFT_BRACKET:
                return Component.literal("[").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_RIGHT_BRACKET:
                return Component.literal("]").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_MINUS:
                return Component.literal("-").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_EQUAL:
                return Component.literal("=").withStyle(net.minecraft.ChatFormatting.WHITE);
            case GLFW.GLFW_KEY_BACKSLASH:
                return Component.literal("\\").withStyle(net.minecraft.ChatFormatting.WHITE);
        }
        try {
            String name = org.lwjgl.glfw.GLFW.glfwGetKeyName(keycode, 0);
            if (name != null && !name.isEmpty()) {
                if (name.matches("\\d+")) {
                } else {
                    if (name.length() == 1)
                        return Component.literal(name.toUpperCase()).withStyle(net.minecraft.ChatFormatting.WHITE);
                    return Component.literal(name.replace('_', ' ')).withStyle(net.minecraft.ChatFormatting.WHITE);
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            com.mojang.blaze3d.platform.InputConstants.Key input = com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(keycode);
            String localized = input.getDisplayName().getString();
            String translation = input.getName();
            if (localized != null && !localized.isEmpty() && !localized.equals(translation)) {
                if (!localized.matches("\\d+")) return input.getDisplayName();
            }
            if (translation != null && !translation.isEmpty()) {
                String token = translation;
                if (token.startsWith("key.keyboard.")) token = token.substring("key.keyboard.".length());
                else if (token.startsWith("key.mouse.")) token = token.substring("key.mouse.".length());
                else if (token.contains(".")) token = token.substring(token.lastIndexOf('.') + 1);
                token = token.replace('_', ' ');
                String pretty = staticCapitalize(token);
                if (pretty.equalsIgnoreCase("grave accent")) pretty = "` / ~";
                return Component.literal(pretty).withStyle(net.minecraft.ChatFormatting.WHITE);
            }
        } catch (Throwable ignored) {
        }
        try {
            com.mojang.blaze3d.platform.InputConstants.Key mouse = com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(keycode);
            String mouseLabel = mouse.getDisplayName().getString();
            String mouseTrans = mouse.getName();
            if (mouseLabel != null && !mouseLabel.isEmpty() && !mouseLabel.equals(mouseTrans))
                return mouse.getDisplayName();
            if (keycode >= GLFW.GLFW_MOUSE_BUTTON_1 && keycode <= GLFW.GLFW_MOUSE_BUTTON_8) {
                int idx = keycode - GLFW.GLFW_MOUSE_BUTTON_1 + 1;
                return Component.literal("Mouse " + idx).withStyle(net.minecraft.ChatFormatting.WHITE);
            }
        } catch (Throwable ignored) {
        }
        return Component.literal("Unknown").withStyle(net.minecraft.ChatFormatting.WHITE);
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