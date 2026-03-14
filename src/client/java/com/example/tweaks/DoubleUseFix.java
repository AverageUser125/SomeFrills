package com.example.tweaks;

import com.example.utils.AllConfig;
import com.example.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;

import static com.example.Main.mc;

public class DoubleUseFix {
    private static type getDisableType() {
        ItemStack held = Utils.getHeldItem();
        if (held.getItem().equals(Items.FISHING_ROD)) {
            return type.Rod;
        }
        if (Utils.getRightClickAbility(held).contains("Attunement")) {
            return type.Dagger;
        }
        return type.None;
    }

    public static boolean onUseItem() {
        return AllConfig.doubleUseFix && mc.crosshairTarget != null && mc.crosshairTarget.getType().equals(HitResult.Type.BLOCK) && getDisableType().equals(type.Dagger);
    }

    public static boolean onUseBlock() {
        if (AllConfig.doubleUseFix && getDisableType().equals(type.Rod)) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            return true;
        }
        return false;
    }

    private enum type {
        Dagger,
        Rod,
        None
    }

}
