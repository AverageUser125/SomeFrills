package com.somefrills.events

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState


class BlockUpdateEvent(val pos: BlockPos, val oldState: BlockState, val newState: BlockState) : FrillsEvent()