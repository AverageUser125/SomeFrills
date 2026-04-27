package com.somefrills.features.tweaks;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.tweaks.TweaksCategory.NoGhostBlocksConfig;
import com.somefrills.events.BreakBlockEvent;
import com.somefrills.events.PlaceBlockEvent;
import com.somefrills.features.core.Feature;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;

import static com.somefrills.Main.mc;

public class NoGhostBlocks extends Feature {
    private final NoGhostBlocksConfig config;

    public NoGhostBlocks() {
        super(FrillsConfig.instance.tweaks.noGhostBlocks.enabled);
        config = FrillsConfig.instance.tweaks.noGhostBlocks;
    }

    @EventHandler
    private void onBreakBlock(BreakBlockEvent event) {
        if (mc.isInSingleplayer() || !config.breaking) return;
        event.cancel();
        BlockState blockState = mc.world.getBlockState(event.blockPos);
        blockState.getBlock().onBreak(mc.world, event.blockPos, blockState, mc.player);
    }

    @EventHandler
    private void onPlaceBlock(PlaceBlockEvent event) {
        if (!config.placing) return;
        event.cancel();
    }
}
