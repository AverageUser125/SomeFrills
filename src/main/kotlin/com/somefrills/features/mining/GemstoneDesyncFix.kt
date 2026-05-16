package com.somefrills.features.mining

import com.somefrills.Main.mc
import com.somefrills.config.FrillsConfig
import com.somefrills.events.BlockUpdateEvent
import com.somefrills.features.core.AreaFeature
import com.somefrills.features.core.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.misc.Utils
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalConnectingBlock.*

@FrillsFeature
object GemstoneDesyncFix : AreaFeature(FrillsConfig.instance.mining.gemstoneDesyncFixEnabled) {
    @EventHandler
    private fun onBlock(event: BlockUpdateEvent) {
        if (event.newState.isAir && Utils.isStainedGlass(event.oldState)) {
            event.newState.updateNeighbors(mc.world, event.pos, NOTIFY_ALL)
        }
    }

    @JvmStatic
    fun onGetUpdateState(original: BlockState): BlockState {
        if (isEnabled && isDefaultPane(original)) {
            return asFullPane(original)
        }
        return original
    }

    private fun isDefaultPane(state: BlockState): Boolean {
        return Utils.isStainedGlass(state) && !isConnectedPane(state)
    }

    private fun isConnectedPane(state: BlockState): Boolean {
        return state.get(NORTH) || state.get(EAST) || state.get(SOUTH) || state.get(WEST)
    }

    private fun asFullPane(state: BlockState): BlockState {
        return state.with(NORTH, true).with(EAST, true).with(SOUTH, true).with(WEST, true)
    }

    override fun checkArea(area: Area): Boolean {
        return area in setOf(
            Area.DWARVEN_MINES,
            Area.CRYSTAL_HOLLOWS,
            Area.MINESHAFT,
            Area.CRIMSON_ISLE,
            Area.THE_RIFT
        )
    }

}