package com.somefrills.features.tweaks;

import com.google.common.collect.Sets;
import com.somefrills.config.FrillsConfig;
import com.somefrills.events.PlaceBlockEvent;
import com.somefrills.features.core.Feature;
import com.somefrills.features.core.Features;
import com.somefrills.misc.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;

import java.util.HashSet;

public class NoAbilityPlace extends Feature {
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

    public NoAbilityPlace() {
        super(FrillsConfig.instance.tweaks.noAbilityPlaceEnabled);
    }

    @EventHandler
    public static boolean onPlaceBlock(PlaceBlockEvent event) {
        return hasAbility(event.context);
    }

    public static boolean hasAbility(ItemPlacementContext context) {
        if (!Features.isActive(NoAbilityPlace.class)) return false;
        if (context == null) return false;
        ItemStack stack = context.getStack();
        String id = Utils.getSkyblockId(stack);
        if (!id.isEmpty() && (abilityWhitelist.contains(id) || id.startsWith("ABIPHONE"))) {
            return true;
        }
        return stack.getItem() instanceof BlockItem && Utils.hasRightClickAbility(stack);
    }
}
