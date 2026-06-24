package com.somefrills.features.mining

import com.somefrills.Main.mc
import com.somefrills.config.FrillsMod

import com.somefrills.events.BlockUpdateEvent
import com.somefrills.features.core.AreaFeature
import com.somefrills.modules.FrillsFeature
import com.somefrills.misc.Area
import com.somefrills.utils.isStainedGlass
import com.somefrills.events.core.EventHandle
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.CrossCollisionBlock.*

@FrillsFeature
object GemstoneDesyncFix : AreaFeature(FrillsMod.config.mining.gemstoneDesyncFixEnabled) {
    @EventHandle
    private fun onBlock(event: BlockUpdateEvent) {
        val level = mc.level ?: return
        if (event.newState.isAir && event.oldState.isStainedGlass()) {
            event.newState.updateNeighbourShapes(level, event.pos, Block.UPDATE_ALL);
        }
    }

    @JvmStatic
    fun onGetUpdateState(original: BlockState): BlockState {
        if (isEnabled() && isDefaultPane(original)) {
            return asFullPane(original)
        }
        return original
    }

    private fun isDefaultPane(state: BlockState): Boolean {
        return state.isStainedGlass() && !isConnectedPane(state)
    }

    private fun isConnectedPane(state: BlockState): Boolean {
        return state.getValue(NORTH) || state.getValue(EAST) || state.getValue(SOUTH) || state.getValue(WEST)
    }

    private fun asFullPane(state: BlockState): BlockState {
        return state.setValue(NORTH, true).setValue(EAST, true).setValue(SOUTH, true).setValue(WEST, true)
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