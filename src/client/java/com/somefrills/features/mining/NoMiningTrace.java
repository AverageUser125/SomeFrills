package com.somefrills.features.mining;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.mining.MiningCategory.NoMiningTraceConfig;
import com.somefrills.features.core.Feature;
import com.somefrills.misc.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class NoMiningTrace extends Feature {
    private final NoMiningTraceConfig config;
    private static final String[] miningTraceNames = new String[]{
            "Pickaxe",
            "Shovel",
            "Drill",
    };

    public NoMiningTrace() {
        super(FrillsConfig.instance.mining.noMiningTrace.enabled);
        config = FrillsConfig.instance.mining.noMiningTrace;
    }

    private boolean isHoldingTool() {
        if (!config.onlyWhenHoldingTool) return false;
        ItemStack mainHand = Utils.getHeldItem();
        if (mainHand.isEmpty()) return false;
        String itemName = mainHand.getItem().getName().getString();
        for (String traceName : miningTraceNames) {
            if (itemName.contains(traceName)) {
                return true;
            }
        }
        return false;
    }

    public boolean canWork(Entity entity) {
        if (!isActive()) return false;
        return isHoldingTool();
    }
}
