package com.somefrills.features.tweaks;

import com.google.common.collect.Sets;
import com.somefrills.config.Feature;
import com.somefrills.misc.Utils;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPlacementContext;

import java.util.HashSet;

public class NoAbilityPlace {
    public static final Feature instance = new Feature("noAbilityPlace", true);
    private static final HashSet<String> abilityWhitelist = Sets.newHashSet(
            "ABINGOPHONE",
            "SUPERBOOM_TNT",
            "INFINITE_SUPERBOOM_TNT",
            "ARROW_SWAPPER",
            "PUMPKIN_LAUNCHER",
            "SNOW_CANNON",
            "SNOW_BLASTER",
            "SNOW_HOWITZER"
    );

    public static boolean hasAbility(ItemPlacementContext context) {
        if (!instance.isActive()) return false;
        if (context == null) return false;
        ItemStack stack = context.getStack();
        String id = Utils.getSkyblockId(stack);
        if (!id.isEmpty()) {
            if (abilityWhitelist.contains(id) || id.startsWith("ABIPHONE")) {
                return true;
            }
        }
        return stack.getItem() instanceof BlockItem && Utils.hasRightClickAbility(stack);
    }
}
