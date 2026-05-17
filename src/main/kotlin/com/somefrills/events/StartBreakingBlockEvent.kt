package com.somefrills.events

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class StartBreakingBlockEvent(val blockPos: BlockPos, val direction: Direction) : Cancellable()
