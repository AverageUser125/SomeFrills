package com.somefrills.misc;

import com.somefrills.events.TickEventPost;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class Input {
    private static final boolean[] keys = new boolean[512];
    private static final boolean[] buttons = new boolean[16];

    private static final boolean[] keyConsumed = new boolean[512];
    private static final boolean[] buttonConsumed = new boolean[16];

    private static final long RELEASE_DELAY_MS = 50L;

    private static final long[] keyReleaseAt = new long[512];
    private static final boolean[] keyReleasePending = new boolean[512];

    private static final long[] buttonReleaseAt = new long[16];
    private static final boolean[] buttonReleasePending = new boolean[16];

    private Input() {
    }

    @EventHandler
    public static void update(TickEventPost event) {
        long now = System.currentTimeMillis();

        for (int i = 0; i < keys.length; i++) {
            if (keyReleasePending[i] && now >= keyReleaseAt[i]) {
                keys[i] = false;
                keyConsumed[i] = false; // allow next real press
                keyReleasePending[i] = false;
            }
        }

        for (int i = 0; i < buttons.length; i++) {
            if (buttonReleasePending[i] && now >= buttonReleaseAt[i]) {
                buttons[i] = false;
                buttonConsumed[i] = false;
                buttonReleasePending[i] = false;
            }
        }
    }

    public static void setKeyState(int key, boolean pressed) {
        if (key < 0 || key >= keys.length) return;

        long now = System.currentTimeMillis();

        if (pressed) {
            // first press only
            if (!keys[key] && !keyConsumed[key]) {
                KeybindManager.onKeyPressed(key);
                keyConsumed[key] = true;
            }

            keys[key] = true;

            // cancel pending delayed release
            keyReleasePending[key] = false;

        } else {
            // schedule release
            keyReleaseAt[key] = now + RELEASE_DELAY_MS;
            keyReleasePending[key] = true;
        }
    }

    public static void setButtonState(int button, boolean pressed) {
        if (button < 0 || button >= buttons.length) return;

        long now = System.currentTimeMillis();

        if (pressed) {
            if (!buttons[button] && !buttonConsumed[button]) {
                // KeybindManager.onMousePressed(button);
                buttonConsumed[button] = true;
            }

            buttons[button] = true;
            buttonReleasePending[button] = false;

        } else {
            buttonReleaseAt[button] = now + RELEASE_DELAY_MS;
            buttonReleasePending[button] = true;
        }
    }

    public static int getKey(KeyBinding bind) {
        return bind.boundKey.getCode();
    }

    public static void setKeyState(KeyBinding bind, boolean pressed) {
        setKeyState(getKey(bind), pressed);
    }

    public static boolean isPressed(KeyBinding bind) {
        return isKeyPressed(getKey(bind)) || isButtonPressed(getKey(bind));
    }

    public static boolean isKeyPressed(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return false;
        return key >= 0 && key < keys.length && keys[key];
    }

    public static boolean isButtonPressed(int button) {
        if (button == -1) return false;
        return button >= 0 && button < buttons.length && buttons[button];
    }

    public static int getModifier(int key) {
        return switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> GLFW.GLFW_MOD_SHIFT;
            case GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL -> GLFW.GLFW_MOD_CONTROL;
            case GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT -> GLFW.GLFW_MOD_ALT;
            case GLFW.GLFW_KEY_LEFT_SUPER, GLFW.GLFW_KEY_RIGHT_SUPER -> GLFW.GLFW_MOD_SUPER;
            default -> 0;
        };
    }
}