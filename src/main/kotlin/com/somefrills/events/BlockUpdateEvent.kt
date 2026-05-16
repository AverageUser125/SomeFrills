package com.somefrills.events

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class BlockUpdateEvent(var pos: BlockPos, var oldState: BlockState, var newState: BlockState) : FrillsEvent()