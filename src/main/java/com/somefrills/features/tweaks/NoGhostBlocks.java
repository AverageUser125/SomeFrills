package com.somefrills.features.tweaks;

import com.somefrills.config.FrillsConfig;
import com.somefrills.config.tweaks.TweaksCategory.NoGhostBlocksConfig;
import com.somefrills.events.BreakBlockEvent;
import com.somefrills.events.PlaceBlockEvent;
import com.somefrills.features.core.Feature;
import com.somefrills.features.core.FrillsFeature;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;

import static com.somefrills.Main.mc;

@FrillsFeature
public class NoGhostBlocks extends Feature {
    private final NoGhostBlocksConfig config;

    public NoGhostBlocks() {
        super(FrillsConfig.tweaks.noGhostBlocks.enabled);
        config = FrillsConfig.tweaks.noGhostBlocks;
    }

    @EventHandler
    private void onBreakBlock(BreakBlockEvent event) {
        if (mc.isInSingleplayer() || mc.world == null || !config.breaking) return;
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
