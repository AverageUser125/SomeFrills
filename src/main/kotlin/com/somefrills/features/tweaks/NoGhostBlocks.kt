package com.somefrills.features.tweaks

import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.config.tweaks.TweaksCategory.NoGhostBlocksConfig
import com.somefrills.events.BreakBlockEvent
import com.somefrills.events.PlaceBlockEvent
import com.somefrills.features.core.Feature
import com.somefrills.features.core.FrillsFeature
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.BlockState

@FrillsFeature
object NoGhostBlocks : Feature(FrillsMod.config.tweaks.noGhostBlocks.enabled) {
    @JvmStatic
    val config: NoGhostBlocksConfig
        get() = FrillsMod.config.tweaks.noGhostBlocks

    @EventHandler
    private fun onBreakBlock(event: BreakBlockEvent) {
        val world = mc.world ?: return
        if (mc.isInSingleplayer || !config.breaking) return
        event.cancel()
        val blockState: BlockState = world.getBlockState(event.blockPos)
        blockState.block.onBreak(world, event.blockPos, blockState, mc.player)
    }

    @EventHandler
    private fun onPlaceBlock(event: PlaceBlockEvent) {
        if (!config.placing) return
        event.cancel()
    }
}
