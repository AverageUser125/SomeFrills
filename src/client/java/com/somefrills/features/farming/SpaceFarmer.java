package com.somefrills.features.farming;

import com.somefrills.config.FrillsConfig;
import com.somefrills.events.InputEvent;
import com.somefrills.events.ScreenOpenEvent;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.KeyAction;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

import static com.somefrills.Main.mc;

public class SpaceFarmer extends Feature {
    public static boolean spaceHeld = false;

    public SpaceFarmer() {
        super(FrillsConfig.instance.farming.spaceFarmerEnabled);
    }

    @EventHandler
    public void onKey(InputEvent event) {
        if (!isActive() || event.key != GLFW.GLFW_KEY_SPACE) {
            return;
        }
        if (mc.currentScreen != null && spaceHeld) {
            spaceHeld = false;
            mc.options.attackKey.setPressed(false);
            return;
        }
        if (event.action == KeyAction.Press && mc.options.sneakKey.isPressed() && Utils.isOnGardenPlot()) {
            spaceHeld = true;
            mc.options.attackKey.setPressed(true);
            event.cancel();
        } else if (event.action == KeyAction.Release && spaceHeld) {
            spaceHeld = false;
            if (mc.options.attackKey.isPressed()) {
                mc.options.attackKey.setPressed(false);
            }
            event.cancel();
        } else if (spaceHeld) {
            event.cancel();
        }
    }

    @EventHandler
    public void onScreen(ScreenOpenEvent event) {
        if (isActive() && spaceHeld) {
            spaceHeld = false;
            mc.options.attackKey.setPressed(false);
        }
    }
}
