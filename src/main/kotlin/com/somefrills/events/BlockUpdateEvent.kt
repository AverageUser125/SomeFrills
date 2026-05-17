package com.somefrills.events

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class BlockUpdateEvent(val pos: BlockPos, val oldState: BlockState, val newState: BlockState) : FrillsEvent()