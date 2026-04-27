package com.somefrills.features.tweaks;

import com.somefrills.config.FrillsConfig;
import com.somefrills.events.InteractBlockEvent;
import com.somefrills.events.InteractItemEvent;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;

import static com.somefrills.Main.mc;

// description moved into constructors

public class DoubleUseFix extends Feature {

    public DoubleUseFix() {
        super(FrillsConfig.instance.tweaks.doubleUseFixEnabled);
    }

    private static Type getDisableType() {
        ItemStack held = Utils.getHeldItem();
        if (held.getItem().equals(Items.FISHING_ROD)) {
            return Type.Rod;
        }
        if (Utils.getRightClickAbility(held).contains("Attunement")) {
            return Type.Dagger;
        }
        return Type.None;
    }

    @EventHandler
    private void onUseItem(InteractItemEvent event) {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType().equals(HitResult.Type.BLOCK) && getDisableType().equals(Type.Dagger)) {
            event.cancel();
        }
    }

    @EventHandler
    private void onUseBlock(InteractBlockEvent event) {
        if (!getDisableType().equals(Type.Rod)) return;
        if (mc.interactionManager == null || mc.player == null) return;

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        event.cancel();
    }

    private enum Type {
        Dagger,
        Rod,
        None
    }
}