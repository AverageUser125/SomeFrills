package com.somefrills.features.fishing;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;
import net.minecraft.util.Identifier;

public class AutoFishKeybinds {
    private static KeyBinding toggleModKey;
    private static KeyBinding toggleVerboseKey;

    public static void register() {
        var category = new KeyBinding.Category(Identifier.of("autofish"));
        toggleModKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autofish.toggle_mod",
            GLFW.GLFW_KEY_O,
                category
        ));
        toggleVerboseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autofish.toggle_verbose",
            GLFW.GLFW_KEY_V,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleModKey.isPressed()) {
                AutoFish.instance.setActive(!AutoFish.instance.isActive());
            }
            if (toggleVerboseKey.isPressed()) {
                AutoFish.verbose.set(!AutoFish.verbose.value());
            }
        });
    }
}
