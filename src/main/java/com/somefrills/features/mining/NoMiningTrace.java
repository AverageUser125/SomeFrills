package com.somefrills.features.mining;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.mining.MiningCategory.NoMiningTraceConfig;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

public class NoMiningTrace extends Feature {
    private final NoMiningTraceConfig config;

    public NoMiningTrace() {
        super(FrillsConfig.instance.mining.noMiningTrace.enabled);
        config = FrillsConfig.instance.mining.noMiningTrace;
    }

    private boolean isPassable(Entity entity) {
        if (entity instanceof ArmorStandEntity stand) {
            if (stand.isInvisible()) {
                // Do not pass through mineshaft entrance armor stand. As they are naked and invisible.
                if (!Utils.isNaked(stand)) return true;
            }
        }
        if (entity instanceof PlayerEntity player) {
            if (Utils.isRealPlayer(player)) return true;
        }
        if (entity instanceof ProjectileEntity) {
            return true;
        }
        return false;
    }

    private boolean isHoldingTool() {
        if (!config.onlyWhenHoldingTool) return true;
        ItemStack mainHand = Utils.getHeldItem();
        if (mainHand.isEmpty()) return false;
        if (mainHand.isOf(Items.PRISMARINE_SHARD)) return true; // For drills, DO NOT FIX THIS!
        if (mainHand.isIn(ItemTags.PICKAXES)) return true; // For pickaxe
        if (mainHand.isIn(ItemTags.SHOVELS)) return true; // For shovels
        return false;
    }

    public boolean canWork(Entity entity) {
        if (!isActive()) return false;
        if (!isHoldingTool()) return false;
        return isPassable(entity);
    }
}
