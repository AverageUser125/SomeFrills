package com.somefrills.features.tweaks

import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.config.tweaks.TweaksCategory.NoGhostBlocksConfig
import com.somefrills.events.BreakBlockEvent
import com.somefrills.events.PlaceBlockEvent
import com.somefrills.features.core.Feature
import com.somefrills.modules.FrillsFeature
import com.somefrills.events.core.EventHandle
import net.minecraft.world.level.block.state.BlockState

@FrillsFeature
object NoGhostBlocks : Feature(FrillsMod.config.tweaks.noGhostBlocks.enabled) {
    @JvmStatic
    val config: NoGhostBlocksConfig
        get() = FrillsMod.config.tweaks.noGhostBlocks

    @EventHandle
    private fun onBreakBlock(event: BreakBlockEvent) {
        val world = mc.level ?: return
        val player = mc.player ?: return
        if (mc.isSingleplayer || !config.breaking) return
        event.cancel()
        val blockState: BlockState = world.getBlockState(event.blockPos)
        blockState.block.playerWillDestroy(world, event.blockPos, blockState, player)
    }

    @EventHandle
    private fun onPlaceBlock(event: PlaceBlockEvent) {
        if (!config.placing) return
        event.cancel()
    }
}
