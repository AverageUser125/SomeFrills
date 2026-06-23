package com.somefrills.events

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

class StartBreakingBlockEvent(val blockPos: BlockPos, val direction: Direction) : Cancellable()
